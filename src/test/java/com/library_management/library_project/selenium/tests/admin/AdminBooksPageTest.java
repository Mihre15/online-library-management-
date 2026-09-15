package com.library_management.library_project.selenium.tests.admin;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.admin.AdminBooksPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("selenium")
public class AdminBooksPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Admin books page redirects unauthenticated visitor to /login")
    public void testRedirectsToLoginWhenUnauthenticated() {
        AdminBooksPage booksPage = new AdminBooksPage(driver);
        booksPage.open(baseUrl);

        assertTrue(booksPage.waitForUrlContains("/login"), "Unauthenticated user should be redirected to /login");
    }

    @Test
    @DisplayName("Admin books page loads book management interface for admin")
    public void testBooksManagementPageLoadsForAdmin() {
        setAuthSession(null, 999L, "ADMIN");

        AdminBooksPage booksPage = new AdminBooksPage(driver);
        booksPage.open(baseUrl);

        assertTrue(booksPage.isPageLoaded(), "Admin books page should be loaded");
        assertEquals("Book Management", booksPage.getHeadingText());
    }

    @Test
    @DisplayName("Clicking + Add Book navigates to /admin/books/new")
    public void testNavigateToAddBook() {
        setAuthSession(null, 999L, "ADMIN");

        AdminBooksPage booksPage = new AdminBooksPage(driver);
        booksPage.open(baseUrl);

        booksPage.clickAddBook();
        assertTrue(booksPage.waitForUrlContains("/admin/books/new"), "Should navigate to /admin/books/new");
    }

    @Test
    @DisplayName("Clicking Back to Admin link navigates to /admin")
    public void testNavigateBackToAdmin() {
        setAuthSession(null, 999L, "ADMIN");

        AdminBooksPage booksPage = new AdminBooksPage(driver);
        booksPage.open(baseUrl);

        booksPage.clickBackToAdmin();
        assertTrue(booksPage.waitForUrlContains("/admin"), "Should navigate back to /admin");
    }
}
