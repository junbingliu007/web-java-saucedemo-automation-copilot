package framework.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

public class CartPage extends BasePage {
    private final By checkoutBtn = By.id("checkout");

    @Step("继续结账")
    public CheckoutStepOnePage checkout() {
        click(checkoutBtn);
        return new CheckoutStepOnePage();
    }
}