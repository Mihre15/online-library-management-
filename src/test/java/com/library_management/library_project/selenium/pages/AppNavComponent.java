package com.library_management.library_project.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AppNavComponent extends BasePage {

    @FindBy(xpath = "//header//a[contains(@class, 'group')]")
    private WebElement brandLink;

    @FindBy(xpath = "//header//nav//a[contains(@href, '/search')]")
    private WebElement catalogLink;

    @FindBy(xpath = "//header//nav//a[contains(@href, '/loans')]")
    private WebElement loansLink;

    @FindBy(xpath = "//header//nav//a[contains(@href, '/admin')]")
    private WebElement adminLink;

    @FindBy(xpath = "//header//nav//button[contains(text(), 'Sign out')]")
    private WebElement signOutButton;

    public AppNavComponent(WebDriver driver) {
        super(driver);
    }

    public boolean isNavVisible() {
        return isElementPresent(By.tagName("header"));
    }

    public boolean isCatalogLinkVisible() {
        return isElementDisplayed(catalogLink);
    }

    public boolean isLoansLinkVisible() {
        return isElementDisplayed(loansLink);
    }

    public boolean isAdminLinkVisible() {
        return isElementPresent(By.xpath("//header//nav//a[contains(@href, '/admin')]")) && isElementDisplayed(adminLink);
    }

    public boolean isSignOutButtonVisible() {
        return isElementDisplayed(signOutButton);
    }

    public void clickBrand() {
        click(brandLink);
    }

    public void clickCatalog() {
        click(catalogLink);
    }

    public void clickLoans() {
        click(loansLink);
    }

    public void clickAdmin() {
        click(adminLink);
    }

    public void clickSignOut() {
        click(signOutButton);
    }
}
