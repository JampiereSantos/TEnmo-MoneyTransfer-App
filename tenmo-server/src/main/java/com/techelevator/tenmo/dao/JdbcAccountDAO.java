package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;

@Component
public class JdbcAccountDAO implements AccountDAO {
	private JdbcTemplate jdbcTemplate;

	public JdbcAccountDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Account findAccountByUserID(Long id) {
		Account tempAccount = null;
		String sqlFindAccountID = "SELECT * FROM accounts WHERE user_id = ?";
		SqlRowSet accountRow = jdbcTemplate.queryForRowSet(sqlFindAccountID, id);
		if (accountRow.next()) {
			tempAccount = mapRowToAccount(accountRow);
		}
		return tempAccount;
	}
	@Override
	public Account findAccountByID(Long id) {
		Account tempAccount = null;
		String sqlFindAccountID = "SELECT * FROM accounts WHERE account_id = ?";
		SqlRowSet accountRow = jdbcTemplate.queryForRowSet(sqlFindAccountID, id);
		if (accountRow.next()) {
			tempAccount = mapRowToAccount(accountRow);
		}
		return tempAccount;
	}
	@Override
	public boolean sendMoney(Transaction transaction) {
		// Find the to and from accounts
		Account to = findAccountByUserID(transaction.getTo());
		Account from = findAccountByUserID(transaction.getFrom());

		// check that user has enough money to send
		if (from.getBalance().doubleValue() > transaction.getAmount().doubleValue()) {
			// adds money to the recieving use and subtracts it from the sending user.
			to.setBalance(to.getBalance().add(transaction.getAmount()));
			from.setBalance(from.getBalance().subtract(transaction.getAmount()));
			// updates both balances
			updateBalance(to);
			updateBalance(from);
			// creates the transfer log
			createTransaction(to.getAccountID(), from.getAccountID(), transaction.getAmount(), 2, 2);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean requestMoney(Transaction transaction) {
		//Figure out what accounts we are trying do a transaction between from their user id
		Account to = findAccountByUserID(transaction.getTo());
		Account from = findAccountByUserID(transaction.getFrom());

		//create transfer log
		return createTransaction(to.getAccountID(), from.getAccountID(), transaction.getAmount(), 1, 1);

	}

	@Override
	public boolean updateBalance(Account account) {
		// Updates the account
		String sqlFindAccountID = "UPDATE accounts SET balance = ? WHERE user_id = ?";
		try {
			jdbcTemplate.update(sqlFindAccountID, account.getBalance(), account.getUserID());
			return true;
		} catch (DataAccessException ex) {
			return false;
		}

	}

	@Override
	public boolean createTransaction(long to, long from, BigDecimal balance, int type, int status) {
		// Creates transfer log
		String sqlFindAccountID = "INSERT INTO transfers (transfer_type_id,transfer_status_id,account_from,account_to,amount) VALUES (?,?,?,?,?)";
		try {
			jdbcTemplate.update(sqlFindAccountID, type, status, from, to, balance);
			return true;
		} catch (DataAccessException ex) {
			ex.printStackTrace();
			return false;
		}

	}

	@Override
	public List<Transaction> listTransfers(long id) {
		List<Transaction> tempTransaction = new ArrayList<>();
		//Joins borderline every table together in order to get the transaction with the String form of the status and type
		String sqlFindAccountID = "SELECT t.transfer_id, tt.transfer_type_desc, ts.transfer_status_desc, t.account_from, t.account_to, t.amount FROM users u "
				+ "INNER JOIN accounts a " + "ON  u.user_id = a.user_id " + "INNER JOIN transfers t "
				+ "ON t.account_from = a.account_id OR t.account_to = a.account_id " + "INNER JOIN transfer_types tt "
				+ "ON t.transfer_type_id = tt.transfer_type_id " + "INNER JOIN transfer_statuses ts "
				+ "ON t.transfer_status_id = ts.transfer_status_id " + "WHERE u.user_id = ?";
		SqlRowSet accountRow = jdbcTemplate.queryForRowSet(sqlFindAccountID, id);
		while (accountRow.next()) {
			tempTransaction.add(mapRowToTransactionTyped(accountRow));
		}
		return tempTransaction;
	}
	/**
	 * Creates a Transaction with String Type and Status
	 * @param row
	 * @return
	 */
	public Transaction mapRowToTransactionTyped(SqlRowSet row) {
		Transaction tempTransaction= mapRowToTransaction(row);
		tempTransaction.setTransferType(row.getString("transfer_type_desc"));
		tempTransaction.setTransferStatus(row.getString("transfer_status_desc"));
		return tempTransaction;
	}
	/**
	 * Creates a Transaction
	 * @param row
	 * @return
	 */
	public Transaction mapRowToTransaction(SqlRowSet row) {
		Transaction tempTransaction = new Transaction();
		tempTransaction.setFrom(row.getLong("account_from"));
		tempTransaction.setTo(row.getLong("account_to"));
		tempTransaction.setAmount(row.getBigDecimal("amount"));
		tempTransaction.setId(row.getLong("transfer_id"));
		tempTransaction.setToUser(findUserByAccountId(tempTransaction.getTo()));
		tempTransaction.setFromUser(findUserByAccountId(tempTransaction.getFrom()));

		return tempTransaction;
	}

	/**
	 * creates an Account
	 * @param row
	 * @return
	 */
	public Account mapRowToAccount(SqlRowSet row) {
		Account tempAccount = new Account();
		tempAccount.setBalance(row.getBigDecimal("balance"));
		tempAccount.setUserID(row.getLong("user_id"));
		tempAccount.setAccountID(row.getLong("account_id"));
		return tempAccount;
	}
	/**
	 * Creates a User object without password hash or role information
	 * @param row
	 * @return
	 */
	private User mapRowToUserPassless(SqlRowSet row) {
		User user = new User();
		user.setId(row.getLong("user_id"));
		user.setUsername(row.getString("username"));
		return user;
	}

	@Override
	public User findUserByAccountId(long id) {
		User tempUser = null;
		String sqlFindUserID = "SELECT users.user_id, users.username FROM users "
				+ "JOIN accounts ON users.user_id = accounts.user_id WHERE account_id = ?";
		SqlRowSet accountRow = jdbcTemplate.queryForRowSet(sqlFindUserID, id);
		if (accountRow.next()) {
			tempUser = mapRowToUserPassless(accountRow);
		}
		return tempUser;
	}
	public Transaction getTransactionByID(long id) {
		Transaction tempTransaction = null;
		String sqlFindTransactionID = "SELECT * FROM transfers WHERE transfer_id = ?";
		SqlRowSet accountRow = jdbcTemplate.queryForRowSet(sqlFindTransactionID, id);
		if (accountRow.next()) {
			tempTransaction = mapRowToTransaction(accountRow);
		}
		return tempTransaction;
	}
	
	@Override
	public boolean completeTransaction(long id, int statusType) {
		//if the status is accepted
		if (statusType == 2) {
			//get the transaction from the db
			Transaction tempTransaction = getTransactionByID(id);
			//grab the associated accounts
			Account to = findAccountByID(tempTransaction.getTo());
			Account from = findAccountByID(tempTransaction.getFrom());
			//if the user has enough money to send
			if (from.getBalance().doubleValue() > tempTransaction.getAmount().doubleValue()) {
				//send the money
				to.setBalance(to.getBalance().add(tempTransaction.getAmount()));
				from.setBalance(from.getBalance().subtract(tempTransaction.getAmount()));
				//update balances
				updateBalance(to);
				updateBalance(from);
				//change transfer to complete
				String transferMoneyTo = "UPDATE transfers " + "SET transfer_status_id = ? " + "WHERE transfer_id = ? ";
				jdbcTemplate.update(transferMoneyTo, statusType,id );
				
				return true;
			}
			//if the user doesn't have enough money
			return false;
		} 	else {
			//change transfer to status type
			String transferMoneyTo = "UPDATE transfers " + "SET transfer_status_id = ? " + "WHERE transfer_id = ? ";
			jdbcTemplate.update(transferMoneyTo, statusType, id);
			
			return true;
		}

	}

}
