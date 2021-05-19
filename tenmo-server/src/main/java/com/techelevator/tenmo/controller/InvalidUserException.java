package com.techelevator.tenmo.controller;

public class InvalidUserException extends Exception {
	public InvalidUserException(String message) {
		super(message);
	}
}
