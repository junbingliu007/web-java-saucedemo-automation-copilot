package framework.driver;

import framework.config.Config;
import framework.config.ConfigLoader;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

public class DriverFactory {
  private static final ThreadLocal<WebDriver> TL = new ThreadLocal<>();

  public static WebDriver getDriver() {
    WebDriver d = TL.get();
    if (d == null) {
      d = createDriver();
      TL.set(d);
    }
    return d;
  }

  public static void quitDriver() {
    WebDriver d = TL.get();
    if (d != null) {
      d.quit();
      TL.remove();
    }
  }

  private static WebDriver createDriver() {
    Config cfg = ConfigLoader.get();
    String browser = System.getProperty("browser", cfg.defaultBrowser).toUpperCase();
    boolean headless = cfg.headless;
    try {
      switch (BrowserType.valueOf(browser)) {
        case CHROME -> {
          ChromeOptions options = new ChromeOptions();
          options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
          if (headless) options.addArguments("--headless=new");
          options.addArguments("--window-size=1920,1080");
          return buildDriver(options, cfg);
        }
        case FIREFOX -> {
          FirefoxOptions options = new FirefoxOptions();
          if (headless) options.addArguments("-headless");
          return buildDriver(options, cfg);
        }
        case EDGE -> {
          EdgeOptions options = new EdgeOptions();
          if (headless) options.addArguments("--headless=new");
          return buildDriver(options, cfg);
        }
        default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
      }
    } catch (Exception e) {
      throw new RuntimeException("Create driver failed", e);
    }
  }

  private static WebDriver buildDriver(Object options, Config cfg) throws Exception {
    WebDriver driver;
    if (cfg.gridEnabled) {
      driver = new RemoteWebDriver(new URL(cfg.gridUrl), (org.openqa.selenium.Capabilities) options);
    } else {
      if (options instanceof ChromeOptions opt) driver = new ChromeDriver(opt);
      else if (options instanceof FirefoxOptions opt) driver = new FirefoxDriver(opt);
      else if (options instanceof EdgeOptions opt) driver = new EdgeDriver(opt);
      else throw new IllegalArgumentException("Unknown options type");
    }
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(cfg.pageLoadTimeoutSec));
    driver.manage().window().maximize();
    return driver;
  }
}