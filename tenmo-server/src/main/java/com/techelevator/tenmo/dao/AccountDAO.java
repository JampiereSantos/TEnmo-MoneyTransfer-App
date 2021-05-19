package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;

public interface AccountDAO {
	
	public Account findAccountByUserID(Long id);
	public Account findAccountByID(Long id);
	public boolean sendMoney(Transaction transaction);
	public boolean updateBalance(Account account);
	public boolean createTransaction(long to, long from, BigDecimal balance,int type,int status);
	public List<Transaction> listTransfers(long id);
	User findUserByAccountId(long id);
	public boolean requestMoney(@Valid Transaction transaction);
	public boolean completeTransaction(long id, int statusType);
	public Transaction getTransactionByID(long id);
}
