package com.library_management.library_project.selenium.pages.admin;

import com.library_management.library_project.selenium.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class AdminAddBookPage extends BasePage {

    @FindBy(xpath = "//h1[contains(text(), 'Add New Book')]")
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

    @FindBy(css = "input[type='number']")
    private WebElement totalCopiesInput;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    public AdminAddBookPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/admin/books/new");
    }

    public boolean isPageLoaded() {
        return isElementDisplayed(pageHeading) && isElementDisplayed(titleInput);
    }

    public String getHeadingText() {
        return getText(pageHeading);
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

    public void enterTotalCopies(int copies) {
        type(totalCopiesInput, String.valueOf(copies));
    }

    public void clickSubmit() {
        click(submitButton);
    }

    public void fillAndSubmit(String title, String author, String isbn, String category, int copies) {
        enterTitle(title);
        enterAuthor(author);
        enterIsbn(isbn);
        selectCategory(category);
        enterTotalCopies(copies);
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
        return isElementPresent(By.cssSelector("button[type='submit'] span.animate-spin"));
    }

    public void clickBackToBooks() {
        click(backToBooksLink);
    }
}
