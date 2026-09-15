package com.library_management.library_project.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class LoansPage extends BasePage {

    @FindBy(xpath = "//h1[contains(text(), 'My loans')]")
    private WebElement pageHeading;

    @FindBy(css = "[data-testid='loans-error']")
    private WebElement errorBanner;

    @FindBy(xpath = "//p[contains(text(), 'A quiet card')]")
    private WebElement emptyStateNotice;

    @FindBy(xpath = "//a[contains(text(), 'Browse the shelves')]")
    private WebElement browseShelvesLink;

    public LoansPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/loans");
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(pageHeading);
    }

    public String getHeadingText() {
        return getText(pageHeading);
    }

    public int getLoanRowCount() {
        return driver.findElements(By.cssSelector("li[data-testid^='loan-row-']")).size();
    }

    public boolean isLoanRowPresent(long loanId) {
        return isElementPresent(By.cssSelector("[data-testid='loan-row-" + loanId + "']"));
    }

    public String getLoanStatus(long loanId) {
        return getText(driver.findElement(By.cssSelector("[data-testid='loan-status-" + loanId + "']")));
    }

    public boolean isReturnButtonVisible(long loanId) {
        return isElementPresent(By.cssSelector("[data-testid='return-button-" + loanId + "']"));
    }

    public void clickReturnButton(long loanId) {
        click(driver.findElement(By.cssSelector("[data-testid='return-button-" + loanId + "']")));
    }

    public boolean isFineDisplayed(long loanId) {
        return isElementPresent(By.cssSelector("[data-testid='fine-amount-" + loanId + "']"));
    }

    public String getFineText(long loanId) {
        return getText(driver.findElement(By.cssSelector("[data-testid='fine-amount-" + loanId + "']")));
    }

    public boolean isEmptyStateDisplayed() {
        return isElementPresent(By.xpath("//p[contains(text(), 'A quiet card')]"));
    }

    public void clickBrowseShelves() {
        click(browseShelvesLink);
    }

    public boolean isErrorDisplayed() {
        return isElementPresent(By.cssSelector("[data-testid='loans-error']")) && isElementDisplayed(errorBanner);
    }
}
