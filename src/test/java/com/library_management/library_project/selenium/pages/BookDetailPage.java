package com.library_management.library_project.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class BookDetailPage extends BasePage {

    @FindBy(xpath = "//a[contains(@href, '/search') and contains(text(), 'Back to the shelves')]")
    private WebElement backToShelvesLink;

    @FindBy(tagName = "h1")
    private WebElement bookTitle;

    @FindBy(css = "[data-testid='borrow-button']")
    private WebElement borrowButton;

    @FindBy(css = "[data-testid='borrow-success']")
    private WebElement successBanner;

    @FindBy(css = "[data-testid='borrow-denied']")
    private WebElement deniedBanner;

    @FindBy(css = "[data-testid='borrow-error']")
    private WebElement errorBanner;

    @FindBy(xpath = "//a[contains(@href, '/loans') and contains(text(), 'See it on My loans')]")
    private WebElement myLoansLink;

    public BookDetailPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl, long bookId) {
        driver.get(baseUrl + "/books/" + bookId);
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(backToShelvesLink);
    }

    public String getBookTitle() {
        return getText(bookTitle);
    }

    public boolean isBorrowButtonVisible() {
        return isElementPresent(By.cssSelector("[data-testid='borrow-button']")) && isElementDisplayed(borrowButton);
    }

    public boolean isBorrowButtonEnabled() {
        return borrowButton.isEnabled();
    }

    public String getBorrowButtonText() {
        return getText(borrowButton);
    }

    public void clickBorrow() {
        click(borrowButton);
    }

    public boolean waitForBorrowButtonDisabled() {
        return waitForElementToBeDisabled(borrowButton);
    }

    public boolean isBorrowLoading() {
        return isElementPresent(By.cssSelector("[data-testid='borrow-button'] span.animate-spin"));
    }

    public void clickBackToShelves() {
        click(backToShelvesLink);
    }

    public boolean isSuccessBannerDisplayed() {
        return isElementPresent(By.cssSelector("[data-testid='borrow-success']")) && isElementDisplayed(successBanner);
    }

    public String getSuccessMessage() {
        return getText(successBanner);
    }

    public boolean isDeniedBannerDisplayed() {
        return isElementPresent(By.cssSelector("[data-testid='borrow-denied']")) && isElementDisplayed(deniedBanner);
    }

    public String getDeniedMessage() {
        return getText(deniedBanner);
    }

    public boolean isErrorBannerDisplayed() {
        return isElementPresent(By.cssSelector("[data-testid='borrow-error']")) && isElementDisplayed(errorBanner);
    }

    public String getErrorMessage() {
        return getText(errorBanner);
    }

    public void clickMyLoansLink() {
        click(myLoansLink);
    }
}
