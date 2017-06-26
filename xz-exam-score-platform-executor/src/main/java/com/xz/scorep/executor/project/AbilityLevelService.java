package com.xz.scorep.executor.project;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.json.JSONUtils;
import com.xz.scorep.executor.bean.AbilityLevel;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author by fengye on 2017/6/26.
 */
@Service
public class AbilityLevelService {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    CacheFactory cacheFactory;

    @Autowired
    ProjectService projectService;

    private static final Map<String, String> GRADE_STUDY_STAGE_MAP = new HashMap<>();

    @PostConstruct
    public void init() {
        GRADE_STUDY_STAGE_MAP.put("1", "1");
        GRADE_STUDY_STAGE_MAP.put("2", "1");
        GRADE_STUDY_STAGE_MAP.put("3", "1");
        GRADE_STUDY_STAGE_MAP.put("4", "1");
        GRADE_STUDY_STAGE_MAP.put("5", "1");
        GRADE_STUDY_STAGE_MAP.put("6", "1");

        GRADE_STUDY_STAGE_MAP.put("7", "2");
        GRADE_STUDY_STAGE_MAP.put("8", "2");
        GRADE_STUDY_STAGE_MAP.put("9", "2");

        GRADE_STUDY_STAGE_MAP.put("10", "3");
        GRADE_STUDY_STAGE_MAP.put("11", "3");
        GRADE_STUDY_STAGE_MAP.put("12", "3");

        GRADE_STUDY_STAGE_MAP.put("0", "0");
    }

    public String findProjectStudyStage(String projectId){
        ExamProject project = projectService.findProject(projectId);

        if(null != project){
            int grade = project.getGrade();
            return GRADE_STUDY_STAGE_MAP.get(String.valueOf(grade));
        }
        return GRADE_STUDY_STAGE_MAP.get("0");
    }

    public void saveAbilityLevels(String projectId, DAO projectDao, JSONArray levels, String subjectId) {
        List<AbilityLevel> abilityLevelList = new ArrayList<>();
        JSONUtils.<JSONObject>forEach(levels, l -> {
            AbilityLevel abilityLevel = new AbilityLevel();
            abilityLevel.setAbilityType(l.getString("ability_type"));
            abilityLevel.setLevelId(l.getString("level_id"));
            abilityLevel.setLevelName(l.getString("level_name"));
            abilityLevel.setSubjectId(subjectId);
            abilityLevel.setStudyStage(findProjectStudyStage(projectId));
            abilityLevelList.add(abilityLevel);
        });
        projectDao.insert(abilityLevelList, "ability_level");
    }
}
