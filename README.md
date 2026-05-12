# saucedemo-automation

Java + TestNG + Selenium + Allure 的 Web 自动化工程，用于覆盖 https://www.saucedemo.com 的核心/全量用例。

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
