package com.library_management.library_project.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    @FindBy(css = "[data-testid='login-email']")
    private WebElement emailInput;

    @FindBy(css = "[data-testid='login-password']")
    private WebElement passwordInput;

    @FindBy(css = "[data-testid='login-submit']")
    private WebElement submitButton;

    @FindBy(css = "[data-testid='login-error']")
    private WebElement errorBanner;

    @FindBy(xpath = "//a[contains(@href, '/register')]")
    private WebElement registerLink;

    @FindBy(xpath = "//h1[contains(text(), 'Sign in')]")
    private WebElement pageTitle;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/login");
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(emailInput) && isElementDisplayed(passwordInput);
    }

    public void enterEmail(String email) {
        type(emailInput, email);
    }

    public void enterPassword(String password) {
        type(passwordInput, password);
    }

    public void clickSubmit() {
        click(submitButton);
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSubmit();
    }

    public boolean isSubmitEnabled() {
        return submitButton.isEnabled();
    }

    public boolean waitForSubmitEnabled() {
        return waitForElementToBeEnabled(submitButton);
    }

    public boolean waitForSubmitDisabled() {
        return waitForElementToBeDisabled(submitButton);
    }

    public boolean isSubmitLoading() {
        return isElementPresent(By.cssSelector("[data-testid='login-submit'] span.animate-spin"));
    }

    public void clickRegisterLink() {
        waitForVisibility(registerLink);
        try {
            click(registerLink);
        } catch (Exception e) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", registerLink);
        }
        if (!driver.getCurrentUrl().contains("/register")) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", registerLink);
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return wait.until(d -> isElementPresent(By.cssSelector("[data-testid='login-error']")) && isElementDisplayed(errorBanner));
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        return getText(errorBanner);
    }

    public String getPageHeading() {
        return getText(pageTitle);
    }
}
