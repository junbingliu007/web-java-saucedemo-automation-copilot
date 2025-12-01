package framework.pages;

import org.openqa.selenium.By;

public class CheckoutCompletePage extends BasePage {
  private final By completeHeader = By.cssSelector(".complete-header");
  public boolean isSuccess() {
    try { return getText(completeHeader).toUpperCase().contains("THANK YOU"); } catch (Exception e) { return false; }
  }
}