package com.projects.cinephiles.exceptions;

public class UserAlreadyExistsException extends Throwable {
    public UserAlreadyExistsException(String usernameAlreadyExists) {
        System.out.println("User already exists");
    }
}
