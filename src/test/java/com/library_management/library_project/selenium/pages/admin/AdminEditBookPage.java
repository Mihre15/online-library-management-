package com.library_management.library_project.selenium.pages.admin;

import com.library_management.library_project.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class AdminEditBookPage extends BasePage {

    @FindBy(xpath = "//h1[contains(text(), 'Edit Book')]")
    private WebElement pageHeading;

    @FindBy(xpath = "//a[contains(@href, '/admin/books') and contains(text(), 'Back to Books')]")
    private WebElement backToBooksLink;

    @FindBy(css = "input[placeholder='Book title']")
    private WebElement titleInput;

    @FindBy(css = "input[placeholder='Author name']")
    private WebElement authorInput;

    @FindBy(css = "input[placeholder='ISBN-13']")
    private WebElement isbnInput;

    @FindBy(tagName = "select")
    private WebElement categorySelect;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    public AdminEditBookPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl, long bookId) {
        driver.get(baseUrl + "/admin/books/" + bookId);
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(pageHeading) && isElementDisplayed(titleInput);
    }

    public String getHeadingText() {
        return getText(pageHeading);
    }

    public String getTitleValue() {
        return waitForVisibility(titleInput).getAttribute("value");
    }

    public String getAuthorValue() {
        return waitForVisibility(authorInput).getAttribute("value");
    }

    public String getIsbnValue() {
        return waitForVisibility(isbnInput).getAttribute("value");
    }

    public void enterTitle(String title) {
        type(titleInput, title);
    }

    public void enterAuthor(String author) {
        type(authorInput, author);
    }

    public void enterIsbn(String isbn) {
        type(isbnInput, isbn);
    }

    public void selectCategory(String category) {
        Select select = new Select(waitForVisibility(categorySelect));
        select.selectByVisibleText(category);
    }

    public void clickSubmit() {
        click(submitButton);
    }

    public boolean isSubmitEnabled() {
        return submitButton.isEnabled();
    }

    public void clickBackToBooks() {
        click(backToBooksLink);
    }
}
