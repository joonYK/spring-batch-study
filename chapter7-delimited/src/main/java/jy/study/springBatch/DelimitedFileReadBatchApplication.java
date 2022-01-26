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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class DelimitedFileReadBatchApplication {

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
                //필드가 구분자로 구분된 파일 처리하는 구성. (기본값 쉼표)
                //DelimitedLineTokenizer. (각 줄을 파싱해 FieldSet으로 만드는 LineTokenizer 구현체)
                .delimited()
                //각 레코드에서 파싱해야할 컬럼의 이름들.
                .names("firstName", "middleInitial", "lastName", "addressNumber",
                        "street", "city", "state", "zipCode")
                //빌더가 BeanWrapperFieldSetMapper(파일의 레코드를 객체로 변환하는 LineMapper 구현체) 생성.
                //.targetType(Customer.class)
                .fieldSetMapper(new CustomerFieldSetMapper())
                .build();
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("delimitedFileReadStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("delimitedFileReadJob")
                .start(step())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(DelimitedFileReadBatchApplication.class, args);
    }
}
