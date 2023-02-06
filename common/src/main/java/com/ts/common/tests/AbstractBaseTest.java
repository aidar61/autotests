package com.ts.common.tests;

import com.ts.common.application.TrackStudioApiControllers;
import lombok.Getter;

public abstract class AbstractBaseTest {
    @Getter
    protected static TrackStudioApiControllers apiController;
}
