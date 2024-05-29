package com.ts.tests.runners;


import com.ts.common.config.AppConfigProvider;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class RetryTest implements IRetryAnalyzer {
    private static final int RETRIES = AppConfigProvider.get().retriesNumber();
    private static final Map<String, Integer> retriesMap = Collections.synchronizedMap(new HashMap<>());

    @Override
    public boolean retry(ITestResult iTestResult) {
        if (iTestResult.getStatus() == ITestResult.SUCCESS) {
            return false;
        }
        String key = iTestResult.getTestContext().getName() + "/" + iTestResult.getMethod().getMethodName();
        retriesMap.putIfAbsent(key, 0);
        int reties = retriesMap.get(key);
        if (reties < RETRIES) {
            retriesMap.put(key, reties + 1);
            return true;
        }
        return false;
    }

}
