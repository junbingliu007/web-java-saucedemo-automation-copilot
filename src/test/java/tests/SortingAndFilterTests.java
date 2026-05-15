package tests;

import framework.assertions.AssertHelper;
import framework.pages.InventoryPage;
import framework.pages.LoginPage;
import framework.utils.CsvUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.apache.commons.csv.CSVRecord;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.IntStream;

@Epic("SauceDemo")
@Feature("排序与筛选")
public class SortingAndFilterTests extends BaseTest {

    @DataProvider(name = "standardUser")
    public Object[][] standardUser() {
        List<CSVRecord> rows = CsvUtils.read("testdata/users.csv");
        return rows.stream()
                .filter(r -> "standard".equals(r.get("role")))
                .map(r -> new Object[]{r.get("username"), r.get("password")})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "standardUser")
    public void sortByPriceLowToHigh(String user, String pass) {
        InventoryPage inv = new LoginPage().loginAs(user, pass);
        inv.sortBy("Price (low to high)");
        List<Double> prices = inv.collectPrices();
        boolean ascending = IntStream.range(1, prices.size())
                .allMatch(i -> prices.get(i) >= prices.get(i - 1));
        AssertHelper.assertTrue(ascending, "价格应按低到高排序");
    }
}