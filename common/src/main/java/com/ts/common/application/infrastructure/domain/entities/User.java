package com.ts.common.application.infrastructure.domain.entities;

import com.ts.common.application.infrastructure.domain.BaseEntity;

import javax.persistence.*;
import java.text.MessageFormat;

@Entity
@Table(name = "GR_USER")
public class User extends BaseEntity {
    public User() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    String id;
    @Column(name = "USER_LOGIN")
    String login;
    @Column(name = "USER_NAME")
    String name;

    @Override
    public String toString() {
        return MessageFormat.format("Id: {0}, Login: {1}, Name: {2}", this.id, this.login, this.name);
    }
}
