package jy.study.springBatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class ExampleBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleBatchApplication.class,
                "customerUpdateFile=/input/customer_update.csv",
                "transactionFile=/input/transactions.xml",
                "outputDirectory=output");

    }
}
