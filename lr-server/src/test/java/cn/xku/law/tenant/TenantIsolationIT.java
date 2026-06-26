package cn.xku.law.tenant;

import cn.xku.law.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 多租户隔离集成测试：验证 TenantLineHandler 行级注入在真实查询中生效——
 * 平台租户管理员（tenant_id=1）查询用户列表时，看不到其他租户（tenant_id=2）的用户。
 * （白名单 38 张表的策略由 TenantLineHandlerTest 单测断言，此处补端到端验证。）
 */
class TenantIsolationIT extends AbstractIntegrationTest {

    @Test
    void adminCannotSeeUsersOfAnotherTenant() throws Exception {
        insertUser("plat_user", "Pwd@123", PLATFORM_TENANT_ID); // 同租户(1)
        insertUser("acme_user", "Pwd@123", 2L);                 // 其他租户(2)

        TokenPair admin = loginAdmin();
        String body = getJson("/system/users", admin.accessToken())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // lr_user 非白名单表 → 注入 tenant_id=1：只见本租户用户
        assertThat(body).contains("plat_user");
        assertThat(body).doesNotContain("acme_user");
    }
}
