package jy.study.springBatch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ImportJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    private final Step importCustomerUpdates;

    private final Step importTransactions;

    private final Step applyTransactions;

    private final Step generateStatements;

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("importJob")
                .start(importCustomerUpdates)
                .next(importTransactions)
                .next(applyTransactions)
                .next(generateStatements)
                .incrementer(new RunIdIncrementer())
                .build();
    }


}
