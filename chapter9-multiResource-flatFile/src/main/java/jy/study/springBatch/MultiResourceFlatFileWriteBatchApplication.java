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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class MultiResourceFlatFileWriteBatchApplication {


    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final CustomerOutputFileSuffixCreator suffixCreator;

    @Bean
    public JdbcCursorItemReader<Customer>  customerJdbcCursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .dataSource(dataSource)
                .sql("select * from customer")
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Customer> delegateItemWriter(CustomerRecordCountFooterCallback footerCallback) {
        BeanWrapperFieldExtractor<Customer> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"firstName", "lastName", "address", "city", "state", "zipcode"});
        fieldExtractor.afterPropertiesSet();

        FormatterLineAggregator<Customer> lineAggregator = new FormatterLineAggregator<>();

        lineAggregator.setFormat("%s %s lives at %s %s in %s, %s.");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<Customer> itemWriter = new FlatFileItemWriter<>();

        itemWriter.setName("delegateCustomerItemWriter");
        itemWriter.setLineAggregator(lineAggregator);
        itemWriter.setAppendAllowed(true);
        itemWriter.setFooterCallback(footerCallback);

        return itemWriter;
    }

    @Bean
    public MultiResourceItemWriter<Customer> multiCustomerFileWriter() {
        return new MultiResourceItemWriterBuilder<Customer>()
                .name("multiCustomerFileWriter")
                .delegate(delegateItemWriter(null))
                /*
                 * ??? ???????????? ?????? ????????? ????????? ??????. (??? ????????? ????????? ????????? ???????????? ?????????)
                 * step??? ?????? ????????? ?????????. 25?????? ?????????????????? ????????? 10?????? 25?????? ????????? 30?????? ?????????.
                 * 25????????? ???????????? ?????? ?????????????????? ????????? 10??? ??????????????? 5?????? ??? ????????? ???????????????.
                 */
                .itemCountLimitPerResource(25)
                .resource(new FileSystemResource("chapter9-multiResource-flatFile/output/customer"))
                //?????? ????????? ??????
                .resourceSuffixCreator(suffixCreator)
                .build();
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("multiResourceFlatFileWriteStep")
                .<Customer, Customer>chunk(10)
                .reader(customerJdbcCursorItemReader(null))
                .writer(multiCustomerFileWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("multiResourceFlatFileWriteJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(MultiResourceFlatFileWriteBatchApplication.class, "customerFile=/input/customer.csv");
    }
}
