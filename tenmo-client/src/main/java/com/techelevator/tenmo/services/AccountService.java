package com.techelevator.tenmo.services;

import org.springframework.http.HttpMethod;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.models.Account;
import com.techelevator.tenmo.models.Transaction;
import com.techelevator.tenmo.models.User;

public class AccountService {
	private String baseUrl;
	private RestTemplate restTemplate = new RestTemplate();
	public static String AUTH_TOKEN = "";

	public AccountService(String url) {
		this.baseUrl = url;
	}
	/**
	 *  Gets an Account from the server by user id
	 * @param userID
	 * @return
	 * @throws AccountServiceException
	 */
	public Account getAccountByUserID(int userID) throws AccountServiceException {
		Account account = null;
		try {
			account = restTemplate
					.exchange(baseUrl + "user/" + userID + "/account/", HttpMethod.GET, makeAuthEntity(), Account.class)
					.getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return account;

	}

	/**
	 * Lists all users (Including the current user)
	 * @return
	 * @throws UserServiceException
	 */
	public User[] listOfUsers() throws UserServiceException {
		User[] moneyTransfer = null;
		try {
			moneyTransfer = restTemplate.exchange(baseUrl + "user/", HttpMethod.GET, makeAuthEntity(), User[].class)
					.getBody();
		} catch (RestClientResponseException ex) {
			throw new UserServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return moneyTransfer;
	}
	/**
	 * Lists all transactions with user friendly String type and status names
	 * @param userID
	 * @return
	 * @throws UserServiceException
	 */
	public Transaction[] listOfTransaction(int userID) throws UserServiceException {
		Transaction[] transactionList = null;
		try {
			transactionList = restTemplate.exchange(baseUrl +"/user/" + userID +"/account/transfers", HttpMethod.GET, makeAuthEntity(), Transaction[].class).getBody();
		} catch (RestClientResponseException ex) {
			throw new UserServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return transactionList;
	}
	
	
	

	
	/**
	 * Sends a Transaction to the Server to send money
	 * @param transaction
	 * @return
	 * @throws AccountServiceException
	 */
	public boolean sendMoneyTo(Transaction transaction) throws AccountServiceException {
		boolean success = false;
		try {
			//POSTS an Authenticated HttpEntity with a Transaction attached to the body
			success = restTemplate
					.exchange(baseUrl + "send", HttpMethod.POST, makeAuthTransaction(transaction), Boolean.class).getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return success;
	}
	
	/**
	 * Closes out a request for money
	 * @param id
	 * @param status
	 * @return
	 * @throws AccountServiceException
	 */
	public boolean completeTransaction(long id, int status) throws AccountServiceException {
		boolean success = false;
		try {
			//POSTS an Authenticated HttpEntity with a Transaction attached to the body
			success = restTemplate
					.exchange(baseUrl + "transfers/"+id+"/"+status, HttpMethod.POST, makeAuthEntity(), Boolean.class).getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return success;
	}
	/**
	 * Creates an Authenticated HttpEntity with a transaction attached
	 * @param transaction
	 * @return HttpEntity Transaction
	 */
	public HttpEntity<Transaction> makeAuthTransaction(Transaction transaction) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(AUTH_TOKEN);
		HttpEntity<Transaction> entity = new HttpEntity<>(transaction, headers);
		return entity;
	}
	
	
	@SuppressWarnings("rawtypes")
	/**
	 * Creates an Authenticated HttpEntity
	 * @return HttpEntity bodyless
	 */
	private HttpEntity makeAuthEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(AUTH_TOKEN);
		HttpEntity entity = new HttpEntity<>(headers);
		return entity;
	}
	public boolean requestMoneyFrom(Transaction transaction) throws AccountServiceException{
		boolean success = false;
		try {
			//POSTS an Authenticated HttpEntity with a Transaction attached to the body
			success = restTemplate
					.exchange(baseUrl + "request", HttpMethod.POST, makeAuthTransaction(transaction), Boolean.class).getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return success;
	}
	

}
