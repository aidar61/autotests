package com.ts.common.application.infrastructure.dao;

import com.ts.common.application.infrastructure.domain.HibernateSessionFactoryUtil;
import com.ts.common.application.infrastructure.domain.entities.User;

public class UserDao {
    public User findById(String id) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(User.class, id);
    }
}
