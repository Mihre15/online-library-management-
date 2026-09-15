package com.library_management.library_project.selenium.tests.admin;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.admin.AdminAddBookPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("selenium")
public class AdminAddBookPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Admin add book page redirects unauthenticated visitor to /login")
    public void testRedirectsToLoginWhenUnauthenticated() {
        AdminAddBookPage addBookPage = new AdminAddBookPage(driver);
        addBookPage.open(baseUrl);

        assertTrue(addBookPage.waitForUrlContains("/login"), "Unauthenticated user should be redirected to /login");
    }

    @Test
    @DisplayName("Admin add book form displays all book attribute inputs and heading")
    public void testAddBookFormElementsDisplayed() {
        setAuthSession(null, 999L, "ADMIN");

        AdminAddBookPage addBookPage = new AdminAddBookPage(driver);
        addBookPage.open(baseUrl);

        assertTrue(addBookPage.isPageLoaded(), "Add book form elements should be visible");
        assertEquals("Add New Book", addBookPage.getHeadingText());
        assertFalse(addBookPage.isSubmitEnabled(), "Submit button should be disabled when fields are empty");
    }

    @Test
    @DisplayName("Entering required fields enables the Add Book submit button")
    public void testSubmitButtonEnablesWhenFieldsFilled() {
        setAuthSession(null, 999L, "ADMIN");

        AdminAddBookPage addBookPage = new AdminAddBookPage(driver);
        addBookPage.open(baseUrl);

        addBookPage.enterTitle("Clean Architecture");
        addBookPage.enterAuthor("Robert C. Martin");
        addBookPage.enterIsbn("978-0134494166");
        addBookPage.selectCategory("Software");
        addBookPage.enterTotalCopies(5);

        assertTrue(addBookPage.waitForSubmitEnabled(), "Submit button should be enabled when form is complete");
    }

    @Test
    @DisplayName("Add Book submit button is disabled during book creation request after click")
    public void testSubmitButtonDisabledWhileLoading() {
        setAuthSession(null, 999L, "ADMIN");

        AdminAddBookPage addBookPage = new AdminAddBookPage(driver);
        addBookPage.open(baseUrl);

        addBookPage.enterTitle("Clean Code");
        addBookPage.enterAuthor("Robert C. Martin");
        addBookPage.enterIsbn("978-0132350884");
        addBookPage.selectCategory("Software");
        addBookPage.enterTotalCopies(3);
        assertTrue(addBookPage.waitForSubmitEnabled(), "Submit button should be enabled before click");

        delayNetworkRequests(3000);
        addBookPage.clickSubmit();
        assertTrue(addBookPage.waitForSubmitDisabled() || addBookPage.isSubmitLoading(),
                "Submit button should be disabled or show spinner while request is resolving");
    }

    @Test
    @DisplayName("Clicking Back to Books navigates back to /admin/books")
    public void testNavigateBackToBooks() {
        setAuthSession(null, 999L, "ADMIN");

        AdminAddBookPage addBookPage = new AdminAddBookPage(driver);
        addBookPage.open(baseUrl);

        addBookPage.clickBackToBooks();
        assertTrue(addBookPage.waitForUrlContains("/admin/books"), "Should navigate back to /admin/books");
    }
}
