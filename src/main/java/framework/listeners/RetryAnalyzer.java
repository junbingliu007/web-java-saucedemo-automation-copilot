package framework.listeners;

import framework.config.ConfigLoader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
  private int count = 0;
  private final int max = ConfigLoader.get().retryCount;
  @Override
  public boolean retry(ITestResult result) {
    return count++ < max;
  }
}