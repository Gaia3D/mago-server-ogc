package com.gaia3d.mago.server.exception;

public class MemberUnauthorizedException extends RuntimeException {
    public MemberUnauthorizedException(String message) {
        super(message);
    }
}
