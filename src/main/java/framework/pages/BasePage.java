package framework.pages;

import framework.config.ConfigLoader;
import framework.driver.DriverFactory;
import framework.utils.Waits;
import io.qameta.allure.Step;
import org.openqa.selenium.*;

public abstract class BasePage {
  protected final WebDriver driver;
  protected final Waits waits;

  protected BasePage() {
    this.driver = DriverFactory.getDriver();
    this.waits = new Waits(driver, ConfigLoader.get().explicitWaitSec);
  }

//  protected WebElement $(By locator) { return waits.visible(locator); }

  protected WebElement $(By locator) {
    try {
      return driver.findElement(locator);
    } catch (StaleElementReferenceException e) {
      // 重新查找元素
      return driver.findElement(locator);
    } catch (NoSuchElementException e) {
      // 显示等待
      return waits.visible(locator);
    }
  }

  @Step("点击元素: {locator}")
  protected void click(By locator) { waits.clickable(locator).click(); }

  @Step("输入文本: {text} 到 {locator}")
  protected void type(By locator, String text) {
    WebElement el = $(locator);
    el.clear();
    el.sendKeys(text);
  }

  protected String getText(By locator) { return $(locator).getText(); }
}