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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class ClassifierCompositeWriterBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> customerItemReader(
            @Value("#{jobParameters['customerFile']}") Resource inputFile
    ) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .delimited()
                .names("firstName", "middleInitial", "lastName", "address",
                        "city", "state", "zipCode")
                .targetType(Customer.class)
                .resource(inputFile)
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<Customer> xmlDelegateItemWriter(
            @Value("#{jobParameters['outputFile']}") Resource outputFile
    ) {
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        marshaller.afterPropertiesSet();

        return new StaxEventItemWriterBuilder<Customer>()
                .name("customerItemWriter")
                .resource(outputFile)
                .marshaller(marshaller)
                .rootTagName("customers")
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Customer> jdbcDelegateItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .namedParametersJdbcTemplate(new NamedParameterJdbcTemplate(dataSource))
                .sql("INSERT INTO CUSTOMER (first_name," +
                        "middle_initial, " +
                        "last_name, " +
                        "address, " +
                        "city, " +
                        "state, " +
                        "zipcode) " +
                        "VALUES(:firstName, " +
                        ":middleInitial, " +
                        ":lastName, " +
                        ":address, " +
                        ":city, " +
                        ":state, " +
                        ":zipcode)")
                .beanMapped()
                .build();
    }

    @Bean
    public ClassifierCompositeItemWriter<Customer> classifierCompositeItemWriter() {
        Classifier<Customer, ItemWriter<? super Customer>> classifier =
                new CustomerClassifier(
                        xmlDelegateItemWriter(null),
                        jdbcDelegateItemWriter(null));

        return new ClassifierCompositeItemWriterBuilder<Customer>()
                .classifier(classifier)
                .build();
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("classifierCompositeWriteStep")
                .<Customer, Customer>chunk(5)
                .reader(customerItemReader(null))
                .writer(classifierCompositeItemWriter())

                /*
                 * ItemStream ?????????????????? ??????????????? ????????? ???????????? ??????(open, close, update)??????.
                 * ?????? ?????? ???????????? ????????? ????????? ??????????????? open ???????????? ????????? ????????? ?????? close??? ?????????.
                 * update ???????????? ??? ????????? ????????? ??? ?????? ??????(????????? ????????? ??? ???)??? ????????????.
                 *
                 * ClassifierCompositeItemWriter??? ItemStream??? ???????????? ?????? ????????? xml ????????? ?????? ?????? ????????????,
                 * StaxEventItemWriter??? write ???????????? ???????????? ?????? ????????? ??????.
                 * ????????? ItemStream??? ????????? ItemReader??? ItemWriter??? stream?????? ???????????? ??????.
                 *
                 * JdbcBatchItemWriter??? ????????? ???????????? ?????? ItemStream??? ???????????? ????????? ??????.
                 */
                .stream(xmlDelegateItemWriter(null))
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("classifierCompositeWriteJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ClassifierCompositeWriterBatchApplication.class,
                "customerFile=/input/customer.csv",
                "outputFile=file:output/xmlCustomer.xml");
    }
}
