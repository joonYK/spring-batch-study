package jy.study.springBatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class HibernateReadBatchApplication {

    public static void main(String[] args) {

		SpringApplication.run(HibernateReadBatchApplication.class, "city=Chicago");
    }
}
