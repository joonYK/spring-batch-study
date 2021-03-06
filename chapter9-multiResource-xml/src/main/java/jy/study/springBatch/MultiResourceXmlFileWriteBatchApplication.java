package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class MultiResourceXmlFileWriteBatchApplication {


    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final CustomerOutputFileSuffixCreator suffixCreator;

    private final CustomerXmlHeaderCallback headerCallback;

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
    public StaxEventItemWriter<Customer> delegateItemWriter() throws Exception {
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        marshaller.afterPropertiesSet();

        return new StaxEventItemWriterBuilder<Customer>()
                .name("customerItemWriter")
                .marshaller(marshaller)
                .rootTagName("customers")
                //rootTag?????? ???????????? xml ?????? ??????
                .headerCallback(headerCallback)
                .build();
    }

    @Bean
    public MultiResourceItemWriter<Customer> multiCustomerFileWriter() throws Exception {
        return new MultiResourceItemWriterBuilder<Customer>()
                .name("multiCustomerFileWriter")
                .delegate(delegateItemWriter())
                /*
                 * ??? ???????????? ?????? ????????? ????????? ??????. (??? ????????? ????????? ????????? ???????????? ?????????)
                 * step??? ?????? ????????? ?????????. 25?????? ?????????????????? ????????? 10?????? 25?????? ????????? 30?????? ?????????.
                 * 25????????? ???????????? ?????? ?????????????????? ????????? 10??? ??????????????? 5?????? ??? ????????? ???????????????.
                 */
                .itemCountLimitPerResource(25)
                .resource(new FileSystemResource("chapter9-multiResource-xml/output/customer"))
                //?????? ????????? ??????
                .resourceSuffixCreator(suffixCreator)
                .build();
    }

    @Bean
    public Step step() throws Exception {
        return this.stepBuilderFactory.get("multiResourceWriteStep")
                .<Customer, Customer>chunk(10)
                .reader(customerJdbcCursorItemReader(null))
                .writer(multiCustomerFileWriter())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        return this.jobBuilderFactory.get("multiResourceWriteJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(MultiResourceXmlFileWriteBatchApplication.class, "customerFile=/input/customer.csv");
    }
}
