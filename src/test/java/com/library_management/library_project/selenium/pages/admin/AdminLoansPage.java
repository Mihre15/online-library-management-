package com.library_management.library_project.selenium.pages.admin;

import com.library_management.library_project.selenium.pages.BasePage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class AdminLoansPage extends BasePage {

    @FindBy(xpath = "//h1[contains(text(), 'Loan Management')]")
    private WebElement pageHeading;

    @FindBy(xpath = "//a[contains(@href, '/admin') and contains(text(), 'Back to Admin')]")
    private WebElement backToAdminLink;

    @FindBy(css = "table tbody tr")
    private List<WebElement> loanRows;

    public AdminLoansPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/admin/loans");
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(pageHeading);
    }

    public String getHeadingText() {
        return getText(pageHeading);
    }

    public void filterByStatus(String status) {
        click(driver.findElement(By.xpath("//button[contains(text(), '" + status + "')]")));
    }

    public int getLoanRowCount() {
        return driver.findElements(By.cssSelector("table tbody tr")).size();
    }

    public boolean isConfirmReturnButtonPresent(String memberEmail) {
        return isElementPresent(By.xpath("//tr[contains(., '" + memberEmail + "')]//button[contains(text(), 'Confirm Return')]"));
    }

    public void clickConfirmReturn(String memberEmail) {
        click(driver.findElement(By.xpath("//tr[contains(., '" + memberEmail + "')]//button[contains(text(), 'Confirm Return')]")));
    }

    public boolean isMarkOverdueButtonPresent(String memberEmail) {
        return isElementPresent(By.xpath("//tr[contains(., '" + memberEmail + "')]//button[contains(text(), 'Mark Overdue')]"));
    }

    public void clickMarkOverdue(String memberEmail) {
        click(driver.findElement(By.xpath("//tr[contains(., '" + memberEmail + "')]//button[contains(text(), 'Mark Overdue')]")));
    }

    public boolean isReportLostButtonPresent(String memberEmail) {
        return isElementPresent(By.xpath("//tr[contains(., '" + memberEmail + "')]//button[contains(text(), 'Report Lost')]"));
    }

    public void clickReportLostAndConfirm(String memberEmail) {
        click(driver.findElement(By.xpath("//tr[contains(., '" + memberEmail + "')]//button[contains(text(), 'Report Lost')]")));
        Alert alert = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    public void clickBackToAdmin() {
        click(backToAdminLink);
    }
}
