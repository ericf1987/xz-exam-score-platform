package com.xz.scorep.executor.project;

import com.xz.ajiaedu.common.beans.exam.ExamProject;
import com.xz.scorep.executor.db.DbiHandleFactoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    @Autowired
    private DbiHandleFactoryManager dbiHandleFactoryManager;

    public void saveProject(ExamProject project) {

    }
}
