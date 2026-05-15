# Web自动化测试框架
> 项目：saucedemo-automation | 技术栈：Java 17 + Selenium 4 + TestNG + Allure
---

## 关键特性
- Page Object（PO）模式 + 组件化（Page Components）
- 显式等待策略（WebDriverWait），避免隐式等待冲突
- 线程安全：ThreadLocal<WebDriver> 支持方法级并行
- Allure 报告：失败截图、步骤、标签、附件
- 元素查找失败重试机制：自动处理 StaleElementReferenceException，次数可配置（`elementRetryCount`）
- 用例失败重试机制：全局 IRetryAnalyzer，通过 AnnotationTransformer 自动注入，次数可配置（`testRetryCount`）
- 多浏览器并行：Chrome / Firefox / Edge
- 数据驱动：CSV DataProvider
- 灵活配置：`config.properties` + 命令行覆盖
- 统一断言封装：AssertHelper（自动记录步骤与期望/实际）

## 快速开始

```bash
# 运行全量测试（默认并行，Chrome + Firefox + Edge）
mvn clean test

# 无头模式
mvn clean test -Dheadless=true

# 指定浏览器 + 重试次数
mvn clean test -Dbrowser=chrome -Dheadless=true -DtestRetryCount=2

# 生成并查看 Allure 报告（需先运行测试）
mvn allure:serve
```


## 目录

1. [PO 模式（Page Object Pattern）](#1-po-模式)
2. [DDT 数据驱动（Data-Driven Testing）](#2-ddt-数据驱动)
3. [等待机制](#3-等待机制)
4. [TestNG 集成](#4-testng-集成)
5. [Allure 集成](#5-allure-集成)
6. [配置加载机制](#6-配置加载机制)
7. [监听机制](#7-监听机制)
8. [查找元素失败重试机制](#8-查找元素失败重试机制)
9. [用例失败重试机制](#9-用例失败重试机制)
10. [DriverFactory 驱动工厂](#10-driverfactory-驱动工厂)
11. [整体架构图](#11-整体架构图)

---

## 1. PO 模式

### 核心思想

将页面的**元素定位**和**操作行为**封装到独立的 Page 类中，测试代码只调用页面方法，不直接操作 WebDriver。

### 层次结构

```
BasePage（抽象基类）
├── LoginPage
├── InventoryPage
├── CartPage
├── CheckoutStepOnePage
├── CheckoutStepTwoPage
├── CheckoutCompletePage
└── components/
    ├── HeaderComponent
    └── InventoryItemComponent
```

### BasePage — 所有页面的父类

**文件：** `src/main/java/framework/pages/BasePage.java`

```java
public abstract class BasePage {
    protected final WebDriver driver;
    protected final Waits waits;

    protected BasePage() {
        this.driver = DriverFactory.getDriver();  // 从 ThreadLocal 取当前线程的 driver
        this.waits = new Waits(driver, ConfigLoader.get().explicitWaitSec);
    }

    // 查找+操作原子化，彻底消除 Stale 窗口期
    protected <T> T withRetry(By locator, Function<WebElement, T> action) { ... }

    protected void click(By locator) { ... }
    protected void type(By locator, String text) { ... }
    protected String getText(By locator) { ... }
}
```

**关键设计：**
- `driver` 和 `waits` 是 `protected`，子类可直接使用
- 所有底层操作（click/type/getText）统一在 BasePage 定义，子类不重复写
- 构造器无参数，driver 通过 `DriverFactory.getDriver()` 从 ThreadLocal 获取，天然支持并发

### 具体页面 — LoginPage

**文件：** `src/main/java/framework/pages/LoginPage.java`

```java
public class LoginPage extends BasePage {
    private final By username = By.id("user-name");
    private final By password = By.id("password");
    private final By loginBtn  = By.id("login-button");
    private final By errorMsg  = By.cssSelector("h3[data-test='error']");

    @Step("登录，用户名：{user}, 密码：{pass}")
    public InventoryPage loginAs(String user, String pass) {
        type(username, user);
        type(password, pass);
        click(loginBtn);
        return new InventoryPage();  // 登录成功后返回商品页
    }
}
```

**关键设计：**
- 定位器用 `private final By` 声明，集中管理，改一处全生效
- 方法返回值是**下一个页面对象**，支持链式调用：`login.loginAs(...).addToCart(...)`
- `@Step` 注解让操作步骤自动出现在 Allure 报告中

### 组件化 — HeaderComponent

**文件：** `src/main/java/framework/pages/components/HeaderComponent.java`

多个页面共享同一个 Header，抽成 Component 避免重复定义。`InventoryPage` 通过 `header()` 方法返回该组件：

```java
public HeaderComponent header() { return new HeaderComponent(); }
public CartPage openCart() {
    header().openCart();
    return new CartPage();
}
```

### PO 模式优点总结

| 优点 | 说明 |
|------|------|
| 可维护性 | 页面元素变更只改 Page 类，测试代码不动 |
| 可读性 | 测试代码读起来像业务流程，不是 Selenium API |
| 复用性 | 同一页面操作在多个测试中共享 |
| 链式调用 | 方法返回页面对象，流程清晰 |

---

## 2. DDT 数据驱动

### 核心思想

将测试数据与测试逻辑分离，同一个测试方法用不同数据集执行多次。

### CSV 数据文件

**文件：** `src/test/resources/testdata/users.csv`

```csv
username,password,role,expectedError
standard_user,secret_sauce,standard,
locked_out_user,secret_sauce,locked,Epic sadface: Sorry, this user has been locked out.
```

### CsvUtils — CSV 读取工具

**文件：** `src/main/java/framework/utils/CsvUtils.java`

```java
public class CsvUtils {
    public static List<CSVRecord> read(String resourcePath) {
        Reader in = new InputStreamReader(
            CsvUtils.class.getClassLoader().getResourceAsStream(resourcePath)
        );
        return new ArrayList<>(
            CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in).getRecords()
        );
    }
}
```

- `withFirstRecordAsHeader()` 让第一行作为列名，通过 `r.get("username")` 按名取值
- 从 classpath 读取，路径相对于 `src/test/resources/`

### @DataProvider — TestNG 数据提供者

**文件：** `src/test/java/tests/LoginTests.java`

```java
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

@Test(dataProvider = "users")
public void loginMatrix(String user, String pass, String role, String expectedError) {
    // CSV 有 N 行 → 此方法执行 N 次，每次参数不同
}
```

**执行流程：**
1. TestNG 先调用 `@DataProvider` 方法，得到 `Object[][]`
2. 对每一行数据，独立执行一次 `@Test` 方法
3. Allure 报告中每次执行单独展示，参数可见

### DDT 扩展方式

除 CSV 外，Jackson 依赖已引入，可直接支持 JSON 数据源：

```java
ObjectMapper mapper = new ObjectMapper();
List<UserData> users = mapper.readValue(
    getClass().getResourceAsStream("/testdata/users.json"),
    new TypeReference<List<UserData>>() {}
);
```

---


## 3. 等待机制

### 为什么不用隐式等待

隐式等待是全局的，作用在每次 `findElement` 上；显式等待内部也会反复调用 `findElement`。两者同时生效时等待时间叠加，行为不可预测。

此外隐式等待只能等"元素存在于 DOM"，无法等待可见、可点击等具体状态。元素已在 DOM 但被遮挡或 disabled 时，隐式等待直接返回引用，操作就报错。

因此本项目 `implicitWaitSec=0` 完全禁用隐式等待，统一用显式等待。

### 三种等待对比

| 类型 | 实现 | 本项目 |
|------|------|--------|
| 隐式等待 | `driver.manage().timeouts().implicitlyWait()` | **禁用**（与显式等待混用时间叠加，行为不可预测） |
| 显式等待 | `WebDriverWait` + `ExpectedConditions` | 主要方式 |
| 页面加载超时 | `pageLoadTimeout` | 兜底超时，防止页面长时间无响应卡住测试 |

### Waits 封装

**文件：** `src/main/java/framework/utils/Waits.java`

```java
public class Waits {
    private final WebDriverWait wait;

    public Waits(WebDriver driver, int seconds) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    public WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement presence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public void invisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public void textToBePresentInElement(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }
}
```

`BasePage` 构造时注入，超时秒数来自配置：

```java
this.waits = new Waits(driver, ConfigLoader.get().explicitWaitSec);
```

### pageLoadTimeout 的作用

`pageLoadTimeout` 在 `DriverFactory` 创建 driver 时设置：

```java
// src/main/java/framework/driver/DriverFactory.java
driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(cfg.pageLoadTimeoutSec));
```

它限制 `driver.get(url)` 的最长等待时间。超时后抛 `TimeoutException`，防止页面无响应时测试永久阻塞。与显式等待互不干扰——显式等待管元素级别的条件，`pageLoadTimeout` 管整页加载。

### 配置项

```properties
# src/main/resources/config.properties
implicitWaitSec=0        # 禁用
explicitWaitSec=10       # 元素级显式等待
pageLoadTimeoutSec=30    # 页面加载兜底超时
```

命令行可覆盖：`mvn test -DexplicitWaitSec=15`

---

## 4. TestNG 集成

### BaseTest — 测试生命周期管理

**文件：** `src/test/java/tests/BaseTest.java`

```java
public class BaseTest {
    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional String browser) {
        if (browser != null) System.setProperty("browser", browser);
        driver = DriverFactory.getDriver();
        openBaseUrl();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
```

- `alwaysRun = true`：测试失败时 tearDown 也执行，防止浏览器进程泄漏
- `@Optional`：browser 参数可选，不传时使用配置文件默认值

### TestNG XML — 并发配置

**文件：** `src/test/resources/testng-parallel.xml`

```xml
<suite name="SauceDemo Parallel Suite" parallel="tests" thread-count="3">
  <listeners>
    <listener class-name="framework.listeners.AllureTestListener"/>
    <listener class-name="framework.listeners.AnnotationTransformer"/>
  </listeners>
  <test name="Chrome">
    <parameter name="browser" value="chrome"/>
    <packages><package name="tests"/></packages>
  </test>
  <!-- Firefox, Edge 同理 -->
</suite>
```

**并发模式说明：**

| parallel 值 | 含义 |
|-------------|------|
| `tests` | 每个 `<test>` 标签并发（本项目：3浏览器同时跑） |
| `classes` | 每个测试类并发 |
| `methods` | 每个测试方法并发（最细粒度） |

### TestNG 注解速查

| 注解 | 作用 |
|------|------|
| `@Test` | 标记测试方法 |
| `@BeforeMethod` | 每个测试方法前执行 |
| `@AfterMethod` | 每个测试方法后执行 |
| `@DataProvider` | 提供测试数据 |
| `@Parameters` | 从 XML 注入参数 |
| `@Optional` | 参数可选，有默认值 |

### Maven 运行命令

```bash
mvn clean test                                                          # 默认
mvn clean test -Dheadless=true                                          # 无头
mvn clean test -Dbrowser=firefox                                        # 指定浏览器
mvn clean test -DsuiteXmlFile=src/test/resources/testng-parallel.xml   # 并发跨浏览器
mvn clean test -Dgrid.enabled=true -Dgrid.url=http://localhost:4444     # Selenium Grid
mvn allure:serve                                                        # 查看报告
```

---

## 5. Allure 集成

### 报告层级注解

```java
@Epic("SauceDemo")       // 最高层：产品
@Feature("登录")          // 中间层：功能模块
public class LoginTests extends BaseTest {

    @Story("不同用户登录路径")  // 最低层：用户故事
    @Test(description = "按用户类型验证登录结果")
    public void loginMatrix(...) { ... }
}
```

层级：`Epic > Feature > Story > Test`

### @Step — 步骤追踪

```java
@Step("登录，用户名：{user}, 密码：{pass}")
public InventoryPage loginAs(String user, String pass) { ... }
```

`{user}` 语法自动将参数值插入步骤名称，报告中可见实际传入的值。

### AssertHelper — 断言与报告联动

**文件：** `src/main/java/framework/assertions/AssertHelper.java`

```java
public static void assertContains(String actual, String expectedSubstr, String message) {
    Allure.step("断言包含: " + message + " | 期望='" + expectedSubstr + "' 实际='" + actual + "'");
    Assert.assertTrue(actual != null && actual.contains(expectedSubstr), message);
}
```

断言失败时，Allure 报告直接显示期望值和实际值，不需要翻日志。

### AllureTestListener — 失败自动截图

**文件：** `src/main/java/framework/listeners/AllureTestListener.java`

测试失败时自动附加两类证据：

| 附件 | 条件 | 用途 |
|------|------|------|
| 失败截图（PNG） | `screenshotOnFail=true` 且 driver 非 null | 直观看到失败页面 |
| PageSource（HTML） | driver 非 null | 分析 DOM 结构，辅助定位元素问题 |

截图失败时降级为文本附件记录异常信息，不影响主流程。

---

## 6. 配置加载机制

### 三层优先级（高 → 低）

```
命令行 -D 参数
    ↓ 覆盖
pom.xml <properties> 默认值
    ↓ 覆盖
config.properties 文件
```

### 值的注入链路

```
pom.xml <properties>
  <browser>chrome</browser>          ← Maven 属性默认值
        ↓
maven-surefire-plugin <systemPropertyVariables>
  <browser>${browser}</browser>      ← 转成 JVM -D 参数
        ↓
ConfigLoader.java
  System.getProperty("browser", p.getProperty("browser"))
  //                  ↑ JVM 系统属性      ↑ config.properties 兜底
```

关键代码位置：
- `pom.xml:91-97` — surefire 将 Maven 属性注入为 JVM 系统属性
- `src/main/java/framework/config/ConfigLoader.java:15-23` — 读取顺序：系统属性 > 配置文件

### 支持的配置项

| 属性 | pom.xml 默认值 | 说明 |
|------|---------------|------|
| `browser` | `chrome` | 浏览器类型 chrome/firefox/edge |
| `headless` | `false` | 无头模式 |
| `baseUrl` | `https://www.saucedemo.com` | 测试目标地址 |
| `retryCount` | `1` | 失败重试次数 |
| `threads` | `3` | 并行线程数 |
| `explicitWaitSec` | 见 config.properties | 显式等待秒数 |
| `pageLoadTimeoutSec` | 见 config.properties | 页面加载超时秒数 |
| `screenshotOnFail` | 见 config.properties | 失败截图开关 |
| `grid.enabled` | 见 config.properties | Selenium Grid 开关 |
| `grid.url` | 见 config.properties | Grid Hub 地址 |

---

## 7. 监听机制

`framework/listeners/` 下三个类构成测试生命周期拦截层，各司其职。

### 三者协作时序

```
Suite 启动
  └── AnnotationTransformer.transform()  ← 给每个 @Test 注入 RetryAnalyzer

测试执行
  └── @Test 方法运行
        ├── 成功 → AllureTestListener.onTestSuccess()（空实现）
        └── 失败 → AllureTestListener.onTestFailure()  ← 截图 + PageSource
                  └── RetryAnalyzer.retry()
                        ├── true  → 重新执行 @Test
                        └── false → 标记 FAILED → @AfterMethod → quitDriver()
```

### 相关文件

| 文件 | 职责 |
|------|------|
| `framework/listeners/AnnotationTransformer.java` | Suite 启动时给每个 @Test 注入 RetryAnalyzer |
| `framework/listeners/RetryAnalyzer.java` | 失败重试次数计数与判断 |
| `framework/listeners/AllureTestListener.java` | 失败时采集截图和 PageSource 附件 |
| `src/test/resources/testng-parallel.xml` | 注册三个 listener 的入口 |

---

## 8. 查找元素失败重试机制

### 查找+操作原子化

**文件：** `src/main/java/framework/pages/BasePage.java`

```java
protected <T> T withRetry(By locator, Function<WebElement, T> action) {
    int retries = ConfigLoader.get().elementRetryCount;
    for (int i = 0; i < retries; i++) {
        try {
            return action.apply(waits.visible(locator));  // 每次重试都重新等待+查找
        } catch (StaleElementReferenceException e) {
            if (i == retries - 1) throw e;
        }
    }
    throw new RuntimeException("Action failed after retries: " + locator);
}

protected void click(By locator) {
    withRetry(locator, el -> { el.click(); return null; });
}

protected void type(By locator, String text) {
    withRetry(locator, el -> { el.clear(); el.sendKeys(text); return null; });
}

protected String getText(By locator) {
    return withRetry(locator, WebElement::getText);
}
```

次数通过 `elementRetryCount` 配置，命令行可覆盖：`-DelementRetryCount=3`

---

## 9. 用例失败重试机制

### 组件关系

```
testng-parallel.xml
  └── listener: AnnotationTransformer
        └── transform() 在运行时被 TestNG 调用
              └── 对每个 @Test 方法自动注入 retryAnalyzer = RetryAnalyzer.class
                    └── RetryAnalyzer.retry()
```

### RetryAnalyzer 核心逻辑

```java
// count++ < max：先比较再自增，实际重试次数 = max 次
public boolean retry(ITestResult result) {
    return count++ < max;
}
```

| count | max=2 | 返回值 | 行为 |
|-------|-------|--------|------|
| 0 | 2 | true | 第1次重试 |
| 1 | 2 | true | 第2次重试 |
| 2 | 2 | false | 停止，标记 FAILED |

### 为什么用 AnnotationTransformer 而不是 @Test(retryAnalyzer=...)

`AnnotationTransformer` 在运行时统一给所有测试方法注入，一处配置全局生效，避免每个测试类重复声明。每个测试实例独立持有一个 `RetryAnalyzer` 实例，`count` 不跨测试共享。

---

## 10. DriverFactory 驱动工厂

**文件：** `src/main/java/framework/driver/DriverFactory.java`

### ThreadLocal — 线程隔离

```java
private static final ThreadLocal<WebDriver> TL = new ThreadLocal<>();
```

`ThreadLocal<T>` 为每个线程维护一份独立的变量副本：

```
Thread-1 → TL.get() → ChromeDriver@A
Thread-2 → TL.get() → ChromeDriver@B   ← 完全隔离，互不影响
Thread-3 → TL.get() → ChromeDriver@C
```

`getDriver()` 采用懒加载：线程第一次调用时才创建，避免资源浪费。

### 本地 vs 远程统一入口

通过 `gridEnabled` 标志，同一套代码支持两种执行模式：

| 模式 | 场景 | 实现 |
|------|------|------|
| 本地 | 开发调试 | `ChromeDriver` / `FirefoxDriver` / `EdgeDriver` |
| 远程 | CI/Selenium Grid | `RemoteWebDriver` + Grid URL |

### quitDriver() — 防内存泄漏

```java
d.quit();
TL.remove();  // 必须调用，否则线程池复用时旧引用残留导致内存泄漏
```

### 整体数据流

```
测试方法调用 getDriver()
    → TL.get() 为 null（首次）
    → createDriver() 读取 Config
    → 按 browser 类型构造 Options
    → buildDriver() 决定本地/远程
    → 设置超时 + 最大化窗口
    → TL.set(driver) 绑定到当前线程
    → 返回 driver
```

---

## 11. 整体架构图

```
┌─────────────────────────────────────────────────────────┐
│                    测试层 (tests/)                        │
│  BaseTest          LoginTests    CartAndCheckoutTests    │
│  @Before/@After    @DataProvider  @Epic/@Feature/@Story  │
└──────────────────────────┬──────────────────────────────┘
                           │ 调用
┌──────────────────────────▼──────────────────────────────┐
│                   页面层 (pages/)                         │
│  BasePage（withRetry + Waits）                            │
│  ├── LoginPage / InventoryPage / CartPage / ...          │
│  └── components/ HeaderComponent / InventoryItemComponent│
└──────────────────────────┬──────────────────────────────┘
                           │ 依赖
┌──────────────────────────▼──────────────────────────────┐
│                   基础设施层 (framework/)                  │
│  DriverFactory（ThreadLocal）  ConfigLoader（Properties） │
│  Waits（显式等待）              CsvUtils（DDT数据读取）    │
│  AssertHelper（断言+Allure）    RetryAnalyzer（重试）      │
│  AllureTestListener（截图）     AnnotationTransformer     │
└─────────────────────────────────────────────────────────┘
```

### 数据流

```
CSV文件 → CsvUtils.read() → @DataProvider → @Test(dataProvider)
                                                    │
                                              LoginPage.loginAs()
                                                    │
                                          BasePage.withRetry()  ←── 原子化重试
                                                    │
                                          waits.visible()       ←── 显式等待
                                                    │
                                          action.apply(el)      ←── 操作元素
```

### 并发模型

```
TestNG Suite (parallel="tests", thread-count=3)
├── Thread-1: Chrome  → ThreadLocal<WebDriver>[Chrome]
├── Thread-2: Firefox → ThreadLocal<WebDriver>[Firefox]
└── Thread-3: Edge    → ThreadLocal<WebDriver>[Edge]
```

每个线程独立持有 WebDriver，互不干扰，测试结束后 `TL.remove()` 清理。
