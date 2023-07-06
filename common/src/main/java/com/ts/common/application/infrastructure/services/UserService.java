package com.ts.common.application.infrastructure.services;

import com.ts.common.application.infrastructure.dao.UserDao;
import com.ts.common.application.infrastructure.domain.entities.User;

public class UserService {
    private UserDao usersDao = new UserDao();

    public User findUser(String id) {
        return usersDao.findById(id);
    }
}
