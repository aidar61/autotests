package com.ts.common.application.infrastructure.dao;

import com.ts.common.application.infrastructure.domain.HibernateSessionFactoryUtil;
import com.ts.common.application.infrastructure.domain.entities.Task;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class TaskDao {
    public Task findById(String id) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(Task.class, id);
    }

    public List<Task> findByCategory(String columnName, String filterValue, int pageNumber, int pageSize) {
        try (Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession()) {
            String hql = "FROM Task WHERE " + columnName + " = :filterValue";
            Query<Task> query = session.createQuery(hql, Task.class);
            query.setParameter("filterValue", filterValue);
            query.setFirstResult((pageNumber - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
