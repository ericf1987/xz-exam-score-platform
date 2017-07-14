package com.xz.scorep.executor.utils;

import com.xz.scorep.executor.db.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luckylo
 * @createTime 2017-07-13.
 */
public class DataBaseUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DataBaseUtils.class);

    //尝试连接这个科目的备份库,当链接不上时,返回项目的原始库
    public static String getDataBaseName(String projectId, String subjectId, DAOFactory daoFactory) {
        String projectBakId = projectId + "_" + subjectId + "_bak";
        try {
            daoFactory.getProjectDao(projectBakId);
            return projectBakId;
        } finally {
            return projectId;
        }
    }
}
