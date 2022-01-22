package jy.study.springBatch.dao;

import jy.study.springBatch.domain.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class TransactionDaoSupport extends JdbcTemplate implements TransactionDao {

    public TransactionDaoSupport(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 계좌번호와 연관된 모든 거래 레코드를 조회
     */
    @Override
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        return query(
                "select t.id, t.timestamp, t.amount " +
                    "from transaction t inner join account_summary a on " +
                    "a. id = t.account_summary_id " +
                    "where a.account_number = ?",
                new Object[] { accountNumber },
                (rs, rowNum) -> {
                    Transaction trans = new Transaction();
                    trans.setAmount(rs.getDouble("amount"));
                    trans.setTimestamp(rs.getDate("timestamp"));
                    return trans;
                }
        );
    }
}
