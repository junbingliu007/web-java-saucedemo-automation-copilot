package framework.pages.components;

import framework.pages.BasePage;
import org.openqa.selenium.By;

public class HeaderComponent extends BasePage {
  private final By cartLink = By.cssSelector(".shopping_cart_link");
  private final By cartBadge = By.cssSelector(".shopping_cart_badge");

  public void openCart() { click(cartLink); }
  public int getCartCount() {
    try { return Integer.parseInt(getText(cartBadge)); } catch (Exception e) { return 0; }
  }
}