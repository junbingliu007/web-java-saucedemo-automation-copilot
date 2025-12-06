package framework.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (NoSuchElementException e) {
            return wait.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(locator)));
        }
    }

    public WebElement invisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator)) ? null : null;
    }

    public WebElement textToBePresentInElement(By locator, String text) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text)) ? null : null;
    }


    public WebElement clickable(By locator) {
        try{
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (NoSuchElementException e) {
            return wait.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(locator)));
        }
    }

    public WebElement presence(By locator) {
        try{
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (NoSuchElementException e) {
            return wait.until(ExpectedConditions.refreshed(ExpectedConditions.presenceOfElementLocated(locator)));
        }
    }
}