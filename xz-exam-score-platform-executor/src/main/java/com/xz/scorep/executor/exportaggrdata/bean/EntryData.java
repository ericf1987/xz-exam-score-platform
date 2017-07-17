package com.xz.scorep.executor.exportaggrdata.bean;

/**
 * @author by fengye on 2017/7/17.
 */
public class EntryData {
    private String name;
    private byte[] content;

    public EntryData() {
    }

    public EntryData(String name) {
        this.name = name;
    }

    public EntryData(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
