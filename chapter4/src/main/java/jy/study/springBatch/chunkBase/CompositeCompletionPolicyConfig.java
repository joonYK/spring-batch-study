package jy.study.springBatch.chunkBase;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EnableBatchProcessing
@Configuration
public class CompositeCompletionPolicyConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job chunkBasedJob() {
        return this.jobBuilderFactory.get("chunkBasedJob2")
                .start(chunkStep())
                .build();
    }

    @Bean
    public Step chunkStep() {
        return this.stepBuilderFactory.get("chunkStep2")
                .<String, String>chunk(completionPolicy())
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public ListItemReader<String> itemReader() {
        List<String> items = new ArrayList<>(100000);

        for (int i = 0; i < 100000; i++) {
            items.add(UUID.randomUUID().toString());
        }

        return new ListItemReader<>(items);
    }

    @Bean
    public ItemWriter<? super String> itemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println(">> current item = " + item);
            }
        };
    }

    /**
     * CompletionPolicy 는 청크가 완료되는 시점을 프로그래밍 방식으로 정의할 수 있게 제공.
     */
    @Bean
    public CompletionPolicy completionPolicy() {
        //여러 정책을 함께 구성.
        //여러 정책 중 하나라도 청크 완료라고 판단되면 해당 청크가 완료된 것으로 표시
        CompositeCompletionPolicy policy = new CompositeCompletionPolicy();

        policy.setPolicies(
                new CompletionPolicy[] {
                        //해당 시간이 넘을 때 완료로 간주 (밀리세컨드)
                        new TimeoutTerminationPolicy(3),
                        //아이템을 해당 임곗값만큼 처리하면 완료된 것으로 간주.
                        new SimpleCompletionPolicy(1000)
                }
        );

        return policy;
    }
}
