package jy.study.springBatch.itemReader;

import jy.study.springBatch.domain.Transaction;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.transform.FieldSet;

public class TransactionReader implements ItemStreamReader<Transaction> {

    private ItemStreamReader<FieldSet> fieldSetReader;
    private int recordCount = 0;
    private int expectedRecordCount = 0;

    private StepExecution stepExecution;

    public TransactionReader(ItemStreamReader<FieldSet> fieldSetReader) {
        this.fieldSetReader = fieldSetReader;
    }

    @Override
    public Transaction read() throws Exception {
        if (this.recordCount == 25) {
            throw new ParseException("this isn't what I hoped to happen");
        }

        //실제 읽기 작업을 위임
        return process(fieldSetReader.read());
    }

    /**
     * resources의 transactionFile.csv의 각 레코드를 처리
     */
    private Transaction process(FieldSet fieldSet) {
        Transaction result = null;

        if (fieldSet != null) {
            //데이터 레코드
            if (fieldSet.getFieldCount() > 1) {
                result = new Transaction();
                //계좌 번호
                result.setAccountNumber(fieldSet.readString(0));
                //타임 스탬프
                result.setTimestamp(fieldSet.readDate(1, "yyyy-MM-DD HH:mm:ss"));
                //금액(양수는 입금, 음수는 출금)
                result.setAmount(fieldSet.readDouble(2));

                recordCount++;
            //푸터 레코드
            } else {
                //총 레코드 개수
                expectedRecordCount = fieldSet.readInt(0);

                //StepExecution을 사용해서 중지.
                //잡이 STOPPED 상태를 반환하는 대신 스프링 배치가 JobInterruptedException을 던짐.
//                if (expectedRecordCount != this.recordCount) {
//                    this.stepExecution.setTerminateOnly();
//                }
            }
        }
        return result;
    }

//    @BeforeStep
//    public void beforeStep(StepExecution execution) {
//        this.stepExecution = execution;
//    }

    public void setFieldSetReader(ItemStreamReader<FieldSet> fieldSetReader) {
        this.fieldSetReader = fieldSetReader;
    }

//    @AfterStep
//    public ExitStatus afterStep(StepExecution execution) {
//        /*
//         * 읽어들인 레코드 수와 마지막 푸터 레코드에 기록된 수와 일치하는지 체크.
//         * 유효하지 않으면 잡을 중지.
//         */
//        if (recordCount == expectedRecordCount) {
//            return execution.getExitStatus();
//        } else {
//            return ExitStatus.STOPPED;
//        }
//    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        this.fieldSetReader.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        this.fieldSetReader.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        this.fieldSetReader.close();
    }
}
