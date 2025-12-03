package framework.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Waits {
    private final WebDriverWait wait;

    public Waits(WebDriver driver, int seconds) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    public WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement presence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
}