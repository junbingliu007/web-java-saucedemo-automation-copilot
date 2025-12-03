package tests;

import framework.assertions.AssertHelper;
import framework.pages.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("SauceDemo")
@Feature("购物车与结账")
public class CartAndCheckoutTests extends BaseTest {

    @Story("标准用户完成一次下单")
    @Test(description = "标准用户下单成功流程")
    public void standardUserCheckout() {
        InventoryPage inv = new LoginPage().loginAs("standard_user", "secret_sauce");
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