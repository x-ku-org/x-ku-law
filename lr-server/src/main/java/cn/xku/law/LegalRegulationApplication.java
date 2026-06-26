package cn.xku.law;

import cn.xku.law.config.StorageProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 法规智询启动类 */
@SpringBootApplication(scanBasePackages = "cn.xku.law")
@MapperScan("cn.xku.law.**.mapper")
@EnableConfigurationProperties(StorageProperties.class)
@EnableAsync
@EnableScheduling
public class LegalRegulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegalRegulationApplication.class, args);
    }
}
