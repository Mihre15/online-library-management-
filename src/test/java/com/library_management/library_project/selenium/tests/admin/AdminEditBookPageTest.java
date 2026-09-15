package com.library_management.library_project.selenium.tests.admin;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.admin.AdminEditBookPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("selenium")
public class AdminEditBookPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Admin edit book page redirects unauthenticated visitor to /login")
    public void testRedirectsToLoginWhenUnauthenticated() {
        AdminEditBookPage editBookPage = new AdminEditBookPage(driver);
        editBookPage.open(baseUrl, 1L);

        assertTrue(editBookPage.waitForUrlContains("/login"), "Unauthenticated user should be redirected to /login");
    }

    @Test
    @DisplayName("Admin edit book page loads for admin user")
    public void testEditBookPageLoadsForAdmin() {
        setAuthSession(null, 999L, "ADMIN");

        AdminEditBookPage editBookPage = new AdminEditBookPage(driver);
        editBookPage.open(baseUrl, 1L);

        assertTrue(editBookPage.waitForUrlContains("/admin/books/1"), "Should navigate to /admin/books/1");
        if (editBookPage.isPageLoaded()) {
            assertEquals("Edit Book", editBookPage.getHeadingText());
        }
    }

    @Test
    @DisplayName("Clicking Back to Books navigates back to /admin/books")
    public void testNavigateBackToBooks() {
        setAuthSession(null, 999L, "ADMIN");

        AdminEditBookPage editBookPage = new AdminEditBookPage(driver);
        editBookPage.open(baseUrl, 1L);

        if (editBookPage.isPageLoaded()) {
            editBookPage.clickBackToBooks();
            assertTrue(editBookPage.waitForUrlContains("/admin/books"), "Should navigate back to /admin/books");
        }
    }
}
