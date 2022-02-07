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
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 페이지라 부르는 청크 단위로 레코드를 조회해서 처리. (DB에서 한 번에 여러 개 조회)
 * 커서 기법처럼 잡은 아이템 한 건씩 처리해서 레코드 처리 자체는 차이가 없음. (데이터베이스에서 가져오는 부분에서만 다름)
 */
@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class JdbcPagingReadBatchApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public JdbcPagingItemReader<Customer> customerItemReader(
            DataSource dataSource,
            PagingQueryProvider queryProvider,
            @Value("#{jobParameters['city']}") String city
    ) {
        Map<String, Object> parameterValues = new HashMap<>(1);
        parameterValues.put("city", city);

        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .parameterValues(parameterValues)
                //10개씩 조회
                .pageSize(10)
                .rowMapper(new CustomerRowMapper())
                .build();
    }

    /**
     * 사용 중인 데이터베이스를 자동으로 감지해 적정한 PagingQueryProvider를 반환.
     * DataSource를 사용해 작업 중인 DB의 타입을 결정. (setDatabaseType 으로 명시적으로 지정 가능)
     */
    @Bean
    public SqlPagingQueryProviderFactoryBean pagingQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

        factoryBean.setSelectClause("select *");
        factoryBean.setFromClause("from Customer");
        factoryBean.setWhereClause("where city = :city");
        //각 페이지의 쿼리를 실행할 때마다 동일한 레코드 정렬 순서를 보장하려면 order by 필요.
        factoryBean.setSortKey("id");
        factoryBean.setDataSource(dataSource);

        return factoryBean;
    }

    @Bean
    public ItemWriter<Object> itemWriter() {
        return items -> items.forEach(System.out::println);
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("jdbcPagingReadStep")
                .<Customer, Customer>chunk(100)
                .reader(customerItemReader(null, null, null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("jdbcPagingReadJob")
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public static void main(String[] args) {

        SpringApplication.run(JdbcPagingReadBatchApplication.class, "city=Chicago");
    }
}
