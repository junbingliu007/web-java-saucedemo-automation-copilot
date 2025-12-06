# saucedemo-automation

Java + TestNG + Selenium + Allure 的 Web 自动化工程，用于覆盖 https://www.saucedemo.com 的核心/全量用例。

## 关键特性
- Page Object（PO）模式 + 组件化（Page Components）
- 显式等待策略（WebDriverWait），避免隐式等待冲突
- 线程安全：ThreadLocal<WebDriver> 支持方法级并行
- Allure 报告：失败截图、步骤、标签、附件
- 重试机制：全局 IRetryAnalyzer，可配置次数
- 多浏览器并行：Chrome / Firefox / Edge
- 数据驱动：CSV DataProvider
- 灵活配置：`config.properties` + 命令行覆盖
- 统一断言封装：AssertHelper（自动记录步骤与期望/实际）

## 快速开始
```bash
mvn clean
```bash
mvn clean test
```bash
mvn clean test -DsuiteXmlFile=src/test/resources/testng-parallel.xml -Dheadless=true

# 生成并查看 Allure 报告：
# (需安装 allure 命令行)
```bash
allure serve target/allure-results

# 生成Allure报告
mvn allure:report

# 打开Allure报告
mvn allure:serve


命令行覆盖示例：
```bash
mvn clean test -DsuiteXmlFile=src/test/resources/testng-parallel.xml   -Dbrowser=firefox -DretryCount=2 -DbaseUrl=https://www.saucedemo.com
```