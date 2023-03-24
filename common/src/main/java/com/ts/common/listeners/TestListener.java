package com.ts.common.listeners;


import io.qameta.allure.Attachment;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

@Slf4j
public class TestListener extends TestListenerAdapter {
    private final ConsoleOutputCapturer consoleOutputCapturer = new ConsoleOutputCapturer();


    @SuppressWarnings("UnusedReturnValue")
    @Attachment(value = "Test Log", type = "text/plain")
    public String stopCatch() {
        return consoleOutputCapturer.stop();
    }

    @Override
    public void beforeConfiguration(ITestResult tr) {
        consoleOutputCapturer.start();
        super.beforeConfiguration(tr);
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        super.onConfigurationSkip(itr);
    }


    @Override
    public void onTestSuccess(ITestResult tr) {
        stopCatch();
        printTestResult(tr);
        super.onTestSuccess(tr);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        stopCatch();
        printTestResult(tr);
        super.onTestFailure(tr);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        stopCatch();
        printTestResult(tr);
        super.onTestSkipped(tr);
    }


    @Override
    public void onStart(ITestContext testContext) {
        //Before class
        String className = testContext.getAllTestMethods().length > 0 ? testContext.getAllTestMethods()[0].getInstance().getClass().toString() : "UNDEFINED";
        log.warn("START CLASS: {}", className);
        super.onStart(testContext);
    }

    @Override
    public void onFinish(ITestContext testContext) {
        //After class
        String className = testContext.getAllTestMethods().length > 0 ? testContext.getAllTestMethods()[0].getInstance().getClass().toString() : "UNDEFINED";
        log.warn("CLASS FINISHED: {}", className);
        log.warn("Passed tests: {}", testContext.getPassedTests().size());
        log.warn("Failed tests: {}", testContext.getFailedTests().size());
        log.warn("Skipped tests: {}", testContext.getSkippedTests().size());

        super.onFinish(testContext);
    }

    private String getExecutionStatus(ITestResult testResult) {
        switch (testResult.getStatus()) {
            case ITestResult.SUCCESS:
                return "PASSED";
            case ITestResult.FAILURE:
                return "FAILED";
            case ITestResult.SKIP:
                return "SKIPPED";
            default:
                return "NOT_EXECUTED";
        }
    }

    private void printTestResult(ITestResult testResult) {
        log.warn("Test is " + getExecutionStatus(testResult));
        log.warn("Test method name: " + testResult.getMethod().getMethodName());
    }
}
