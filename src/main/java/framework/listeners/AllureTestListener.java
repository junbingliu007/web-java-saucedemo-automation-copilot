package framework.listeners;

import framework.config.ConfigLoader;
import framework.driver.DriverFactory;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;

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
            "image/png", "png", screenshot
        );
      } catch (Exception e) {
        Allure.addAttachment("截图异常", "text/plain", ("截图失败: " + e.getMessage()), "txt");
      }
    }
    try {
      String pageSource = driver.getPageSource();
      Allure.addAttachment("PageSource", "text/html", pageSource.getBytes(StandardCharsets.UTF_8), ".html");
    } catch (Exception ignore) {}
  }
  @Override public void onTestSkipped(ITestResult result) {}
  @Override public void onTestSuccess(ITestResult result) {}
  @Override public void onStart(ISuite suite) {}
  @Override public void onFinish(ISuite suite) {}
}