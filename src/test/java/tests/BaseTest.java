package tests;

import framework.config.ConfigLoader;
import framework.driver.DriverFactory;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

public class BaseTest {
    protected WebDriver driver;

    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional String browser) {
        if (browser != null) System.setProperty("browser", browser);
        driver = DriverFactory.getDriver();
        openBaseUrl();
    }

    @Step("打开基础地址")
    protected void openBaseUrl() {
        String url = ConfigLoader.get().baseUrl;
        int attempts = 3;
        for (int i = 1; i <= attempts; i++) {
            try {
                driver.get(url);
                return;
            } catch (Exception e) {
                if (i == attempts) throw e;
                try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}