package com.xz.scorep.executor.exportexcel;

/**
 * (description)
 * created at 2017/2/15
 *
 * @author yidin
 */
public class ExcelReportException extends RuntimeException {

    public ExcelReportException() {
    }

    public ExcelReportException(String message) {
        super(message);
    }

    public ExcelReportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelReportException(Throwable cause) {
        super(cause);
    }
}
