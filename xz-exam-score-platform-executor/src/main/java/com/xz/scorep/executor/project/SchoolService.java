package com.xz.scorep.executor.project;

import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolService {

    @Autowired
    private DAOFactory daoFactory;

    public void saveSchool(String projectId, ProjectSchool school) {
        String sql = "insert into school(id,name,area,city,province) values(?,?,?,?,?)";

        daoFactory.getProjectDao(projectId).execute(sql,
                school.getId(), school.getName(), school.getArea(), school.getCity(), school.getProvince());
    }

    public void clearSchools(String projectId) {
        daoFactory.getProjectDao(projectId).execute("truncate table school");
    }

    public ProjectSchool findSchool(String projectId, String schoolId) {
        return daoFactory.getProjectDao(projectId).queryFirst(
                ProjectSchool.class, "select * from school where id=?", schoolId);
    }

    public List<ProjectSchool> listSchool(String projectId) {
        return daoFactory.getProjectDao(projectId).query(ProjectSchool.class, "select * from school");
    }
}
