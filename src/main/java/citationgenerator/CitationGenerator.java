package citationgenerator;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CitationGenerator {
    private final WebDriver webDriver;
    public static final String APA = "American Psychological Association 7th Edition";
    public static final String MLA = "Modern Language Association 9th edition";
    public static final String CMS = "Chicago Manual of Style 17th edition (full note)";
    WebDriverWait webDriverWait;
    public CitationGenerator(String style) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        webDriver = new ChromeDriver(options);
        webDriver.get("https://bibify.org");
        webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
        setCitationStyle(style);
    }
    public String retrieveCitation(String link) {
        WebElement input = webDriver.findElement(By.id("input-11"));
        input.sendKeys(link);
        try {
            Thread.sleep(3250);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WebElement foundCitation = webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.className("csl-entry")));
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].value=''", input);

        String citation = foundCitation.getAttribute("innerHTML").replaceAll("&amp;","&").replace("<i>","*").replace("</i>","*");
        StringBuilder formattedCitation = new StringBuilder(citation).insert(citation.length(), ">").insert(citation.indexOf("http"), "<");
        return formattedCitation.toString();
    }
    private void setCitationStyle(String style) {
        webDriver.findElements(By.xpath("//div[@tabindex='0' and @aria-selected='false' and @role='tab' and contains(@class, 'v-tab')]")).get(2).click();
        webDriver.findElements(By.xpath("//div[@class='v-text-field__slot']")).get(1).findElement(By.tagName("input")).sendKeys(style);
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        webDriver.findElement(By.xpath("//div[@class='px-2 py-2 style-card v-card v-card--link v-sheet theme--light elevation-0']")).click();
    }
    public void quitDriver() {
        webDriver.quit();
    }
}