package jy.study.springBatch.itemProcessor;

import jy.study.springBatch.domain.Account;
import jy.study.springBatch.domain.Statement;
import jy.study.springBatch.domain.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountItemProcessor implements ItemProcessor<Statement, Statement> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Statement process(Statement item) throws Exception {
        String sql =
                "SELECT " +
                    "a.account_id," +
                    "a.balance," +
                    "a.last_statement_date," +
                    "t.transaction_id," +
                    "t.description," +
                    "t.credit," +
                    "t.debit," +
                    "t.timestamp " +
                "FROM account a left join " +
                        "transaction t on a.account_id = t.account_account_id " +
                "WHERE " +
                    "a.account_id in " +
                    "(SELECT account_account_id " +
                    "FROM customer_account " +
                    "WHERE customer_customer_id = ?) " +
                "ORDER BY t.timestamp";

        item.setAccounts(
                this.jdbcTemplate.query(
                        sql,
                        new Object[] {item.getCustomer().getId()},
                        new AccountResultSetExtractor()));

        return item;
    }

    public static class AccountResultSetExtractor implements ResultSetExtractor<List<Account>> {

        private final List<Account> accounts = new ArrayList<>();
        private Account curAccount;

        @Nullable
        @Override
        public List<Account> extractData(ResultSet rs) throws SQLException, DataAccessException {
            while (rs.next()) {
                if (curAccount == null) {
                    curAccount = extractAccount(rs);
                } else if(rs.getLong("account_id") != curAccount.getId()) {
                    curAccount = extractAccount(rs);
                }

                if (StringUtils.hasText(rs.getString("description"))) {
                    curAccount.addTransaction(
                        Transaction.builder()
                            .transactionId(rs.getLong("transaction_id"))
                            .accountId(rs.getLong("account_id"))
                            .description(rs.getString("description"))
                            .credit(rs.getBigDecimal("credit"))
                            .debit(rs.getBigDecimal("debit"))
                            .timestamp(new Date(rs.getTimestamp("timestamp").getTime()))
                            .build());
                }

                if (curAccount != null)
                    accounts.add(curAccount);
            }

            return accounts;
        }

        private Account extractAccount(ResultSet rs) throws SQLException {
            return new Account(
                    rs.getLong("account_id"),
                    rs.getBigDecimal("balance"),
                    rs.getDate("last_statement_date"));
        }
    }
}
