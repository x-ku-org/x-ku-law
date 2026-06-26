package cn.xku.law.auth;

import cn.xku.law.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 自助注册集成测试，覆盖：
 *  - 注册成功后返回令牌、自动登录可访问 /auth/me，且新用户为 normal、落 platform 租户、无角色；
 *  - 用户名 / 手机号唯一性冲突分别返回对应业务错误码（HTTP 409）；
 *  - 参数校验失败（弱口令）返回 400。
 */
class RegisterIT extends AbstractIntegrationTest {

    @Test
    void registerThenAutoLoginAndAccessMe() throws Exception {
        String json = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "newcomer",
                                "password", "Pwd@123",
                                "mobile", "13900000001"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode data = objectMapper.readTree(json).get("data");
        String accessToken = data.get("accessToken").asText();
        assertThat(accessToken).isNotBlank();
        assertThat(data.get("refreshToken").asText()).isNotBlank();

        // 新用户落 platform 租户、normal 类型、enabled 状态
        Long tenantId = jdbcTemplate.queryForObject(
                "SELECT tenant_id FROM lr_user WHERE username=?", Long.class, "newcomer");
        assertThat(tenantId).isEqualTo(PLATFORM_TENANT_ID);
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT user_type, status FROM lr_user WHERE username=?", "newcomer");
        assertThat(row.get("user_type")).isEqualTo("normal");
        assertThat(row.get("status")).isEqualTo("enabled");

        // 自动登录令牌可访问 /auth/me，且角色为空、租户为 platform
        getJson("/auth/me", accessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userType").value("normal"))
                .andExpect(jsonPath("$.data.tenantCode").value(PLATFORM_TENANT_CODE))
                .andExpect(jsonPath("$.data.roles").isEmpty());
    }

    @Test
    void duplicateUsernameRejected() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "dupuser", "password", "Pwd@123", "mobile", "13900000002"))))
                .andExpect(status().isOk());

        // 同用户名、不同手机号 → 用户名冲突
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "dupuser", "password", "Pwd@123", "mobile", "13900000003"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1007));
    }

    @Test
    void duplicateMobileRejected() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "mobuser1", "password", "Pwd@123", "mobile", "13900000004"))))
                .andExpect(status().isOk());

        // 不同用户名、同手机号 → 手机号冲突
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "mobuser2", "password", "Pwd@123", "mobile", "13900000004"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1008));
    }

    @Test
    void weakPasswordRejectedByValidation() throws Exception {
        // 参数校验失败由 GlobalExceptionHandler.handleMethodArgumentNotValid 返回普通 CommonResult
        // （HTTP 200，业务 code=400），区别于 AppException 走 ResponseEntity 设置 HTTP 状态。
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "weakpwd", "password", "123", "mobile", "13900000005"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        // 校验拦截后不应落库
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lr_user WHERE username=?", Integer.class, "weakpwd");
        assertThat(count).isZero();
    }
}
