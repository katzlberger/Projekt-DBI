package ht.leonding.dbi.error;

public abstract sealed class DomainException extends RuntimeException
        permits NotFoundException, DuplicateException, InvalidRequestException {

    protected DomainException(String message) {
        super(message);
    }
}
