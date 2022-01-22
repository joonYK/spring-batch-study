package jy.study.springBatch.dao;

import jy.study.springBatch.domain.Transaction;

import java.util.List;

public interface TransactionDao {

    List<Transaction> getTransactionsByAccountNumber(String accountNumber);
}
