package com.ts.common;

import com.ts.common.application.database.DbHelper;
import com.ts.common.controllers.sla.SlaHelpController;
import com.ts.common.tests.AbstractBaseTest;

public class TestTest extends AbstractBaseTest {
    private static SlaHelpController slaHelpController;

    public static void main(String[] args) {
        dbHelper = new DbHelper();
        System.out.println(dbHelper.getGrTaskTable().receiveByTaskNumber("1405533").toString());

    }
}
