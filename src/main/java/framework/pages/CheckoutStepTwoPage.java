package framework.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

public class CheckoutStepTwoPage extends BasePage {
    private final By finishBtn = By.id("finish");

    @Step("确认订单")
    public CheckoutCompletePage finish() {
        click(finishBtn);
        return new CheckoutCompletePage();
    }
}