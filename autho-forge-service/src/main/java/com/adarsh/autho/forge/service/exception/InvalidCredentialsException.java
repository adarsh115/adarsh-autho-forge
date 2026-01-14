package com.adarsh.autho.forge.service.exception;

public class InvalidCredentialsException extends RuntimeException{
    private String message;

    public InvalidCredentialsException(String msg){
        super(msg);
        this.message = msg;
    }

}
