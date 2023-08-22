package com.ts.common.application.infrastructure.domain.entities;

import com.ts.common.application.infrastructure.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "GR_TASK")
public class Task extends BaseEntity {
    @Column(name = "TASK_ID")
    public String id;
    @Column(name = "TASK_SHORTNAME")
    public String shortName;
    @Column(name = "TASK_NAME")
    public String name;
    @Column(name = "TASK_SUBMITDATE")
    public Date submitDate;
    @Column(name = "TASK_UPDATEDATE")
    public Date updateDate;
    @Column(name = "TASK_CLOSEDATE")
    public Date closetDate;
    @Column(name = "TASK_ABUDGET")
    public double aBudget;
    @Column(name = "TASK_BUDGET")
    public double budget;
    @Column(name = "TASK_DEADLINE")
    public Date deadLine;
    @Column(name = "TASK_CATEGORY")
    public String category;
    @Column(name = "TASK_STATUS")
    public String status;
    @Column(name = "TASK_RESOLUTION")
    public String resolution;
    @Column(name = "TASK_PRIORITY")
    public String priority;
    @Column(name = "TASK_SUBMITTER")
    public String submitter;
    @Column(name = "TASK_HANDLER")
    public String handler;
    @Column(name = "TASK_PARENT")
    public String parent;
    @Column(name = "TASK_NUMBER")
    public String number;
    @Column(name = "TASK_PATH")
    public String path;
    @Column(name = "TASK_TEXT")
    public String text;
    @Column(name = "TASK_UPDSUBMITTER")
    public String updSubmitter;
    @Column(name = "TASK_STATEDATE")
    public Date stateDate;
}