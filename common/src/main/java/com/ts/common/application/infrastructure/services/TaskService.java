package com.ts.common.application.infrastructure.services;

import com.ts.common.application.infrastructure.dao.TaskDao;
import com.ts.common.application.infrastructure.domain.entities.Task;

import java.util.List;

public class TaskService {
    private TaskDao taskDao = new TaskDao();

    public List<Task> GetByCategory(String columnName, String value, int pageIndex, int pageSize){
        return taskDao.findByCategory(columnName, value, pageIndex, pageSize);
    }
}
