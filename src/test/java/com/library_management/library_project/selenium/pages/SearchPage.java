package com.library_management.library_project.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class SearchPage extends BasePage {

    @FindBy(css = "[data-testid='search-input']")
    private WebElement searchInput;

    @FindBy(xpath = "//form//button[contains(text(), 'Search')]")
    private WebElement searchButton;

    @FindBy(css = "[data-testid='search-empty']")
    private WebElement emptyState;

    @FindBy(css = "[data-testid='search-error']")
    private WebElement errorBanner;

    @FindBy(xpath = "//h1[contains(text(), 'Currently Available Books')]")
    private WebElement pageHeading;

    @FindBy(css = "li[data-testid^='book-row-']")
    private List<WebElement> bookRows;

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/search");
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(searchInput) && isElementDisplayed(searchButton);
    }

    public void search(String query) {
        type(searchInput, query);
        click(searchButton);
    }

    public int getBookCount() {
        return driver.findElements(By.cssSelector("li[data-testid^='book-row-']")).size();
    }

    public boolean isBookRowPresent(long bookId) {
        return isElementPresent(By.cssSelector("[data-testid='book-row-" + bookId + "']"));
    }

    public String getAvailabilityBadgeText(long bookId) {
        return getText(driver.findElement(By.cssSelector("[data-testid='availability-badge-" + bookId + "']")));
    }

    public void clickBook(long bookId) {
        click(driver.findElement(By.cssSelector("[data-testid='book-row-" + bookId + "'] a")));
    }

    public boolean isEmptyStateDisplayed() {
        return isElementPresent(By.cssSelector("[data-testid='search-empty']")) && isElementDisplayed(emptyState);
    }

    public boolean isErrorDisplayed() {
        return isElementPresent(By.cssSelector("[data-testid='search-error']")) && isElementDisplayed(errorBanner);
    }

    public void selectShelf(String shelfName) {
        click(driver.findElement(By.xpath("//button[text()='" + shelfName + "']")));
    }

    public String getPageHeading() {
        return getText(pageHeading);
    }
}
