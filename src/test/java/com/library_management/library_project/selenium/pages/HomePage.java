package com.library_management.library_project.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePage {

    @FindBy(xpath = "//p[contains(text(), 'One moment')]")
    private WebElement loadingText;

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/");
    }

    public boolean isRedirectedToLogin() {
        return waitForUrlContains("/login");
    }

    public boolean isRedirectedToSearch() {
        return waitForUrlContains("/search");
    }
}
