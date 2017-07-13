package com.xz.scorep.executor.pss.service;

import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.ajiaedu.common.system.SystemUtils;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.api.utils.HttpUtils;
import com.xz.scorep.executor.bean.ExamSubject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author by fengye on 2017/5/24.
 */
public class PssServiceTest extends BaseTest {

    @Autowired
    PssService pssService;

    public static final String PROJECT_ID = "430000-1527d8e87b5a48ed9ba7f9211c449035";

    public static final String SCHOOL_ID = "e367d9cc-acdb-4221-84a5-1de83384b7c8";

    public static final String CLASS_ID = "d82f9ca1-6020-4557-ad5f-335821f1b9bc";

    public static final String SUBJECT_ID = "005";

    public static final String STUDENT_ID = "04c4f670-babe-42bb-84f3-f1012c71dbad";

    @Test
    public void testSendToPDF() throws Exception {
        String relativePath = StringUtil.joinPathsWith("/", PROJECT_ID, SCHOOL_ID, CLASS_ID, SUBJECT_ID);
        String pdfName = STUDENT_ID + "_POSITIVE.pdf";
        String url = pssService.getURL(PROJECT_ID, SCHOOL_ID, CLASS_ID, SUBJECT_ID, STUDENT_ID);
        pssService.sendToPDFByPost(relativePath, pdfName, url);
    }

    @Test
    public void testRunTaskByClassAndSubject() throws Exception {
        ExamSubject examSubject = new ExamSubject();
        examSubject.setId(SUBJECT_ID);

        pssService.runTaskByClassAndSubject(PROJECT_ID, SCHOOL_ID, null, SUBJECT_ID, null);
    }

    @Test
    public void test1() throws Exception{
        String url = "http://192.168.1.200:8080/createPdfByUrlSync?isVertical=false&relativePath=430700-a5a39f1f86b3408d9ced3cf82eb8a1c9%2F3ce843ad-87ab-45c1-a650-c142fa438159%2Fd82f9ca1-6020-4557-ad5f-335821f1b9bc%2F005&createUrl=http%3A%2F%2F192.168.1.56%3A8080%2Fpss%2FshowImg%3FprojectId%253D430700-a5a39f1f86b3408d9ced3cf82eb8a1c9%2526schoolId%253D3ce843ad-87ab-45c1-a650-c142fa438159%2526classId%253Dd82f9ca1-6020-4557-ad5f-335821f1b9bc%2526subjectId%253D005%2526studentId%253D04c4f670-babe-42bb-84f3-f1012c71dbad%2526isPositive%253Dtrue&pdfName=04c4f670-babe-42bb-84f3-f1012c71dbad_POSITIVE.pdf";
        String url1 = "http://192.168.1.200:8080/createPdfByUrlSync?relativePath=A/B/C&pdfName=123.pdf&createUrl=http://192.168.1.56:8080/pss/showImg?projectId%3D430700-a5a39f1f86b3408d9ced3cf82eb8a1c9%26schoolId%3D3ce843ad-87ab-45c1-a650-c142fa438159%26classId%3Dd82f9ca1-6020-4557-ad5f-335821f1b9bc%26subjectId%3D005%26studentId%3D04c4f670-babe-42bb-84f3-f1012c71dbad%26isPositive%3Dfalse";
        String url2 = "http://192.168.1.200:8080/createPdfByUrlSync?isVertical=false&relativePath=430700-a5a39f1f86b3408d9ced3cf82eb8a1c9%2F3ce843ad-87ab-45c1-a650-c142fa438159%2Fd82f9ca1-6020-4557-ad5f-335821f1b9bc%2F005&createUrl=http%3A%2F%2F192.168.1.56%3A8080%2Fpss%2FshowImg%3FprojectId%253D430700-a5a39f1f86b3408d9ced3cf82eb8a1c9%2526schoolId%253D3ce843ad-87ab-45c1-a650-c142fa438159%2526classId%253Dd82f9ca1-6020-4557-ad5f-335821f1b9bc%2526subjectId%253D005%2526studentId%253D04c4f670-babe-42bb-84f3-f1012c71dbad%2526isPositive%253Dtrue&pdfName=04c4f670-babe-42bb-84f3-f1012c71dbad_POSITIVE.pdf";
        String decode = URLDecoder.decode(url2, "utf-8");
        System.out.println(decode);
    }

    @Test
    public void test2() throws Exception{
        String baidu = "http://www.baidu.com";
        Map<String, String> map = new HashMap<>();
        map.put("wd", "123");
        HttpUtils.sendRequest(baidu, map);
    }

    @Test
    public void test3() throws Exception{
        String url = "http://192.168.1.200:8080/createPdfByUrlSync";
        HttpRequest request = new HttpRequest(url)
                .setParameter("isVertical", "false")
                .setParameter("relativePath", "430700-a5a39f1f86b3408d9ced3cf82eb8a1c9%2F3ce843ad-87ab-45c1-a650-c142fa438159%2Fd82f9ca1-6020-4557-ad5f-335821f1b9bc%2F005")
                .setParameter("createUrl", "http://192.168.1.56:8080/pss/showImg?projectId%3D430700-a5a39f1f86b3408d9ced3cf82eb8a1c9%26schoolId%3D3ce843ad-87ab-45c1-a650-c142fa438159%26classId%3Dd82f9ca1-6020-4557-ad5f-335821f1b9bc%26subjectId%3D005%26studentId%3D04c4f670-babe-42bb-84f3-f1012c71dbad%26isPositive%3Dtrue")
                .setParameter("pdfName", "04c4f670-babe-42bb-84f3-f1012c71dbad_POSITIVE.pdf");
        System.out.println(request.request());
    }

    @Test
    public void Test4() throws Exception{
        pssService.regenerateFail(PROJECT_ID);
    }

    @Test
    public void pressureTest() throws Exception{

        List<PdfTask> tasks = new ArrayList<>();

        long begin = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            String fileName = i + ".pdf";
            String dir = StringUtil.joinPathsWith("/", "pressureTest");
            String url = "https://git-scm.com/docs";
            PdfTask pdfTask = new PdfTask(fileName, dir, url, false);
            pdfTask.start();
            tasks.add(pdfTask);
        }

        long end = System.currentTimeMillis();

        System.out.println(end - begin);
    }

    class PdfTask extends Thread{

        private String fileName;

        private String dir;

        private String url;

        private boolean isVertical;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isVertical() {
            return isVertical;
        }

        public void setVertical(boolean vertical) {
            isVertical = vertical;
        }

        public PdfTask(){

        }

        public PdfTask(String fileName, String dir, String url, boolean isVertical){
            this.fileName = fileName;
            this.dir = dir;
            this.url = url;
            this.isVertical = isVertical;
        }

        @Override
        public void run() {
            pssService.sendToPDFByPost(this.dir, this.fileName, this.url);
        }
    }
}