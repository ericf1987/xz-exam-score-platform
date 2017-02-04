package com.xz.scorep.executor.project;

import com.xz.ajiaedu.common.beans.user.School;
import com.xz.scorep.executor.db.DBIHandle;
import com.xz.scorep.executor.db.DbiHandleFactoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchoolService {

    @Autowired
    private DbiHandleFactoryManager dbiHandleFactoryManager;

    public void saveSchool(String projectId, School school) {
        DBIHandle dbiHandle = getProjectDBIHandle(projectId);
        dbiHandle.runHandle(handle -> {
            handle.execute("delete from school where id=?", school.getId());

            String sql = "insert into school(id,name,area,city,province) values(?,?,?,?,?)";
            handle.insert(sql, school.getId(), school.getName(),
                    school.getArea(), school.getCity(), school.getProvince());
        });
    }

    private DBIHandle getProjectDBIHandle(String projectId) {
        return dbiHandleFactoryManager.getDefaultDbiHandleFactory().getProjectDBIHandle(projectId);
    }
}
