package com.techelevator.tenmo.models;

import java.math.BigDecimal;

/**
 * The main vehicle to send transaction data, can be reused to send money, send requests and so on.
 * @author Shawn
 *
 */
public class Transaction {
	
	private long to;	
	private long from;
	
	private User toUser;
	private User fromUser;
	
	private BigDecimal amount;
	
	private long id;
	
	private String transferType;//Types Request, Send
	private String transferStatus;//Statuses Approved, Pending, Rejected
	
	
	
	public long getTo() {
		return to;
	}
	public void setTo(long to) {
		this.to = to;
	}
	public long getFrom() {
		return from;
	}
	public void setFrom(long from) {
		this.from = from;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTransferType() {
		return transferType;
	}
	public void setTransferType(String transferType) {
		this.transferType = transferType;
	}
	public String getTransferStatus() {
		return transferStatus;
	}
	public void setTransferStatus(String transferStatus) {
		this.transferStatus = transferStatus;
	}
	public User getFromUser() {
		return fromUser;
	}
	public void setFromUser(User fromUser) {
		this.fromUser = fromUser;
	}
	public User getToUser() {
		return toUser;
	}
	public void setToUser(User toUser) {
		this.toUser = toUser;
	}
}
