package com.xz.scorep.executor.expressReport.service;

import com.xz.scorep.executor.pss.service.PssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author by fengye on 2017/7/13.
 */
@Service
public class PressureTestService {

    @Autowired
    PssService pssService;

    @Autowired
    static final Logger LOG = LoggerFactory.getLogger(PressureTestService.class);

    public void startPressureTest(String pdfName, String relativePath, String createUrl, String threadCount) {
        int count = Integer.valueOf(threadCount);

        long begin = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            PdfTask pdfTask = new PdfTask(pdfName + "_" + i + ".pdf", relativePath, createUrl, false);
            pdfTask.start();
            LOG.info("线程：{} 开始作业 文件名：{}，相对路径：{}，URL：{}", pdfTask.getName(), pdfTask.getPdfName(), pdfTask.getRelativePath(), pdfTask.getCreateUrl());
        }

        long end = System.currentTimeMillis();

        LOG.info("测试完成，耗时：{}", end - begin);
    }

    class PdfTask extends Thread {

        private String pdfName;

        private String relativePath;

        private String createUrl;

        private boolean isVertical;

        public String getPdfName() {
            return pdfName;
        }

        public void setPdfName(String pdfName) {
            this.pdfName = pdfName;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public void setRelativePath(String relativePath) {
            this.relativePath = relativePath;
        }

        public String getCreateUrl() {
            return createUrl;
        }

        public void setCreateUrl(String createUrl) {
            this.createUrl = createUrl;
        }

        public boolean isVertical() {
            return isVertical;
        }

        public void setVertical(boolean vertical) {
            isVertical = vertical;
        }

        public PdfTask() {

        }

        public PdfTask(String fileName, String relativePath, String createUrl, boolean isVertical) {
            this.pdfName = fileName;
            this.relativePath = relativePath;
            this.createUrl = createUrl;
            this.isVertical = isVertical;
        }

        @Override
        public void run() {
            pssService.sendToPDFByPost(this.relativePath, this.pdfName, this.createUrl);
        }
    }
}
