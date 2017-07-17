package com.xz.scorep.executor.exportaggrdata.exception;

/**
 * @author by fengye on 2017/7/17.
 */
public class CreatorException extends RuntimeException{
    public CreatorException() {
    }

    public CreatorException(String message) {
        super(message);
    }

    public CreatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreatorException(Throwable cause) {
        super(cause);
    }
}
