package com.ts.common.listeners;

import io.qameta.allure.Attachment;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class LogCatchListener extends TestListenerAdapter {
    private final ConsoleOutputCapturer consoleOutputCapturer = new ConsoleOutputCapturer();

    @Override
    public void beforeConfiguration(ITestResult tr) {
        consoleOutputCapturer.start();
        super.beforeConfiguration(tr);
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        stopCatch();
        super.onConfigurationSkip(itr);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        stopCatch();
        super.onTestFailure(tr);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        stopCatch();
        super.onTestSuccess(tr);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        stopCatch();
        super.onTestSkipped(tr);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Attachment(value = "Test Log", type = "text/plain")
    public String stopCatch() {
        return consoleOutputCapturer.stop();
    }
}
