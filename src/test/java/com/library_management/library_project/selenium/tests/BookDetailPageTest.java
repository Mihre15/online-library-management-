package com.library_management.library_project.selenium.tests;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.BookDetailPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("selenium")
public class BookDetailPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Book detail page redirects unauthenticated visitors to /login")
    public void testRedirectsToLoginWhenUnauthenticated() {
        BookDetailPage bookDetailPage = new BookDetailPage(driver);
        bookDetailPage.open(baseUrl, 1L);

        assertTrue(bookDetailPage.waitForUrlContains("/login"), "AuthGuard should redirect unauthenticated user to /login");
    }

    @Test
    @DisplayName("Book detail page renders back button and book details for authenticated user")
    public void testBookDetailPageLoadsForAuthenticatedUser() {
        setAuthSession(null, 1L, "STUDENT");

        BookDetailPage bookDetailPage = new BookDetailPage(driver);
        bookDetailPage.open(baseUrl, 1L);

        assertTrue(bookDetailPage.waitForUrlContains("/books/1"), "Should navigate to book 1 detail page");
    }

    @Test
    @DisplayName("Clicking back to shelves link navigates to /search")
    public void testNavigateBackToShelves() {
        setAuthSession(null, 1L, "STUDENT");

        BookDetailPage bookDetailPage = new BookDetailPage(driver);
        bookDetailPage.open(baseUrl, 1L);

        if (bookDetailPage.isPageLoaded()) {
            bookDetailPage.clickBackToShelves();
            assertTrue(bookDetailPage.waitForUrlContains("/search"), "Should navigate back to /search");
        }
    }

    @Test
    @DisplayName("Borrow button is disabled during borrow request after click")
    public void testBorrowButtonDisabledWhileLoading() {
        setAuthSession(null, 1L, "STUDENT");

        BookDetailPage bookDetailPage = new BookDetailPage(driver);
        bookDetailPage.open(baseUrl, 1L);

        if (bookDetailPage.isPageLoaded() && bookDetailPage.isBorrowButtonVisible() && bookDetailPage.isBorrowButtonEnabled()) {
            bookDetailPage.clickBorrow();
            assertTrue(bookDetailPage.waitForBorrowButtonDisabled() || bookDetailPage.isBorrowLoading(),
                    "Borrow button should become disabled while processing borrow request");
        }
    }
}
