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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;

import javax.sql.DataSource;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class JdbcReadBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    /**
     * ResultSet을 생성하면서 커서를 열고, 스프링 배치가 read 메서드를 호출할 때마다 도매인 객체로 매핑할 로우를 가져옴.
     * DataSource, 실행할 쿼리, RowMapper 구현제가 필요.
     */
    @Bean
    public JdbcCursorItemReader<Customer> customerItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .dataSource(dataSource)
                .sql("select * from customer where city = ?")
                .rowMapper(new CustomerRowMapper())
                //SQL에 파라미터를 설정
                .preparedStatementSetter(citySetter(null))
                .build();
    }

    @Bean
    @StepScope
    public ArgumentPreparedStatementSetter citySetter(
            @Value("#{jobParameters['city']}") String city
    ) {
        return new ArgumentPreparedStatementSetter(new Object[] {city});
    }

    @Bean
    public ItemWriter<Object> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("jdbcReadStep")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("jdbcReadJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {

		SpringApplication.run(JdbcReadBatchApplication.class, "city=Chicago");
    }
}
