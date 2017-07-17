package com.xz.scorep.executor.exportaggrdata.bean;

import com.alibaba.fastjson.JSON;
import com.hyd.simplecache.utils.MD5;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import org.junit.Test;

import java.util.UUID;

/**
 * @author by fengye on 2017/7/17.
 */
public class AllPassOrFailTest extends BaseTest {

    @Test
    public void test1() throws Exception {
        AllPassOrFail allPassOrFail = new AllPassOrFail();

        allPassOrFail.setProject("430500-6539f2f49f74411a8a1beb232a0cedf1");

        Range range = new Range();
        range.setName(Range.SCHOOL);
        range.setId("200f3928-a8bd-48c4-a2f4-322e9ffe3700");
        allPassOrFail.setRange(range);

        allPassOrFail.setAllPassCount(270);
        allPassOrFail.setAllPassRate(47.87);
        allPassOrFail.setAllFailCount(13);
        allPassOrFail.setAllFailRate(2.30);
        allPassOrFail.setMd5(MD5.digest(UUID.randomUUID().toString()));

        String s = JSON.toJSONString(allPassOrFail);
        System.out.println(s);
    }

}