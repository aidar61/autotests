package com.ts.common.tests;

//import com.ts.common.application.Pages;
import com.ts.common.application.controllers.TrackStudioApiControllers;
import com.ts.common.application.database.DbHelper;
import lombok.Getter;

public abstract class AbstractBaseTest {
    @Getter
    protected static TrackStudioApiControllers apiController;
    @Getter
    protected static DbHelper dbHelper;
//    @Getter
//    protected static Pages trackStudioPages;

}
