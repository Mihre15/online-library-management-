package com.library_management.library_project.selenium.pages.admin;

import com.library_management.library_project.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class AdminMembersPage extends BasePage {

    @FindBy(xpath = "//h1[contains(text(), 'Member Management')]")
    private WebElement pageHeading;

    @FindBy(xpath = "//a[contains(@href, '/admin') and contains(text(), 'Back to Admin')]")
    private WebElement backToAdminLink;

    @FindBy(css = "table tbody tr")
    private List<WebElement> memberRows;

    public AdminMembersPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/admin/members");
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(pageHeading);
    }

    public String getHeadingText() {
        return getText(pageHeading);
    }

    public int getMemberRowCount() {
        return driver.findElements(By.cssSelector("table tbody tr")).size();
    }

    public boolean isMemberEmailPresent(String email) {
        return isElementPresent(By.xpath("//table//td[contains(text(), '" + email + "')]"));
    }

    public String getMemberStatus(String email) {
        WebElement row = driver.findElement(By.xpath("//tr[contains(., '" + email + "')]"));
        return row.findElement(By.xpath(".//td[4]//span")).getText().trim();
    }

    public void toggleMemberStatus(String email) {
        WebElement row = driver.findElement(By.xpath("//tr[contains(., '" + email + "')]"));
        WebElement toggleButton = row.findElement(By.xpath(".//td[7]//button"));
        click(toggleButton);
    }

    public void clickBackToAdmin() {
        click(backToAdminLink);
    }
}
