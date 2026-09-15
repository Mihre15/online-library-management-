package com.library_management.library_project.selenium.pages.admin;

import com.library_management.library_project.selenium.pages.BasePage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class AdminBooksPage extends BasePage {

    @FindBy(xpath = "//h1[contains(text(), 'Book Management')]")
    private WebElement pageHeading;

    @FindBy(xpath = "//a[contains(@href, '/admin/books/new') and contains(text(), 'Add Book')]")
    private WebElement addBookButton;

    @FindBy(xpath = "//a[contains(@href, '/admin') and contains(text(), 'Back to Admin')]")
    private WebElement backToAdminLink;

    @FindBy(css = "table tbody tr")
    private List<WebElement> bookRows;

    public AdminBooksPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/admin/books");
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(pageHeading);
    }

    public String getHeadingText() {
        return getText(pageHeading);
    }

    public void clickAddBook() {
        click(addBookButton);
    }

    public void clickBackToAdmin() {
        click(backToAdminLink);
    }

    public int getBookRowCount() {
        return driver.findElements(By.cssSelector("table tbody tr")).size();
    }

    public void clickEditBook(long bookId) {
        click(driver.findElement(By.cssSelector("a[href='/admin/books/" + bookId + "']")));
    }

    public void clickDeleteBookAndConfirm(long bookId) {
        WebElement row = driver.findElement(By.xpath("//tr[contains(., '/admin/books/" + bookId + "') or .//a[@href='/admin/books/" + bookId + "']]"));
        WebElement deleteBtn = row.findElement(By.xpath(".//button[contains(text(), 'Delete')]"));
        deleteBtn.click();
        Alert alert = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    public boolean isBookWithTitlePresent(String title) {
        return isElementPresent(By.xpath("//table//td[contains(text(), '" + title + "')]"));
    }
}
