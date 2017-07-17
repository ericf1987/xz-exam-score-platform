package com.xz.scorep.executor.exportaggrdata.packcreator;

import com.alibaba.fastjson.JSON;
import com.xz.scorep.executor.exportaggrdata.bean.EntryData;
import com.xz.scorep.executor.exportaggrdata.context.CreatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

/**
 * @author by fengye on 2017/7/17.
 */
public interface ScoreDataEntryCreator {
    Logger LOG = LoggerFactory.getLogger(ScoreDataEntryCreator.class);

    List<EntryData> createEntries(CreatorContext var1);

    default byte[] toBytes(Object object) {
        try {
            return JSON.toJSONString(object).getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            LOG.error("", var3);
            return new byte[0];
        }
    }

    default byte[] toBytes(List<? extends Object> list) {
        try {
            StringBuilder e = new StringBuilder();
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
                Object obj = var3.next();
                if(obj instanceof String) {
                    e.append((String)obj).append("\n");
                } else {
                    e.append(JSON.toJSONString(obj)).append("\n");
                }
            }

            return e.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException var5) {
            LOG.error("", var5);
            return new byte[0];
        }
    }
}
