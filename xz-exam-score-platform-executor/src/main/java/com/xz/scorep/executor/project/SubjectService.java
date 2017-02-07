package com.xz.scorep.executor.project;

import com.xz.scorep.executor.db.DbiHandleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SubjectService {

    @Autowired
    private DbiHandleFactory dbiHandleFactory;

    public void clearSubjects(String projectId) {
        dbiHandleFactory.getProjectDBIHandle(projectId)
                .runHandle(handle -> handle.execute("truncate table subject"));
    }

    public void saveSubject(String projectId, String subjectId) {
        dbiHandleFactory.getProjectDBIHandle(projectId).runHandle(handle -> {
            String querySubject = "select * from subject where id=?";
            Map<String, Object> map = handle.createQuery(querySubject).bind(0, subjectId).first();

            if (map == null) {
                handle.insert("insert into subject(id) values(?)", subjectId);
            }
        });
    }
}
