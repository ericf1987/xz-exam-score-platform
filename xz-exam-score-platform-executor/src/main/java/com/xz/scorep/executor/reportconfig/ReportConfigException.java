package com.xz.scorep.executor.reportconfig;

/**
 * (description)
 * created at 2017/2/9
 *
 * @author yidin
 */
public class ReportConfigException extends RuntimeException {

    public ReportConfigException() {
    }

    public ReportConfigException(String message) {
        super(message);
    }

    public ReportConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportConfigException(Throwable cause) {
        super(cause);
    }
}
