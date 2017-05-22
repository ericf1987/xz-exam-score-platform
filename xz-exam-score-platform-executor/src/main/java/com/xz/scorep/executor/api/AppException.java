package com.xz.scorep.executor.api;

/**
 * 业务异常
 *
 * @author zhaorenwu
 */
public class AppException extends RuntimeException {

    public AppException() {
    }

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppException(Throwable cause) {
        super(cause);
    }
}
