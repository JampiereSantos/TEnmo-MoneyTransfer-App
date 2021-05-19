package com.techelevator.tenmo.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class Account {
	@NotNull
	private BigDecimal balance;
	@Positive
	private long userID;
	@Positive
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
