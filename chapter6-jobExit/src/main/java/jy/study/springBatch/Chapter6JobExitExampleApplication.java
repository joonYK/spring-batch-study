package jy.study.springBatch;

import jy.study.springBatch.dao.TransactionDao;
import jy.study.springBatch.dao.TransactionDaoSupport;
import jy.study.springBatch.domain.AccountSummary;
import jy.study.springBatch.domain.Transaction;
import jy.study.springBatch.itemProcessor.TransactionApplierProcessor;
import jy.study.springBatch.itemReader.TransactionReader;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class Chapter6JobExitExampleApplication {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    /**
     * TransactionReader Bean 생성
     */
    @Bean
    @StepScope
    public TransactionReader transactionReader() {
        return new TransactionReader(fileItemReader(null));
    }

    /**
     * transactionReader 에서 read를 위임해서 실제 레코드를 읽어들일 Reader
     */
    @Bean
    @StepScope
    public FlatFileItemReader<FieldSet> fileItemReader(
            @Value("#{jobParameters['transactionFile']}") Resource inputFile) {
        //CSV 파일을 읽을 수 있는 Reader
        return new FlatFileItemReaderBuilder<FieldSet>()
                .name("fileItemReader")
                .resource(inputFile)
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PassThroughFieldSetMapper())
                .build();
    }

    /**
     * 값을 데이터베이스에 저장하는 역학을 하는 JdbcBatchItemWriter.
     * transaction 테이블에 저장.
     */
    @Bean
    public JdbcBatchItemWriter<Transaction> transactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO TRANSACTION (ACCOUNT_SUMMARY_ID, TIMESTAMP, AMOUNT) " +
                     "VALUES (" +
                        "(SELECT ID FROM ACCOUNT_SUMMARY WHERE ACCOUNT_NUMBER = :accountNumber), " +
                        ":timestamp, " +
                        ":amount" +
                     ")")
                .dataSource(dataSource)
                .build();
    }

    /**
     * transactionFile.csv에서 읽은 데이터들을 DB에 옮기는 작업을 하는 스텝.
     * 거래 정보를 읽는 리더(transactionReader) 및 jdbc 라이터를 사용.
     * 잡이 재시작되면 이 스텝이 다시 실행될 수 있도록 구성(레코드 개수와 푸터 레코드가 일치하지 않으면 모든 정보를 초기화하고 다시 실행하길 원함)
     */
    @Bean
    public Step importTransactionFileStep() {
        return this.stepBuilderFactory.get("importTransactionFileStep")
                .<Transaction, Transaction>chunk(100)
                .reader(transactionReader())
                .writer(transactionWriter(null))
                .allowStartIfComplete(true)
                .listener(transactionReader())
                .build();
    }

    /**
     * DB로부터 AccountSummary 레코드를 읽음.
     */
    @Bean
    @StepScope
    public JdbcCursorItemReader<AccountSummary> accountSummaryReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<AccountSummary>()
                .name("accountSummaryReader")
                .dataSource(dataSource)
                .sql("SELECT ACCOUNT_NUMBER, CURRENT_BALANCE " +
                        "FROM ACCOUNT_SUMMARY A " +
                        "WHERE A.ID IN (" +
                        "	SELECT DISTINCT T.ACCOUNT_SUMMARY_ID " +
                        "	FROM TRANSACTION T) " +
                        "ORDER BY A.ACCOUNT_NUMBER")
                .rowMapper((resultSet, rowNumber) -> {
                    AccountSummary summary = new AccountSummary();

                    summary.setAccountNumber(resultSet.getString("account_number"));
                    summary.setCurrentBalance(resultSet.getDouble("current_balance"));

                    return summary;
                }).build();
    }

    /**
     * 거래 정보를 조회하는 dao
     */
    @Bean
    public TransactionDao transactionDao(DataSource dataSource) {
        return new TransactionDaoSupport(dataSource);
    }

    /**
     * 계좌에 거래 정보를 적용하는 커스텀 ItemProcessor
     */
    @Bean
    public TransactionApplierProcessor transactionApplierProcessor() {
        return new TransactionApplierProcessor(transactionDao(null));
    }

    /**
     * 갱신된 계좌 요약 레코드를 DB에 기록.
     */
    @Bean
    public JdbcBatchItemWriter<AccountSummary> accountSummaryWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<AccountSummary>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("UPDATE ACCOUNT_SUMMARY " +
                        "SET CURRENT_BALANCE = :currentBalance " +
                        "WHERE ACCOUNT_NUMBER = :accountNumber")
                .build();
    }

    /**
     * 거래 정보들을 가져와서 입금/출금 정보를 계좌에 적용하고, 계좌를 DB에 다시 저장하는 스텝.
     */
    @Bean
    public Step applyTransactionsStep() {
        return this.stepBuilderFactory.get("applyTransactionsStep")
                .<AccountSummary, AccountSummary>chunk(100)
                .reader(accountSummaryReader(null))
                .processor(transactionApplierProcessor())
                .writer(accountSummaryWriter(null))
                .build();
    }

    /**
     * 각 레코드의 계좌번호와 현재 잔액으로 CSV 파일을 생성.
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<AccountSummary> accountSummaryFileWriter(
            @Value("#{jobParameters['summaryFile']}") Resource summaryFile) {

        DelimitedLineAggregator<AccountSummary> lineAggregator = new DelimitedLineAggregator<>();
        BeanWrapperFieldExtractor<AccountSummary> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"accountNumber", "currentBalance"});
        fieldExtractor.afterPropertiesSet();
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<AccountSummary>()
                .name("accountSummaryFileWriter")
                .resource(summaryFile)
                .lineAggregator(lineAggregator)
                .build();
    }

    @Bean
    public Step generateAccountSummaryStep() {
        return this.stepBuilderFactory.get("generateAccountSummaryStep")
                .<AccountSummary, AccountSummary>chunk(100)
                .reader(accountSummaryReader(null))
                .writer(accountSummaryFileWriter(null))
                .build();
    }

    @Bean
    public Job transactionJob() {
        return this.jobBuilderFactory.get("transactionJob2")
                .start(importTransactionFileStep())
                .next(applyTransactionsStep())
                .next(generateAccountSummaryStep())
                .build();

//        return this.jobBuilderFactory.get("transactionJob")
//				.start(importTransactionFileStep())
//				.on("STOPPED").stopAndRestart(importTransactionFileStep())
//				.from(importTransactionFileStep()).on("*").to(applyTransactionsStep())
//				.from(applyTransactionsStep()).next(generateAccountSummaryStep())
//				.end()
//				.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(Chapter6JobExitExampleApplication.class, args);
    }
}
