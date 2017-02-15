package com.xz.scorep.executor.table;

/**
 * (description)
 * created at 2017/2/15
 *
 * @author yidin
 */
public class TableCell {

    private Object value;

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
