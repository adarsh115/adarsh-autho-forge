package com.adarsh.autho.forge.service.exception;

public class UserNameAlreadyExistsException extends RuntimeException{
    private String message;

    public UserNameAlreadyExistsException(String msg){
        super(msg);
        this.message = msg;
    }

}
