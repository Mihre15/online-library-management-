package com.library_management.library_project.selenium.pages.admin;

import com.library_management.library_project.selenium.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AdminDashboardPage extends BasePage {

    @FindBy(xpath = "//h1[contains(text(), 'Library Admin Dashboard')]")
    private WebElement pageHeading;

    @FindBy(css = "a[href='/admin/books']")
    private WebElement manageBooksCard;

    @FindBy(css = "a[href='/admin/members']")
    private WebElement manageMembersCard;

    @FindBy(css = "a[href='/admin/loans']")
    private WebElement manageLoansCard;

    public AdminDashboardPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/admin");
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(pageHeading) && isElementDisplayed(manageBooksCard);
    }

    public String getHeadingText() {
        return getText(pageHeading);
    }

    public void clickManageBooks() {
        click(manageBooksCard);
    }

    public void clickManageMembers() {
        click(manageMembersCard);
    }

    public void clickManageLoans() {
        click(manageLoansCard);
    }
}
