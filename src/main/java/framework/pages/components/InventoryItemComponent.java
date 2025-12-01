package framework.pages.components;

import framework.pages.BasePage;
import org.openqa.selenium.By;

public class InventoryItemComponent extends BasePage {
  private final String itemName;
  public InventoryItemComponent(String itemName) { this.itemName = itemName; }

  private By root() {
    return By.xpath("//div[text()='" + itemName + "']/ancestor::div[@class='inventory_item']");
  }
  private By addBtn() { return By.xpath("//div[text()='" + itemName + "']/ancestor::div[@class='inventory_item']//button[contains(@data-test,'add-to-cart')] | //div[text()='" + itemName + "']/ancestor::div[@class='inventory_item']//button[normalize-space()='Add to cart']"); }
  private By removeBtn() { return By.xpath("//div[text()='" + itemName + "']/ancestor::div[@class='inventory_item']//button[contains(@data-test,'remove')] | //div[text()='" + itemName + "']/ancestor::div[@class='inventory_item']//button[normalize-space()='Remove']"); }
  private By price() { return By.xpath("//div[text()='" + itemName + "']/ancestor::div[@class='inventory_item']//div[@class='inventory_item_price']"); }

  public void addToCart() { click(addBtn()); }
  public void removeFromCart() { click(removeBtn()); }
  public String getPriceText() { return getText(price()); }
}