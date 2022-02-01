package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import jy.study.springBatch.domain.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.util.Collections;
import java.util.List;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class XmlReadBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public StaxEventItemReader<Customer> customerFileReader(
            @Value("#{jobParameters['customerFile']}") Resource inputFile
    ) {
        return new StaxEventItemReaderBuilder<Customer>()
                .name("customerFileReader")
                .resource(inputFile)
                // XML 내에서 아이템으로 취급할 각 XML 프래그먼트의 루트 엘리먼트를 식별하는데 사용.
                .addFragmentRootElements("customer")
                .unmarshaller(customerMarshaller())
                .build();
    }

    @Bean
    public Unmarshaller customerMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(Customer.class, Transaction.class);
        return jaxb2Marshaller;
    }

    @Bean
    public ItemWriter<Object> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("xmlReadStep")
                .<Customer, Customer>chunk(10)
                .reader(customerFileReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("xmlReadJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {
        List<String> realArgs = Collections.singletonList("customerFile=input/customer.xml");
		SpringApplication.run(XmlReadBatchApplication.class, realArgs.toArray(new String[1]));
    }
}
