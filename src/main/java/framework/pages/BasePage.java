package framework.pages;

import framework.config.ConfigLoader;
import framework.driver.DriverFactory;
import framework.utils.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.*;

import java.util.function.Function;

public abstract class BasePage {
  protected final WebDriver driver;
  protected final Waits waits;

  protected BasePage() {
    this.driver = DriverFactory.getDriver();
    this.waits = new Waits(driver, ConfigLoader.get().explicitWaitSec);
  }

  // 查找+操作原子化，彻底消除 Stale 窗口期
  protected <T> T withRetry(By locator, Function<WebElement, T> action) {
    int retries = ConfigLoader.get().elementRetryCount;
    for (int i = 0; i < retries; i++) {
      try {
        return action.apply(waits.visible(locator));  // 每次重试都重新等待+查找
      } catch (StaleElementReferenceException e) {
        if (i == retries - 1) throw e;
      }
    }
    throw new RuntimeException("Action failed after retries: " + locator);
  }

  @Step("点击元素: {locator}")
  protected void click(By locator) {
    withRetry(locator, el -> { el.click(); return null; });
  }

  @Step("输入文本: {text} 到 {locator}")
  protected void type(By locator, String text) {
    withRetry(locator, el -> { el.clear(); el.sendKeys(text); return null; });
  }

  protected String getText(By locator) {
    return withRetry(locator, WebElement::getText);
  }
}
