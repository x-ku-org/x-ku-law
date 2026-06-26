package cn.xku.law.aspect;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.system.domain.OperationLogDO;
import cn.xku.law.system.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private static final String MASK = "***";
    private static final Set<String> SENSITIVE_FIELD_MARKERS = Set.of(
            "password", "token", "secret", "credential", "authorization");
    private static final Pattern SENSITIVE_TEXT_PATTERN = Pattern.compile(
            "(?i)(password|token|secret|credential|authorization)(\\s*[=:]\\s*)([^,\\]\\)}\\s]+)");

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint pjp, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        String status = "success";
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            status = "fail";
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;
            saveLog(operLog, status, (int) duration, pjp.getArgs());
        }
    }

    private void saveLog(OperLog operLog, String status, int durationMs, Object[] args) {
        try {
            OperationLogDO entity = new OperationLogDO();
            entity.setUserId(SecurityUtils.getCurrentUserId());
            entity.setModuleName(operLog.module());
            entity.setOperationType(operLog.type());
            entity.setResponseStatus(status);
            entity.setDurationMs(durationMs);
            entity.setOperationTime(LocalDateTime.now());

            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                entity.setRequestMethod(request.getMethod());
                entity.setRequestUrl(request.getRequestURI());
                entity.setIp(resolveClientIp(request));
            }

            if (args != null && args.length > 0) {
                String params = serializeRequestParams(args);
                entity.setRequestParams(params.length() > 2000 ? params.substring(0, 2000) : params);
            }

            operationLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("[OperLogAspect] 写操作日志失败，不影响业务: {}", e.getMessage());
        }
    }

    private String serializeRequestParams(Object[] args) {
        try {
            ArrayNode array = objectMapper.createArrayNode();
            for (Object arg : args) {
                array.add(sanitizeArg(arg));
            }
            return objectMapper.writeValueAsString(array);
        } catch (Exception e) {
            return maskSensitiveText(Arrays.toString(args));
        }
    }

    private JsonNode sanitizeArg(Object arg) {
        try {
            JsonNode node = objectMapper.valueToTree(arg);
            maskJson(node);
            return node;
        } catch (Exception e) {
            return objectMapper.getNodeFactory().textNode(maskSensitiveText(String.valueOf(arg)));
        }
    }

    private void maskJson(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (isSensitiveField(field.getKey())) {
                    objectNode.put(field.getKey(), MASK);
                } else {
                    maskJson(field.getValue());
                }
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                maskJson(item);
            }
        }
    }

    private boolean isSensitiveField(String fieldName) {
        String normalized = fieldName.toLowerCase(Locale.ROOT);
        return SENSITIVE_FIELD_MARKERS.stream().anyMatch(normalized::contains);
    }

    private String maskSensitiveText(String text) {
        return SENSITIVE_TEXT_PATTERN.matcher(text).replaceAll("$1$2" + MASK);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
