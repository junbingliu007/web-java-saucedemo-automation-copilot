package tests;

import framework.assertions.AssertHelper;
import framework.pages.*;
import framework.utils.CsvUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.apache.commons.csv.CSVRecord;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

@Epic("SauceDemo")
@Feature("购物车与结账")
public class CartAndCheckoutTests extends BaseTest {

    @DataProvider(name = "standardUser")
    public Object[][] standardUser() {
        List<CSVRecord> rows = CsvUtils.read("testdata/users.csv");
        return rows.stream()
                .filter(r -> "standard".equals(r.get("role")))
                .map(r -> new Object[]{r.get("username"), r.get("password")})
                .toArray(Object[][]::new);
    }

    @Story("标准用户完成一次下单")
    @Test(dataProvider = "standardUser", description = "标准用户下单成功流程")
    public void standardUserCheckout(String user, String pass) {
        InventoryPage inv = new LoginPage().loginAs(user, pass);
        AssertHelper.assertTrue(inv.isLoaded(), "登录后应进入商品页");

        inv.addToCart("Sauce Labs Backpack")
                .addToCart("Sauce Labs Bike Light");
        CartPage cart = inv.openCart();
        CheckoutStepOnePage step1 = cart.checkout();
        CheckoutStepTwoPage step2 = step1.fillInfo("Jun-Bing", "Liu", "100000");
        CheckoutCompletePage done = step2.finish();

        AssertHelper.assertTrue(done.isSuccess(), "订单应成功提交");
    }
}