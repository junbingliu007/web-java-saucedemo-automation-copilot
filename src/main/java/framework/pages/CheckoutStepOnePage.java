package framework.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

public class CheckoutStepOnePage extends BasePage {
    private final By firstName = By.id("first-name");
    private final By lastName = By.id("last-name");
    private final By postalCode = By.id("postal-code");
    private final By continueBtn = By.id("continue");
    private final By errorMsg = By.cssSelector("h3[data-test='error']");

    @Step("填写收货信息：{fn} {ln} {zip}")
    public CheckoutStepTwoPage fillInfo(String fn, String ln, String zip) {
        type(firstName, fn);
        type(lastName, ln);
        type(postalCode, zip);
        click(continueBtn);
        return new CheckoutStepTwoPage();
    }

    public String getError() {
        try {
            return getText(errorMsg);
        } catch (Exception e) {
            return "";
        }
    }
}