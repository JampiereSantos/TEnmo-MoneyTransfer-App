package com.techelevator.tenmo.controller;

import java.security.Principal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;

@RestController
@PreAuthorize("isAuthenticated()")
public class AccountController {
	private UserDAO userdao;
	private AccountDAO accountdao;
	
	public AccountController(UserDAO userdao, AccountDAO accountdao) {
		this.userdao = userdao;
		this.accountdao = accountdao;
		
	}
	@RequestMapping(value = "user/{id}/account",method = RequestMethod.GET)
	public Account getAccountById(@PathVariable long id, Principal principal) throws InvalidUserException {
		if(userdao.findByUsername(principal.getName()).getId().equals(id)) {
			return accountdao.findAccountByUserID(id);
		}else {
			throw new InvalidUserException("You are not the owner of the account");
		}
	}
	@RequestMapping(value = "user/",method = RequestMethod.GET)
	public List<User> listUser() {
		return userdao.findAll();
	}
	@RequestMapping(value = "/send",method = RequestMethod.POST)
	public boolean SendMoney (@Valid @RequestBody Transaction transaction, Principal principal) throws InvalidUserException {	
		if(transaction.getFromUser().getUsername().equals(principal.getName())) {
			return accountdao.sendMoney(transaction);
		}else {
			throw new InvalidUserException("You are not the owner of the sending account");
		}
	}
		
	@RequestMapping(value = "/user/{id}/account/transfers", method = RequestMethod.GET)
	public List<Transaction> listTransfers(@PathVariable long id, Principal principal) throws InvalidUserException {
		if(userdao.findByUsername(principal.getName()).getId().equals(id)) {
			return accountdao.listTransfers(id);
			
		}else {
			throw new InvalidUserException("You are not the owner of the account");
		}
	}
	
	@RequestMapping(value = "/request", method = RequestMethod.POST)
	public boolean RequestMoney (@Valid @RequestBody Transaction transaction, Principal principal) throws InvalidUserException {	
		if(transaction.getToUser().getUsername().equals(principal.getName())) {
			return accountdao.requestMoney(transaction);
		}else {
			throw new InvalidUserException("You are not the owner of the receiving account");
		}
	}
	@RequestMapping(value = "transfers/{id}/{status}", method = RequestMethod.POST)
	public boolean listTransfers(@PathVariable long id, @PathVariable int status, Principal principal) throws InvalidUserException {
		if(userdao.findByUsername(principal.getName()).getId().equals(accountdao.getTransactionByID(id).getFromUser().getId())) {
			return accountdao.completeTransaction(id, status);
		}else {
			throw new InvalidUserException("You are not the owner of the account");
		}
	}

}