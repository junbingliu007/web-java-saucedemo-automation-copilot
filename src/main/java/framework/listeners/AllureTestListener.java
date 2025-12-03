// java
package framework.listeners;

import framework.config.ConfigLoader;
import framework.driver.DriverFactory;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class AllureTestListener implements ITestListener, ISuiteListener {
    @Override
    public void onTestFailure(ITestResult result) {
        var cfg = ConfigLoader.get();
        WebDriver driver = DriverFactory.getDriver();
        if (cfg.screenshotOnFail && driver != null) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.getLifecycle().addAttachment(
                        "失败截图 - " + result.getName(),
                        "image/png",
                        "png",
                        new ByteArrayInputStream(screenshot)
                );
            } catch (Exception e) {
                Allure.getLifecycle().addAttachment(
                        "截图异常",
                        "text/plain",
                        "txt",
                        new ByteArrayInputStream(("截图失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8))
                );
            }
        }
        if (driver != null) {
            try {
                String pageSource = driver.getPageSource();
                Allure.getLifecycle().addAttachment(
                        "PageSource",
                        "text/html",
                        "html",
                        new ByteArrayInputStream(pageSource.getBytes(StandardCharsets.UTF_8))
                );
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    }

    @Override
    public void onStart(ISuite suite) {
    }

    @Override
    public void onFinish(ISuite suite) {
    }
}