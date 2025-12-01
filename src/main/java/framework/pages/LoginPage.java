package framework.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
  private final By username = By.id("user-name");
  private final By password = By.id("password");
  private final By loginBtn = By.id("login-button");
  private final By errorMsg = By.cssSelector("h3[data-test='error']");

  @Step("登录，用户名：{user}, 密码：{pass}")
  public InventoryPage loginAs(String user, String pass) {
    type(username, user);
    type(password, pass);
    click(loginBtn);
    return new InventoryPage();
  }

  public String getError() { try { return getText(errorMsg); } catch (Exception e) { return ""; } }
}