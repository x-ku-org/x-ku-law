package cn.xku.law.subscription;

import cn.xku.law.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户私有数据 Owner 校验集成测试：以订阅规则为代表，验证 A 不能改/删/查 B 的私有数据。
 */
class OwnerValidationIT extends AbstractIntegrationTest {

    @Test
    void userCannotModifyOrSeeAnotherUsersRule() throws Exception {
        insertUser("bob", "Bob@123", PLATFORM_TENANT_ID);
        insertUser("carol", "Carol@123", PLATFORM_TENANT_ID);

        TokenPair bob = login(PLATFORM_TENANT_CODE, "bob", "Bob@123");
        TokenPair carol = login(PLATFORM_TENANT_CODE, "carol", "Carol@123");

        // bob 创建订阅规则
        String createResp = postJson("/subscription/rules", bob.accessToken(),
                Map.of("ruleName", "bob-rule", "keyword", "tax"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long ruleId = objectMapper.readTree(createResp).get("data").asLong();
        assertThat(ruleId).isPositive();

        // carol 改/删 bob 的规则 → 403（OwnerValidator.checkOwner）
        putJson("/subscription/rules/" + ruleId, carol.accessToken(),
                Map.of("ruleName", "hacked")).andExpect(status().isForbidden());
        deleteJson("/subscription/rules/" + ruleId, carol.accessToken()).andExpect(status().isForbidden());

        String carolList = getJson("/subscription/rules", carol.accessToken())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode list = objectMapper.readTree(carolList).get("data").get("list");
        assertThat(list).allSatisfy(node -> assertThat(node.get("id").asLong()).isNotEqualTo(ruleId));

        // bob 本人可以正常改自己的规则
        putJson("/subscription/rules/" + ruleId, bob.accessToken(),
                Map.of("ruleName", "bob-rule-v2")).andExpect(status().isOk());
    }
}
