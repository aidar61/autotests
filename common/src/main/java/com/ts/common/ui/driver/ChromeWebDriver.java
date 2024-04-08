//package com.ts.common.ui.driver;
//
//import com.ts.common.config.AppConfigProvider;
//import io.github.bonigarcia.wdm.WebDriverManager;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//
//import java.time.Duration;
//
//import static com.ts.common.config.AppConfigProvider.*;
//
//public class ChromeWebDriver {
//    public static WebDriver loadChromeDriver() {
//        WebDriverManager.chromedriver().setup();
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--start-maximized");
//        options.addArguments("--disable-extensions");
//        options.addArguments("--window-size=1920,1080");
//
//        if (Boolean.parseBoolean(AppConfigProvider.get().headless())) {
//            options.addArguments("--headless");
//        }
//        WebDriver driver = new ChromeDriver(options);
//        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICITLY_WAIT_SEC));
//        driver.manage().window().maximize();
//
//        return driver;
//    }
//
//}
