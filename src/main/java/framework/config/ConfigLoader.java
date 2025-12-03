package framework.config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static Config CONFIG;

    public static Config get() {
        if (CONFIG == null) {
            try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
                Properties p = new Properties();
                p.load(is);

                String baseUrl = System.getProperty("baseUrl", p.getProperty("baseUrl"));
                String defaultBrowser = System.getProperty("browser", p.getProperty("defaultBrowser"));
                boolean headless = Boolean.parseBoolean(System.getProperty("headless", p.getProperty("headless")));
                int explicitWaitSec = Integer.parseInt(System.getProperty("explicitWaitSec", p.getProperty("explicitWaitSec")));
                int pageLoadTimeoutSec = Integer.parseInt(System.getProperty("pageLoadTimeoutSec", p.getProperty("pageLoadTimeoutSec")));
                boolean screenshotOnFail = Boolean.parseBoolean(System.getProperty("screenshotOnFail", p.getProperty("screenshotOnFail")));
                int retryCount = Integer.parseInt(System.getProperty("retryCount", p.getProperty("retryCount")));
                boolean gridEnabled = Boolean.parseBoolean(System.getProperty("grid.enabled", p.getProperty("grid.enabled")));
                String gridUrl = System.getProperty("grid.url", p.getProperty("grid.url"));

                CONFIG = new Config(baseUrl, defaultBrowser, headless, explicitWaitSec,
                        pageLoadTimeoutSec, screenshotOnFail, retryCount, gridEnabled, gridUrl);
            } catch (Exception e) {
                throw new RuntimeException("Load config failed", e);
            }
        }
        return CONFIG;
    }
}