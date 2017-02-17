package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.SheetHeaderBuilder;
import com.xz.scorep.executor.exportexcel.SheetHeaderBuilder.Direction;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

/**
 * @author by fengye on 2016/6/24.
 *         总体成绩分析-基础数据-学生各科成绩明细
 */
@SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
@Component
public class TotalBasicDataSheets extends SheetGenerator {

    @Autowired
    private SubjectService subjectService;

    @Override
    protected void generateSheet(String projectId, ExcelWriter excelWriter, SheetTask sheetTask) throws Exception {
        setupHeader(projectId, excelWriter);
    }

    private void setupHeader(String projectId, ExcelWriter excelWriter) {
        SheetHeaderBuilder builder = new SheetHeaderBuilder(excelWriter);

        Stream.of("考号", "学校考号", "姓名", "市区", "学校", "班级")
                .forEach(text -> builder.setAndMove(text, 2, 1, Direction.RIGHT));

        Stream.concat(Stream.of(), subjectService.listSubjects(projectId).stream())
                .forEach(subject -> {
                    builder.set(subject.getName(), 1, 2);
                    builder.move(Direction.DOWN);
                    builder.setAndMove("得分", 1, 1, Direction.RIGHT);
                    builder.setAndMove("排名", 1, 1, Direction.RIGHT);
                    builder.move(Direction.UP);
        });
    }
}
