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
                //rootTag내의 최상단에 xml 태그 추가
                .headerCallback(headerCallback)
                .build();
    }

    @Bean
    public MultiResourceItemWriter<Customer> multiCustomerFileWriter() throws Exception {
        return new MultiResourceItemWriterBuilder<Customer>()
                .name("multiCustomerFileWriter")
                .delegate(delegateItemWriter())
                /*
                 * 각 리소스에 쓰기 작업할 아이템 개수. (각 리소스 파일당 아이템 개수만큼 작성됨)
                 * step의 청크 개수에 종속됨. 25개씩 작성하더라도 청크가 10이면 25개가 아니라 30개가 작성됨.
                 * 25개째에 리소스를 새로 열어야하지만 청크는 10개 단위이므로 5개를 더 추가로 작성해야함.
                 */
                .itemCountLimitPerResource(25)
                .resource(new FileSystemResource("chapter9-multiResource-xml/output/customer"))
                //파일 확장자 적용
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
