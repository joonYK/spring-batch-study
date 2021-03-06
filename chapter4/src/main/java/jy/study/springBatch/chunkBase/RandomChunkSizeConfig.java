package jy.study.springBatch.chunkBase;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

//@EnableBatchProcessing
//@Configuration
public class RandomChunkSizeConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job chunkBasedJob() {
        return this.jobBuilderFactory.get("randomChunkJob")
                .start(chunkStep())
                .build();
    }

    @Bean
    public Step chunkStep() {
        return this.stepBuilderFactory.get("randomChunkStep")
                .<String, String>chunk(randomChunkSizePolicy())
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

    @Bean
    public CompletionPolicy randomChunkSizePolicy() {
        return new RandomChunkSizePolicy();
    }

    /**
     * CompletionPolicy ??? ?????????????????? ????????? ????????? ???????????? ??????
     */
    public static class RandomChunkSizePolicy implements CompletionPolicy {

        private int chunkSize;
        private int totalProcessed;
        private final Random random = new Random();

        //?????? ?????? ????????? ????????? ???????????? ?????? ?????? ??????
        @Override
        public boolean isComplete(RepeatContext context, RepeatStatus result) {
            if (RepeatStatus.FINISHED == result)
                return true;
            else
                return isComplete(context);
        }

        //?????? ????????? ????????? ?????? ?????? ?????? ??????
        @Override
        public boolean isComplete(RepeatContext context) {
            return this.totalProcessed >= chunkSize;
        }

        //????????? ????????? ??? ??? ????????? ?????? ?????????
        @Override
        public RepeatContext start(RepeatContext parent) {
            this.chunkSize = random.nextInt(20);
            this.totalProcessed = 0;

            System.out.println("The chunk size has been set to " + this.chunkSize);

            return parent;
        }

        //??? ???????????? ???????????? update ???????????? ??? ?????? ???????????? ?????? ?????? ??????
        @Override
        public void update(RepeatContext context) {
            this.totalProcessed++;
        }
    }
}
