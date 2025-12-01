package framework.pages;

import framework.pages.components.HeaderComponent;
import framework.pages.components.InventoryItemComponent;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class InventoryPage extends BasePage {
  private final By title = By.cssSelector(".title");
  private final By sortSelect = By.cssSelector(".product_sort_container");
  private final By priceEls = By.cssSelector(".inventory_item_price");

  public boolean isLoaded() {
    try { return getText(title).equalsIgnoreCase("Products"); } catch (Exception e) { return false; }
  }

  @Step("将商品加入购物车：{itemName}")
  public InventoryPage addToCart(String itemName) {
    new InventoryItemComponent(itemName).addToCart();
    return this;
  }

  public HeaderComponent header() { return new HeaderComponent(); }

  @Step("打开购物车")
  public CartPage openCart() { header().openCart(); return new CartPage(); }

  @Step("设置排序：{visibleText}")
  public InventoryPage sortBy(String visibleText) {
    click(sortSelect);
    click(By.xpath("//select[@class='product_sort_container']/option[normalize-space(.)='" + visibleText + "']"));
    return this;
  }

  public List<Double> collectPrices() {
    List<Double> prices = new ArrayList<>();
    for (WebElement el : driver.findElements(priceEls)) {
      String t = el.getText().replace("$", "").trim();
      try { prices.add(Double.parseDouble(t)); } catch (NumberFormatException ignore) {}
    }
    return prices;
  }
}