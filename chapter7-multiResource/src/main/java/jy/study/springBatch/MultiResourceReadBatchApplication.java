package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class MultiResourceReadBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    /**
     * 여러개의 파일을 읽어야 하기 때문에 Resource 배열을 파라미터로 받음.
     */
    @Bean
    @StepScope
    public MultiResourceItemReader multiCustomerReader(
        @Value("#{jobParameters['customerFile']}") Resource[] inputFiles
    ) {
        return new MultiResourceItemReaderBuilder<>()
                .name("multiResourceReader")
                .resources(inputFiles)
                .delegate(customerFileReader())
                .build();
    }

    @Bean
    public CustomerFileReader customerFileReader() {
        return new CustomerFileReader(customerItemReader());
    }

    /**
     * 여러개의 리소스를 주입받아서 사용해야하기 때문에 직접 리소스를 구성하지 않음.
     */
    @Bean
    @StepScope
    public FlatFileItemReader customerItemReader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .lineMapper(lineTokenizer())
                .build();
    }

    @Bean
    public PatternMatchingCompositeLineMapper lineTokenizer() {
        Map<String, LineTokenizer> lineTokenizers = new HashMap<>(2);

        //레코드가 CUST 또는 TRANS 로 시작하는지 구분해서 LineTokenizer 실행.
        lineTokenizers.put("CUST*", customerLineTokenizer());
        lineTokenizers.put("TRANS*", transactionLineTokenizer());

        Map<String, FieldSetMapper> fieldSetMappers = new HashMap<>(2);

        BeanWrapperFieldSetMapper<Customer> customerFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        customerFieldSetMapper.setTargetType(Customer.class);

        //레코드가 CUST 또는 TRANS 로 시작하는지 구분해서 FieldSetMapper 실행.
        fieldSetMappers.put("CUST*", customerFieldSetMapper);
        //BeanWrapperFieldSetMapper 는 특수한 타입의 필드(Date, Double 등)를 변환할 수 없다. (String 만 가능)
        //커스텀 FieldSetMapper 를 생성해서 사용.
        fieldSetMappers.put("TRANS*", new TransactionFieldSetMapper());

        PatternMatchingCompositeLineMapper lineMappers = new PatternMatchingCompositeLineMapper();

        lineMappers.setTokenizers(lineTokenizers);
        lineMappers.setFieldSetMappers(fieldSetMappers);

        return lineMappers;
    }

    @Bean
    public DelimitedLineTokenizer transactionLineTokenizer() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();

        lineTokenizer.setNames("prefix", "accountNumber", "transactionDate", "amount");
        return lineTokenizer;
    }

    @Bean
    public DelimitedLineTokenizer customerLineTokenizer() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();

        lineTokenizer.setNames(
                "firstName", "middleInitial", "lastName", "address",
                "city", "state", "zipCode"
        );

        //prefix ("CUST") 는 제외하기 위해 1번 필드부터 7번 필드까지만 사용
        lineTokenizer.setIncludedFields(1, 2, 3, 4, 5, 6, 7);
        return lineTokenizer;
    }

    @Bean
    public ItemWriter<Object> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("multiRecordReadStep")
                .<Customer, Customer>chunk(10)
                .reader(customerFileReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("multiRecordReadJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        List<String> realArgs = Collections.singletonList("customerFile=/input/customerMultiFormat*");

		SpringApplication.run(MultiResourceReadBatchApplication.class, realArgs.toArray(new String[1]));
    }
}
