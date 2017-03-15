package com.xz.scorep.executor.report;

/**
 * (description)
 * created at 2017/3/15
 *
 * @author yidin
 */
public class ReportArchiveException extends RuntimeException {

    public ReportArchiveException() {
    }

    public ReportArchiveException(String message) {
        super(message);
    }

    public ReportArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportArchiveException(Throwable cause) {
        super(cause);
    }
}
