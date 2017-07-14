package com.xz.scorep.executor.utils;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.db.DAOFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author luckylo
 * @createTime 2017-07-14.
 */
public class DataBaseUtilsTest extends BaseTest {
    @Autowired
    DAOFactory daoFactory;

    @Test
    public void test() {
        String projectId = "430100-258c4700b34c4842812f1066b3acdf77";
        String subjectId = "003";
        DAO managerDao = daoFactory.getManagerDao();
        String dataBaseName = DataBaseUtils.getDataBaseName(projectId, subjectId, daoFactory);
        System.out.println("dataBase ... " + dataBaseName);

    }

}