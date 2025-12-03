package tests;

import framework.assertions.AssertHelper;
import framework.pages.InventoryPage;
import framework.pages.LoginPage;
import org.testng.annotations.Test;

import java.util.List;

public class SortingAndFilterTests extends BaseTest {

    @Test
    public void sortByPriceLowToHigh() {
        InventoryPage inv = new LoginPage().loginAs("standard_user", "secret_sauce");
        inv.sortBy("Price (low to high)");
        List<Double> prices = inv.collectPrices();
        boolean ascending = true;
        for (int i = 1; i < prices.size(); i++) {
            if (prices.get(i) < prices.get(i - 1)) {
                ascending = false;
                break;
            }
        }
        AssertHelper.assertTrue(ascending, "价格应按低到高排序");
    }
}