package framework.config;

public class Config {
  public final String baseUrl;
  public final String defaultBrowser;
  public final boolean headless;
  public final int explicitWaitSec;
  public final int pageLoadTimeoutSec;
  public final boolean screenshotOnFail;
  public final int retryCount;
  public final boolean gridEnabled;
  public final String gridUrl;

  public Config(String baseUrl, String defaultBrowser, boolean headless,
                int explicitWaitSec, int pageLoadTimeoutSec,
                boolean screenshotOnFail, int retryCount,
                boolean gridEnabled, String gridUrl) {
    this.baseUrl = baseUrl;
    this.defaultBrowser = defaultBrowser;
    this.headless = headless;
    this.explicitWaitSec = explicitWaitSec;
    this.pageLoadTimeoutSec = pageLoadTimeoutSec;
    this.screenshotOnFail = screenshotOnFail;
    this.retryCount = retryCount;
    this.gridEnabled = gridEnabled;
    this.gridUrl = gridUrl;
  }
}