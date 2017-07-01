package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.simplecache.SimpleCache;
import com.xz.scorep.executor.bean.ExamQuestType;
import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author by fengye on 2017/6/22.
 */
@Service
public class QuestTypeService {

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private CacheFactory cacheFactory;

    private static final Logger LOG = LoggerFactory.getLogger(QuestService.class);

    public void saveQuestType(String projectId, List<ExamQuestType> examQuestTypes) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.execute("truncate table quest_type_list");
        projectDao.insert(examQuestTypes, "quest_type_list");
    }

    public double getQuestTypeFullScore(String projectId, String questTypeId){
        return getQuestType(projectId, questTypeId).getFullScore();
    }

    public ExamQuestType getQuestType(String projectId, String questTypeId){
        return daoFactory.getProjectDao(projectId).queryFirst(ExamQuestType.class, "select * from quest_type_list where id = ?", questTypeId);
    }
}
