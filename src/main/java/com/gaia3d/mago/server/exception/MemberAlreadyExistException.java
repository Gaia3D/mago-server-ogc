package com.gaia3d.mago.server.exception;

public class MemberAlreadyExistException extends RuntimeException {
    public MemberAlreadyExistException(String message) {
        super(message);
    }
}
