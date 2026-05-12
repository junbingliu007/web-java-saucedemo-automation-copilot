package framework.config;

public class Config {
    public final String baseUrl;
    public final String defaultBrowser;
    public final boolean headless;
    public final int explicitWaitSec;
    public final int pageLoadTimeoutSec;
    public final boolean screenshotOnFail;
    /** 用例失败重试次数（IRetryAnalyzer） */
    public final int testRetryCount;
    /** 查找元素失败重试次数（StaleElementReferenceException） */
    public final int elementRetryCount;
    public final boolean gridEnabled;
    public final String gridUrl;

    public Config(String baseUrl, String defaultBrowser, boolean headless,
                  int explicitWaitSec, int pageLoadTimeoutSec,
                  boolean screenshotOnFail, int testRetryCount, int elementRetryCount,
                  boolean gridEnabled, String gridUrl) {
        this.baseUrl = baseUrl;
        this.defaultBrowser = defaultBrowser;
        this.headless = headless;
        this.explicitWaitSec = explicitWaitSec;
        this.pageLoadTimeoutSec = pageLoadTimeoutSec;
        this.screenshotOnFail = screenshotOnFail;
        this.testRetryCount = testRetryCount;
        this.elementRetryCount = elementRetryCount;
        this.gridEnabled = gridEnabled;
        this.gridUrl = gridUrl;
    }
}