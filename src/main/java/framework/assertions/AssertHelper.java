package framework.assertions;

import io.qameta.allure.Allure;
import org.testng.Assert;

public class AssertHelper {
  public static void assertTrue(boolean condition, String message) {
    Allure.step("断言为真: " + message);
    Assert.assertTrue(condition, message);
  }
  public static <T> void assertEquals(T actual, T expected, String message) {
    Allure.step("断言相等: " + message + " | 期望=" + expected + " 实际=" + actual);
    Assert.assertEquals(actual, expected, message);
  }
  public static void assertContains(String actual, String expectedSubstr, String message) {
    Allure.step("断言包含: " + message + " | 期望包含='" + expectedSubstr + "' 实际='" + actual + "'");
    Assert.assertTrue(actual != null && actual.contains(expectedSubstr), message);
  }
}