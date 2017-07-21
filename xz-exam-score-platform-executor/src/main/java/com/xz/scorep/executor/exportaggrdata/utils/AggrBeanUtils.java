package com.xz.scorep.executor.exportaggrdata.utils;

import com.xz.scorep.executor.bean.Target;
import org.springframework.util.StringUtils;

/**
 * @author by fengye on 2017/7/21.
 */
public class AggrBeanUtils {
    public static void setTarget(String projectId, String subjectId, Target target) {
        if(StringUtils.isEmpty(subjectId)){
            target.setId(projectId);
            target.setName(Target.PROJECT);
        }else{
            target.setId(subjectId);
            target.setName(Target.SUBJECT);
        }
    }
}
