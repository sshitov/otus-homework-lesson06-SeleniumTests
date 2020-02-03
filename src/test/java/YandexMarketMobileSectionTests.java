import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class YandexMarketMobileSectionTests {

    public static final Logger logger = LogManager.getLogger(YandexMarketMobileSectionTests.class.getName());
    protected static WebDriver driver;
    protected static WebDriverWait wait;
    protected String baseUrl = "https://market.yandex.ru/catalog--mobilnye-telefony/54726/list";

    @BeforeClass
    public static void setupDriver() {
        logger.debug("setup WebDriver");
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void createWebDriver(){
        logger.debug("Initialize webDriver and set headless");
        ChromeOptions options = new ChromeOptions().setHeadless(false);
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 10);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        // Precondition: Open site "Yandex" in Chrome browser (This step need for confirmation that I not a robot)
        driver.get("https://yandex.ru/");
    }

    @After
    public void closeWebDriver(){
        if (driver != null){
            driver.quit();
        }
    }

    @Test
    public void filteringMobileList() {

        // Precondition: Open "YandexMarket" -> "mobile phone" section
        driver.get(baseUrl);

        // 2. Filter by brand: Xiaomi / OPPO
        // 2.1. Open search field by brand
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(":nth-child(3) ._178jz2CyDL :nth-child(2) footer button"))).click();

        // 2.2. Find and select brand "Xiaomi"
        driver.findElement(By.cssSelector("[id*=suggester]")).sendKeys("Xiaomi");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[name*='Xiaomi']")));
        driver.findElement(By.cssSelector(":nth-child(3) ._178jz2CyDL :nth-child(2) fieldset span")).click();

        // 2.3. Find and select brand "OPPO"
        driver.findElement(By.cssSelector("[id*=suggester]")).clear();
        driver.findElement(By.cssSelector("[id*=suggester]")).sendKeys("OPPO");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[name*='OPPO']")));
        driver.findElement(By.cssSelector(":nth-child(3) ._178jz2CyDL :nth-child(2) fieldset span")).click();

        // 2.4. Verifying that checkboxes is selected
        driver.findElement(By.cssSelector(":nth-child(3) ._178jz2CyDL :nth-child(2) footer button")).click();
        // 2.4.1. use the "isSelected" method
        boolean xiaomiCheckbox = driver.findElement(By.cssSelector("[name*='Xiaomi']")).isSelected();
        Assert.assertTrue(xiaomiCheckbox);
        // 2.4.2. use attribute verifying
        String oppoCheckbox = driver.findElement(By.cssSelector("[name*='OPPO']")).getAttribute("checked");
        Assert.assertEquals("true", oppoCheckbox);

        // 2.5. Waiting for list refresh
        WebElement mobileList = driver.findElement(By.cssSelector(".snippet-list_js_inited.b-spy-init_js_inited"));
        wait.until(ExpectedConditions.stalenessOf(mobileList));

        // 2.6. Verifying that mobile list contains mobile with only select brands
        List<WebElement> mobileListElements = driver.findElements(By.cssSelector("[class='n-snippet-cell2__brand-name']"));
        // 2.6.1. Checking the list size
        Assert.assertEquals(48, mobileListElements.size());
        // 2.6.2. Checking the mobile phone brands
        for (WebElement mobileListElement : mobileListElements) {
            String mobileBrand = mobileListElement.getAttribute("innerText");
            if (!mobileBrand.equals("XIAOMI")){
                if(!mobileBrand.equals("OPPO")) {
                    Assert.fail();
                }
            }
        }

    }
    @Test
    public void sortMobileList() {

        //  Precondition: Open mobile page with selected brand
        driver.get(baseUrl + "?hid=91491&glfilter=7893318%3A7701962%2C6278641&glfilter=16816262%3A16816264&local-offers-first=0&onstock=1");

        // 3. Sort mobile phone list by asc
        // 3.1. Select price sort by ascending
        //driver.findElement(By.cssSelector(".n-filter-block_pos_left :nth-child(3) a")).click();
        driver.findElement(By.linkText("по цене")).click();

        // 3.2. Waiting for list refresh
        WebElement mobileList = driver.findElement(By.cssSelector(".snippet-list_js_inited.b-spy-init_js_inited"));
        wait.until(ExpectedConditions.stalenessOf(mobileList));

        // 3.3. Verifying that mobile list is sort by asc
        // 3.3.1. Get all prices in the list on the first page
        List<WebElement> mobileListElements = driver.findElements(By.cssSelector(".snippet-list_js_inited [class*='n-snippet-cell2__main-price-wrapper'] :first-child [class='price']"));

        // 3.3.2. Creating an array for parse prices
        ArrayList<Integer> pricesList = new ArrayList<>();

        // 3.3.3. Parse list prices and add result to the integer array
        for (WebElement mobileListElement : mobileListElements) {
            String mobilePrice = mobileListElement.getAttribute("innerText");
            String deleteSpaces = mobilePrice.replaceAll(" ", "");
            String cutCurrency = deleteSpaces.substring(0, deleteSpaces.length() - 2);
            int price = Integer.parseInt(cutCurrency);
            pricesList.add(price);
        }

        // 3.3.4. Verifying that  prices were added by ascending
        for (int i = 1; i < pricesList.size(); i++) {
            if (pricesList.get(i-1) > pricesList.get(i)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void addMobileToComparisonList() {

        // 4. Add products to compare list
        // 4.1. Select first mobile phone Xiaomi
        // 4.1.1. get page with select Xiaomi brand and sort by asc
        driver.get(baseUrl + "?hid=91491&glfilter=7893318%3A7701962&glfilter=16816262%3A16816264&local-offers-first=0&onstock=1&how=aprice");
        // 4.1.2. Save first mobile phone title
        String firstXiaomiTitle = driver.findElement(By.cssSelector(":nth-child(1) > .n-snippet-cell2__header a")).getAttribute("title");
        // 4.1.3. Add first phone
        driver.findElement(By.cssSelector(":nth-child(1) > .n-snippet-cell2__hover > div > div > div")).click();
        // 4.1.4. Wait informer
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".popup-informer__details")));
        // 4.1.5. Verifying informer text
        String informerTextForXiaomi = driver.findElement(By.cssSelector(".popup-informer__title")).getText();
        Assert.assertEquals(String.format("Товар %s добавлен к сравнению", firstXiaomiTitle), informerTextForXiaomi);

        // 4.2. Select first mobile phone OPPO
        // 4.2.1. get page with select OPPO brand and sort by asc
        driver.get(baseUrl + "?hid=91491&glfilter=7893318%3A6278641&glfilter=16816262%3A16816264&local-offers-first=0&onstock=1&how=aprice");
        // 4.2.2. Save first mobile phone title
        String firstOPPOTitle = driver.findElement(By.cssSelector(":nth-child(1) > .n-snippet-cell2__header a")).getAttribute("title");
        // 4.2.3. Add first phone
        driver.findElement(By.cssSelector(":nth-child(1) > .n-snippet-cell2__hover > div > div > div")).click();
        // 4.2.4. Wait informer
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".popup-informer__details")));
        // 4.2.5. Verifying informer text
        String informerTextForOPPO = driver.findElement(By.cssSelector(".popup-informer__title")).getText();
        Assert.assertEquals(String.format("Товар %s добавлен к сравнению", firstOPPOTitle), informerTextForOPPO);

    }

    @Test
    public void comparisonListVerifying() {
        // Preconditions: open compare page
        driver.get("https://market.yandex.ru/compare/2GNNkuSRtS55xXHN5BCKszEUM67w?hid=91491&id=401338434&id=439495683");

        // 5. Comparison list verifying:
        // 5.1. Verifying that list contain only two product;
        List<WebElement> mobileCountToCompare = driver.findElements(By.cssSelector("[class*='n-compare-cell-draggable']"));
        int mobileToCompareLen = mobileCountToCompare.size();
        Assert.assertEquals(2, mobileToCompareLen);

        // 5.2. Select "all parameters"
        driver.findElement(By.cssSelector(".n-compare-show-controls__all span")).click();
        // 5.2.1. wait loading parameters
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".n-compare-table_js_inited > :nth-child(8) > .layout_context_compare.i-bem div div")));

        // 5.3. Verifying that list contains the same parameters;
        Assert.assertTrue(driver.findElement(By.cssSelector(":nth-child(8) :nth-child(3) .n-compare-row-name")).isDisplayed());

        // 5.4. Select "different parameters"
        driver.findElement(By.cssSelector(".n-compare-show-controls__diff span")).click();
        // 5.4.1. wait loading parameters
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".n-compare-table_js_inited > :nth-child(8) > .layout_context_compare.i-bem div div")));

        // 5.5. Verifying that list contain only different parameters to compare after select "show only different parameters";
        Assert.assertFalse(driver.findElement(By.cssSelector(":nth-child(8) :nth-child(3) .n-compare-row-name")).isDisplayed());

    }
}
