package jy.study.springBatch.itemProcessor;

import jy.study.springBatch.dao.TransactionDao;
import jy.study.springBatch.domain.AccountSummary;
import jy.study.springBatch.domain.Transaction;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class TransactionApplierProcessor implements ItemProcessor<AccountSummary, AccountSummary> {

    private final TransactionDao transactionDao;

    public TransactionApplierProcessor(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    /**
     * 전달받은 각 AccountSummary 레코드를 기반으로 transactionDao를 사용해 모든 거래 정보를 조회.
     * 거래 정보에 따라 계좌의 현재 잔액을 증가시키거나 감소시킴.
     */
    @Override
    public AccountSummary process(AccountSummary summary) throws Exception {
        List<Transaction> transactions = transactionDao.getTransactionsByAccountNumber(summary.getAccountNumber());

        for (Transaction transaction : transactions) {
            summary.setCurrentBalance(summary.getCurrentBalance() + transaction.getAmount());
        }

        return summary;
    }
}
