package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.xml.XmlNode;
import com.xz.ajiaedu.common.xml.XmlNodeReader;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * (description)
 * created at 2017/3/5
 *
 * @author yidin
 */
public class ExcelConfigParserTest extends BaseTest {

    @Autowired
    private ExcelConfigParser excelConfigParser;

    @Test
    public void parse() throws Exception {
        XmlNode reportConfig = XmlNodeReader.read(
                getClass().getResourceAsStream("/report/config/report-config.xml"));

        List<ReportTask> reportTasks = excelConfigParser.parse(PROJECT_ID, reportConfig);
        reportTasks.forEach(System.out::println);
    }

}