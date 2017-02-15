package com.xz.scorep.executor.project;

import com.xz.scorep.executor.bean.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RangeService {

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    public List<Range> queryRanges(String projectId, String rangeType) {

        if (rangeType.equalsIgnoreCase("province")) {
            return Collections.singletonList(Range.province("430000"));

        } else if (rangeType.equalsIgnoreCase("school")) {
            return schoolService.listSchool(projectId).stream()
                    .map(school -> Range.school(school.getId()))
                    .collect(Collectors.toList());

        } else if (rangeType.equalsIgnoreCase("class")) {
            return classService.listClasses(projectId).stream()
                    .map(c -> Range.clazz(c.getId()))
                    .collect(Collectors.toList());

        }

        throw new IllegalArgumentException("不支持的范围类型 '" + rangeType + "'");
    }
}
