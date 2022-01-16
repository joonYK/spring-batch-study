package jy.study.springBatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class SpringBatchChapter6Application {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(SpringBatchChapter6Application.class);

        Properties properties = new Properties();
        //애플리케이션이 기동되는 시점에 어떠한 잡도 실행되지 않도록 설정.
        properties.put("spring.batch.job.enabled", false);
        springApplication.setDefaultProperties(properties);

        springApplication.run(args);
    }
}
