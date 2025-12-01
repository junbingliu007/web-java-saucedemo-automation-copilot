package tests;

import framework.assertions.AssertHelper;
import framework.pages.InventoryPage;
import framework.pages.LoginPage;
import framework.utils.CsvUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.apache.commons.csv.CSVRecord;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

@Epic("SauceDemo")
@Feature("登录")
public class LoginTests extends BaseTest {

  @DataProvider(name = "users")
  public Object[][] users() {
    List<CSVRecord> rows = CsvUtils.read("testdata/users.csv");
    Object[][] data = new Object[rows.size()][4];
    for (int i = 0; i < rows.size(); i++) {
      var r = rows.get(i);
      data[i][0] = r.get("username");
      data[i][1] = r.get("password");
      data[i][2] = r.get("role");
      data[i][3] = r.get("expectedError");
    }
    return data;
  }

  @Story("不同用户登录路径")
  @Test(dataProvider = "users", description = "按用户类型验证登录结果")
  public void loginMatrix(String user, String pass, String role, String expectedError) {
    LoginPage login = new LoginPage();
    InventoryPage inv = login.loginAs(user, pass);
    if (expectedError != null && !expectedError.isBlank()) {
      String err = login.getError();
      AssertHelper.assertContains(err.toLowerCase(), "locked", "锁定用户应出现错误提示");
    } else {
      AssertHelper.assertTrue(inv.isLoaded(), "登录成功后应进入商品页");
    }
  }

  @Story("负例 - 空密码")
  @Test
  public void loginEmptyPassword() {
    LoginPage login = new LoginPage();
    login.loginAs("standard_user", "");
    AssertHelper.assertContains(login.getError().toLowerCase(), "password", "应提示密码必填");
  }
}