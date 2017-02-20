package com.xz.scorep.executor.db;

/**
 * (description)
 * created at 2017/2/17
 *
 * @author yidin
 */
public class BatchExecutorException extends RuntimeException {

    public BatchExecutorException() {
    }

    public BatchExecutorException(String message) {
        super(message);
    }

    public BatchExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    public BatchExecutorException(Throwable cause) {
        super(cause);
    }
}
