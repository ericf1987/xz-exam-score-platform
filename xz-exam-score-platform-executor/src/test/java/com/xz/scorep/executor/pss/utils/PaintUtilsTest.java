package com.xz.scorep.executor.pss.utils;

import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/6/16.
 */
public class PaintUtilsTest {

    public static final String POSITIVE = "http://znxunzhi-marking-pic.oss-cn-shenzhen.aliyuncs.com/430700-a5a39f1f86b3408d9ced3cf82eb8a1c9/3ce843ad-87ab-45c1-a650-c142fa438159/001/03/paperImage/132150269_positive.png";

    public static final String REVERSE = "http://znxunzhi-marking-pic.oss-cn-shenzhen.aliyuncs.com/430700-a5a39f1f86b3408d9ced3cf82eb8a1c9/3ce843ad-87ab-45c1-a650-c142fa438159/001/03/paperImage/132150269_reverse.png";

    @Test
    public void testLoadImageUrl() throws Exception {
        BufferedImage bufferedImage = PaintUtils.loadImageUrl(POSITIVE);
    }

    @Test
    public void testWriteImageLocal() throws Exception {

    }

    @Test
    public void testModifyImage() throws Exception {

    }
}