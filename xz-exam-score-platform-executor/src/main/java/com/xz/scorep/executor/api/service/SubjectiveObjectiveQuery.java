package com.xz.scorep.executor.api.service;

import com.xz.scorep.executor.cache.CacheFactory;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 主观题,客观题得分详情
 *
 * @author luckylo
 */
@Component
public class SubjectiveObjectiveQuery {
    private static final String QUERY_SUBJECTIVE_SCORE_DETAIL = "";

    private static final String QUERY_OBJECTIVE_SCORE_DETAIL = "";

    @Autowired
    private CacheFactory cacheFactory;

    @Autowired
    private DAOFactory daoFactory;

}
