package com.ts.common;

import com.ts.common.application.database.DbHelper;
import com.ts.common.controllers.sla.slaHelp.SlaHelpController;
import com.ts.common.tests.AbstractBaseTest;
import com.ts.common.utils.RandomDataUtils;

public class TestTest extends AbstractBaseTest {
    private static SlaHelpController slaHelpController;

    public static void main(String[] args) {
        dbHelper = new DbHelper();
//        System.out.println(dbHelper.getGrTaskTable().receiveByTaskNumber("1405533").toString());
//        System.out.println(dbHelper.getGrUserTable().receiveRandomPerson());
//        System.out.println(RandomDataUtils.getRandomUser());

    }
}
