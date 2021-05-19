package com.techelevator.tenmo.models;

import java.math.BigDecimal;

public class Account {
	private BigDecimal balance;
	private long userID;
	private long accountID;
	public BigDecimal getBalance() {
		return balance;
	
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public long getAccountID() {
		return accountID;
	}

	public void setAccountID(long accountID) {
		this.accountID = accountID;
	}
}
