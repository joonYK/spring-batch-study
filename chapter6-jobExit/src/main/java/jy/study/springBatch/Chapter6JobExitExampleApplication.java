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
     */
    @Bean
    public Step importTransactionFileStep() {
        return this.stepBuilderFactory.get("importTransactionFileStep")
                //중지된 잡을 재실행할 수 있는 횟수 제한
                .startLimit(2)
                .<Transaction, Transaction>chunk(3)
                .reader(transactionReader())
                .writer(transactionWriter(null))
                //중지된 잡 재시작시에 스텝 실행을 성공했더라도 항상 이 스텝이 실행되도록 설정. ex) 유효성을 검증하거나 리소스를 정리하는 스텝
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
                .<AccountSummary, AccountSummary>chunk(3)
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
                .<AccountSummary, AccountSummary>chunk(3)
                .reader(accountSummaryReader(null))
                .writer(accountSummaryFileWriter(null))
                .build();
    }

    /**
     * 중지 트랜지션 예제 Job
     */
    //@Bean
    public Job stopTransitionJob() {
        return this.jobBuilderFactory.get("stopTransitionJob")
				.start(importTransactionFileStep())
				.on("STOPPED").stopAndRestart(importTransactionFileStep())
				.from(importTransactionFileStep()).on("*").to(applyTransactionsStep())
				.from(applyTransactionsStep()).next(generateAccountSummaryStep())
				.end()
				.build();
    }

    /**
     * StepExecution을 사용한 중지 예제 Job
     */
    //@Bean
    public Job stepExecutionExitJob() {
        return this.jobBuilderFactory.get("stepExecutionExitJob")
                .start(importTransactionFileStep())
                .next(applyTransactionsStep())
                .next(generateAccountSummaryStep())
                .build();
    }

    /**
     * 예외를 던져 실패상태로 잡 중지 예제 Job.
     * 스텝이 FAILED로 식별되면 스텝을 처음부터 다시 시작하지 않음.
     * 예외가 발생한 해당 청크는 롤백하고 재시작시에 그 청크부터 다시 시작.
     */
    //@Bean
    public Job failExitJob() {
        return this.jobBuilderFactory.get("failExitJob")
                .start(importTransactionFileStep())
                .next(applyTransactionsStep())
                .next(generateAccountSummaryStep())
                .build();
    }

    /**
     * 재시작 제어 예제 잡
     * 1. 중지된 잡의 재시작을 방지
     * 2. 재시작할 수 있는 횟수 제한
     */
    @Bean
    public Job restartControlJob() {
        return this.jobBuilderFactory.get("restartControlJob")
                //재시작을 할 수 없도록 한다.
                //.preventRestart()
                .start(importTransactionFileStep())
                .next(applyTransactionsStep())
                .next(generateAccountSummaryStep())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(Chapter6JobExitExampleApplication.class, args);
    }
}
