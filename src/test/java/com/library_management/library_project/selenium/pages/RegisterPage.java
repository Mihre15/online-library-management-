package com.library_management.library_project.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RegisterPage extends BasePage {

    @FindBy(css = "[data-testid='register-fullname']")
    private WebElement fullNameInput;

    @FindBy(css = "[data-testid='register-email']")
    private WebElement emailInput;

    @FindBy(css = "[data-testid='register-password']")
    private WebElement passwordInput;

    @FindBy(css = "[data-testid='register-confirm-password']")
    private WebElement confirmPasswordInput;

    @FindBy(css = "[data-testid='register-submit']")
    private WebElement submitButton;

    @FindBy(css = "[data-testid='register-error']")
    private WebElement errorBanner;

    @FindBy(xpath = "//a[contains(@href, '/login')]")
    private WebElement signInLink;

    @FindBy(xpath = "//h1[contains(text(), 'Create an account')]")
    private WebElement pageTitle;

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/register");
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(fullNameInput) && isElementDisplayed(emailInput);
    }

    public void enterFullName(String name) {
        type(fullNameInput, name);
    }

    public void enterEmail(String email) {
        type(emailInput, email);
    }

    public void enterPassword(String password) {
        type(passwordInput, password);
    }

    public void enterConfirmPassword(String confirmPassword) {
        type(confirmPasswordInput, confirmPassword);
    }

    public void clickSubmit() {
        click(submitButton);
    }

    public void register(String fullName, String email, String password, String confirmPassword) {
        enterFullName(fullName);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
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
        return isElementPresent(By.cssSelector("[data-testid='register-submit'] span.animate-spin"));
    }

    public void clickSignInLink() {
        waitForVisibility(signInLink);
        try {
            click(signInLink);
        } catch (Exception e) {
            // Only fall back to a JS click if the WebDriver click itself failed — checking the
            // URL right after a successful click races the client-side navigation and can fire
            // a second click at an element that has already navigated away.
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", signInLink);
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return wait.until(d -> isElementPresent(By.cssSelector("[data-testid='register-error']")) && isElementDisplayed(errorBanner));
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        return getText(errorBanner);
    }

    public boolean isPasswordMismatchWarningDisplayed() {
        return isElementPresent(By.xpath("//p[contains(text(), 'Passwords do not match')]"));
    }

    public String getPageHeading() {
        return getText(pageTitle);
    }
}
