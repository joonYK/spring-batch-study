package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class FixedLengthFileReadBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> customerItemReader(
            @Value("#{jobParameters['customerFile']}") Resource inputFile
    ) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .resource(inputFile)
                //고정 너비 파일을 처리하는 구성.
                //FixedLengthTokenizer를 생성하는 빌더가 반환됨. (각 줄을 파싱해 FieldSet으로 만드는 LineTokenizer 구현체)
                .fixedLength()
                //각 레코드에서 파싱해야 할 컬럼의 시작 위치와 종료 위치.
                .columns(
                        new Range(1, 11), new Range(12, 12), new Range(13, 22), new Range(23, 26),
                        new Range(27, 46), new Range(47, 62), new Range(63, 64), new Range(65, 69)
                )
                //각 레코드에서 파싱해야할 컬럼의 이름들.
                .names(
                        "firstName", "middleInitial", "lastName", "addressNumber",
                        "street", "city", "state", "zipCode"
                )
                //빌더가 BeanWrapperFieldSetMapper(파일의 레코드를 객체로 변환하는 LineMapper 구현체) 생성.
                .targetType(Customer.class)
                .build();
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("fixedLengthFileReadStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("fixedLengthFileReadJob")
                .start(step())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(FixedLengthFileReadBatchApplication.class, args);
    }
}
