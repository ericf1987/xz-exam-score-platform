package com.xz.scorep.executor.exportexcel;

import com.xz.ajiaedu.common.lang.Context;
import com.xz.ajiaedu.common.xml.XmlNode;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelConfigParser {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelConfigParser.class);

    public static final String RANGE_PROVINCE = "range:province";

    public static final String RANGE_SCHOOL = "range:school";

    public static final String RANGE_CLASS = "range:class";

    public static final String TARGET_SUBJECT = "target:subject";

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ProjectService projectService;

    public List<ReportTask> parse(String projectId, XmlNode reportConfig) {

        XmlNode reportSet = getReportSetNode(projectId, reportConfig);
        List<ReportTask> reportTasks = new ArrayList<>();

        try {
            Context context = new Context()
                    .put("projectId", projectId)
                    .put(RANGE_PROVINCE, Range.PROVINCE_RANGE)
                    .put("base", reportSet.getString("base"));

            iterateReportSet(context, reportSet, "", reportTasks);
        } catch (Exception e) {
            throw new ExcelReportException(e);
        }
        return reportTasks;
    }

    private XmlNode getReportSetNode(String projectId, XmlNode reportConfig) {
        List<XmlNode> reportSets = reportConfig.getChildren(xmlNode ->
                xmlNode.getTagName().equals("report-set") && xmlNode.getString("id").equals(projectId));

        if (reportSets.isEmpty()) {
            reportSets = reportConfig.getChildren(xmlNode ->
                    xmlNode.getTagName().equals("report-set") && xmlNode.getString("id").equals("default"));
        }

        return reportSets.get(0);
    }


    @SuppressWarnings("unchecked")
    protected void iterateReportSet(
            Context context, XmlNode xmlNode, String path, List<ReportTask> reportTasks) {

        String projectId = context.get("projectId");
        String tagName = xmlNode.getTagName();
        Context newContext = new Context(context);

        if (tagName.equals("report-set")) {
            xmlNode.getChildren().forEach(child -> iterateReportSet(newContext, child, path, reportTasks));

        } else {
            if (tagName.equals("dir")) {
                String newPath = path + "/" + fixPlaceHolders(xmlNode.getString("name"), newContext);

                xmlNode.getChildren().forEach(child -> iterateReportSet(newContext, child, newPath, reportTasks));

            } else if (tagName.equals("iterate")) {
                String type = xmlNode.getString("type");

                if (type.equals(RANGE_SCHOOL)) {
                    List<ProjectSchool> schools = schoolService.listSchool(projectId);
                    schools.forEach(school -> {
                        newContext.put(RANGE_SCHOOL, Range.school(school.getId(), school.getName()));
                        xmlNode.getChildren().forEach(child -> iterateReportSet(newContext, child, path, reportTasks));
                    });

                } else if (type.equals(RANGE_CLASS)) {
                    Range school = newContext.get(RANGE_SCHOOL);
                    List<ProjectClass> classes = classService.listClasses(projectId, school.getId());
                    classes.forEach(projectClass -> {
                        newContext.put(RANGE_CLASS, Range.clazz(projectClass.getId(), projectClass.fixedName()));
                        xmlNode.getChildren().forEach(child -> iterateReportSet(newContext, child, path, reportTasks));
                    });

                } else if (type.equals(TARGET_SUBJECT)) {
                    List<ExamSubject> subjects = subjectService.listSubjects(projectId);
                    subjects.forEach(subject -> {
                        newContext.put(TARGET_SUBJECT, Target.subject(subject.getId(), subject.getName()));
                        xmlNode.getChildren().forEach(child -> iterateReportSet(newContext, child, path, reportTasks));
                    });

                } else {
                    throw new IllegalStateException("Unknown iterate type '" + type + "'");
                }

            } else if (tagName.equals("report")) {
                String className = newContext.getString("base") + xmlNode.getString("class");
                String fileName = fixPlaceHolders(xmlNode.getString("name"), newContext) + ".xlsx";
                Class<? extends ReportGenerator> generatorClass = null;

                try {
                    generatorClass = (Class<? extends ReportGenerator>) Class.forName(className);
                } catch (ClassNotFoundException e) {
                    // nothing to do
                }

                if (generatorClass != null) {
                    Range range = getContextRange(newContext);
                    Target target = getContextTarget(newContext);
                    ReportTask task = createReportTasks(path, fileName, generatorClass, range, target);
                    reportTasks.add(task);
                }

            } else {
                throw new IllegalStateException("Unknown tag name '" + tagName + "'");
            }
        }
    }

    private Target getContextTarget(Context context) {
        if (context.containsKey(TARGET_SUBJECT)) {
            return context.get(TARGET_SUBJECT);
        } else {
            return null;
        }
    }

    private Range getContextRange(Context context) {
        if (context.containsKey(RANGE_CLASS)) {
            return context.get(RANGE_CLASS);
        } else if (context.containsKey(RANGE_SCHOOL)) {
            return context.get(RANGE_SCHOOL);
        } else {
            return context.get(RANGE_PROVINCE);
        }
    }

    private String fixPlaceHolders(String pattern, Context context) {
        Range rangeSchool = context.get(RANGE_SCHOOL);
        Range rangeClass = context.get(RANGE_CLASS);
        Target targetSubject = context.get(TARGET_SUBJECT);

        if (rangeSchool != null) {
            pattern = pattern.replace("{{school}}", rangeSchool.getName());
        }

        if (rangeClass != null) {
            pattern = pattern.replace("{{class}}", rangeClass.getName());
        }

        if (targetSubject != null) {
            pattern = pattern.replace("{{subject}}", targetSubject.getName());
        }

        return pattern;
    }

    private ReportTask createReportTasks(
            String path, String filename, Class<? extends ReportGenerator> generatorClass,
            Range range, Target target) {
        return new ReportTask(generatorClass, path, filename, range, target);
    }

    //////////////////////////////////////////////////////////////

}
