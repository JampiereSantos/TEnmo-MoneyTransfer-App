package com.techelevator.tenmo;

import java.math.BigDecimal;

import com.techelevator.tenmo.models.Account;
import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transaction;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AccountServiceException;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.UserServiceException;
import com.techelevator.view.ConsoleService;

public class App {

	private static final String API_BASE_URL = "http://localhost:8080/";

	private static final String MENU_OPTION_EXIT = "Exit";
	private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN,
			MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS,
			MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS,
			MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };

	private AuthenticatedUser currentUser;
	private ConsoleService console;
	private AuthenticationService authenticationService;
	private AccountService accountService;

	public static void main(String[] args) {
		App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL),
				new AccountService(API_BASE_URL));
		app.run();
	}

	public App(ConsoleService console, AuthenticationService authenticationService, AccountService accountService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.accountService = accountService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");

		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while (true) {
			String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {
		try {
			// Get and print the user's balance formated in $1,000.00 form
			Account userAccount = accountService.getAccountByUserID(currentUser.getUser().getId());
			System.out.printf("Your current balance: $%,.2f\n", userAccount.getBalance().floatValue());
		} catch (AccountServiceException e) {

			System.out.println(e.getMessage());
		}

	}

	private void viewTransferHistory() {

		//get a list of all of all transactions
		Transaction[] listOfTransactions = listTransactions("Approved");
		Transaction selectedTransaction = null;
		long id = -1;
		while (selectedTransaction == null) {
			String input = console.getUserInput("Please enter transfer ID to view details (0 to cancel)");
			if(input.length()>0)
				id = Long.valueOf(input);
			if (id == 0) {
				System.out.println("\nTransaction Cancelled");
				return;
			}

			for (Transaction transaction : listOfTransactions) {
				if (transaction.getId() == id && transaction.getTransferStatus().equalsIgnoreCase("Approved")) {
					selectedTransaction = transaction;
					break;
				}
			}
			if (selectedTransaction == null) {
				System.out.println("\nInvalid Transfer ID please try again!\n");
			}
		}
		System.out.println("\n--------------------");
		System.out.println("Transfer Details");
		System.out.println("--------------------\n");
		System.out.println("Id: " + selectedTransaction.getId());
		System.out.println("From: " + selectedTransaction.getFromUser().getUsername());
		System.out.println("To: " + selectedTransaction.getToUser().getUsername());
		System.out.println("Type: " + selectedTransaction.getTransferType());
		System.out.println("Status: " + selectedTransaction.getTransferStatus());
		System.out.printf("Amount: $%,.2f\n", selectedTransaction.getAmount().floatValue());
		System.out.println("\n--------------------");
	}

	private Transaction[] listTransactions(String type) {
		try {

			System.out.println("----------------------------------");
			System.out.println("Transfers");
			System.out.println("ID\tFrom/To\t\tAmount");
			System.out.println("----------------------------------");
			Transaction[] listOfTransactions = accountService.listOfTransaction(currentUser.getUser().getId());

			for (int i = 0; i < listOfTransactions.length; i++) {
				if (listOfTransactions[i].getTransferStatus().equals(type)) {
					String fromTo;

					if (currentUser.getUser().getId().equals(listOfTransactions[i].getFromUser().getId())) {

						fromTo = "To   " + listOfTransactions[i].getToUser().getUsername();
					} else {
						fromTo = "From " + listOfTransactions[i].getFromUser().getUsername();
					}

					System.out.printf(listOfTransactions[i].getId() + "\t" + fromTo + "\t$%,.2f\n",
							listOfTransactions[i].getAmount().floatValue());
				}
			}
			System.out.println("----------------------------------\n");
			return listOfTransactions;
		} catch (UserServiceException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	private Transaction[] listPending() {
		try {

			System.out.println("----------------------------------");
			System.out.println("Pending Transfers");
			System.out.println("ID\tTo\tAmount");
			System.out.println("----------------------------------");
			Transaction[] listOfTransactions = accountService.listOfTransaction(currentUser.getUser().getId());

			for (int i = 0; i < listOfTransactions.length; i++) {
				if (listOfTransactions[i].getTransferStatus().equals("Pending")) {
					String userTo;

					if (currentUser.getUser().getId().equals(listOfTransactions[i].getFromUser().getId())) {
						userTo = listOfTransactions[i].getToUser().getUsername();			
					} else {
						break;						
					}

					System.out.printf(listOfTransactions[i].getId() + "\t" + userTo + "\t$%,.2f\n",
							listOfTransactions[i].getAmount().floatValue());
				}
			}
			System.out.println("----------------------------------\n");
			return listOfTransactions;
		} catch (UserServiceException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	private void viewPendingRequests() {
		Transaction[] listOfTransactions = listPending();
		long id = -1;
		Transaction selectedTransaction = null;
		while (selectedTransaction == null) {
			String input = console.getUserInput("Please enter transfer ID to approve/reject (0 to cancel)");
			if(input.length()>0)
				id = Long.valueOf(input);

			
			if (id == 0) {
				System.out.println("\nTransaction Cancelled");
				return;
			}

			for (Transaction transaction : listOfTransactions) {
				if (transaction.getId() == id && transaction.getTransferStatus().equalsIgnoreCase("Pending")) {
					selectedTransaction = transaction;
					break;
				}
			}
			if (selectedTransaction == null) {
				System.out.println("\nInvalid Transfer ID please tye again!\n");
			}
		}
		int status = -1;		
		while(status < 0 || status > 2) {
			System.out.println("\n1: Approve\n2: Reject\n0: Don't Approve or Reject");
			status = console.getUserInputInteger("Please choose an option");
			if(status < 0 || status > 2) {
				System.out.println("\nPlease choose a valid option!\n");
			}
		}
		if(status == 0)
			return;
		try {	
			if(accountService.completeTransaction(selectedTransaction.getId(), status+1)) {
				System.out.println((status==1)?"\nTransfer Approved":"\nTransfer Rejected");
			}else {
				System.out.println("\nInsuffient Funds");
			}
		} catch (AccountServiceException e) {
			System.out.println(e.getMessage());
		}

	}

	private void sendBucks() {

		// List the Users and Store that list
		User[] listOfUser = listUsers(); 
		// get to destination account
		User toUser = null;
		long id = -1;
		//Keep asked for the destination if we don't have a valid one
		while (toUser == null) {
			String input = console.getUserInput("What user do you want to send money to (0 to cancel)");
			//as long as the input is longer than zero
			if(input.length()>0) {
				id = Long.valueOf(input);
			}
			//if 0 cancel transaction
			if (id == 0) {
				System.out.println("\nTransaction Cancelled");
				return;
			}
			//check if the user is not you and valid
			for (User user : listOfUser) {
				if (user.getId().longValue() == id && !user.getId().equals(currentUser.getUser().getId())) {
					toUser = user;
				}
			}
			//if no valid user is found ask again
			if (toUser == null) {
				System.out.println("\nPlease select a valid user!\n");
			}
		}
		// get the amount of the transaction
		BigDecimal moneyAmount = null;
		//Keep asking for an amount until a positive amount is given
		while (moneyAmount == null) {
			String input = console.getUserInput("Enter amount: (0 to cancel)");
			//if the input is not empty convert to big decimal and validate
			if(input.length()>0) {
				moneyAmount = new BigDecimal(input);
				
				if (moneyAmount.compareTo(new BigDecimal(0)) == 0) {
					System.out.println("\nTransaction Cancelled");
					return;
				}
				if (moneyAmount.compareTo(new BigDecimal(0)) < 0) {
					System.out.println("\nPlease input a positive amount!\n");
					moneyAmount = null;
				}
			}

			
		}

		// Create the transaction sending the input amount of money from the user's
		// account to the account they selected
		Transaction transaction = new Transaction();
		transaction.setTo(id);
		transaction.setFrom(currentUser.getUser().getId());
		transaction.setAmount(moneyAmount);
		transaction.setToUser(toUser);
		transaction.setFromUser(currentUser.getUser());

		try {
			// start the transaction on the server passing through the details.
			if (accountService.sendMoneyTo(transaction)) {
				System.out.println("\nTransaction processed succesfully");
			} else {
				System.out.println("\nInsufficient funds");
			}
		} catch (AccountServiceException e) {
			System.out.println(e.getMessage());
		}

	}

	private User[] listUsers() {
		try {
			//Grab all the users from the server
			User[] listOfUser = accountService.listOfUsers();
			System.out.println("--------------------");
			System.out.println("Users");
			System.out.println("ID\tName");
			System.out.println("--------------------");
			//print out every user except for you
			for (int i = 0; i < listOfUser.length; i++) {
				if (!listOfUser[i].getId().equals(currentUser.getUser().getId())) {
					System.out.println(listOfUser[i].getId() + "\t" + listOfUser[i].getUsername());
				}
			}
			System.out.println("--------------------\n");
			return listOfUser;
		} catch (UserServiceException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	private void requestBucks() {
		// List the Users and Store that list
		User[] listOfUser = listUsers();
		// get to destination account
		User toUser = null;
		
		long id = -1;
		//Request user to select a valid user
		while (toUser == null) {
			String input = console.getUserInput("What user do you want to request money from (0 to cancel)");
			if(input.length()>0)
				id = Long.valueOf(input);
			//cancel if user 0
			if (id == 0) {
				System.out.println("\nRequest Cancelled");
				return;
			}
			//Make use the user is not us and real
			for (User user : listOfUser) {
				if (user.getId().longValue() == id && !user.getId().equals(currentUser.getUser().getId())) {
					toUser = user;
				}
			}
			if (toUser == null) {
				System.out.println("\nPlease select a valid user!\n");
			}
		}
		// get the amount of the transaction
		BigDecimal moneyAmount = null;
		while (moneyAmount == null) {
			
			String input = console.getUserInput("Enter Amount (0 to cancel)");
			if(input.length()>0) {
				moneyAmount = new BigDecimal(input);
				if (moneyAmount.compareTo(new BigDecimal(0)) == 0) {
					System.out.println("\nTransaction Cancelled");
					return;
				}
				if (moneyAmount.compareTo(new BigDecimal(0)) < 0) {
					System.out.println("Please input a positive amount!");
					moneyAmount = null;
				}
			}

			
		}

		// Create the transaction sending the input amount of money from the user's
		// account to the account they selected
		Transaction transaction = new Transaction();
		transaction.setFrom(id);
		transaction.setTo(currentUser.getUser().getId());
		transaction.setAmount(moneyAmount);
		transaction.setFromUser(toUser);
		transaction.setToUser(currentUser.getUser());

		try {
			// start the transaction on the server passing through the details.
			if (accountService.requestMoneyFrom(transaction)) {
				System.out.println("\nRequest sent succesfully.");
			} else {
				System.out.println("\nRequest failed to send.");
			}
		} catch (AccountServiceException e) {
			System.out.println(e.getMessage());
		}

	}

	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while (!isAuthenticated()) {
			String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
		while (!isRegistered) // will keep looping until user is registered
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				authenticationService.register(credentials);
				isRegistered = true;
				System.out.println("\nRegistration successful. You can now login.");
			} catch (AuthenticationServiceException e) {
				System.out.println("REGISTRATION ERROR: " + e.getMessage());
				System.out.println("Please attempt to register again.");
			}
		}
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) // will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: " + e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
		AccountService.AUTH_TOKEN = currentUser.getToken();
	}

	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
}
