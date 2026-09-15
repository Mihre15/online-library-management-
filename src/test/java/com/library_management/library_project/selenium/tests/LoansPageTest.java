package com.library_management.library_project.selenium.tests;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.LoansPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("selenium")
public class LoansPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("My Loans page redirects unauthenticated visitors to /login")
    public void testRedirectsToLoginWhenUnauthenticated() {
        LoansPage loansPage = new LoansPage(driver);
        loansPage.open(baseUrl);

        assertTrue(loansPage.waitForUrlContains("/login"), "AuthGuard should redirect unauthenticated user to /login");
    }

    @Test
    @DisplayName("My Loans page displays heading and loads properly for authenticated student")
    public void testLoansPageLoadsForAuthenticatedUser() {
        setAuthSession(null, 1L, "STUDENT");

        LoansPage loansPage = new LoansPage(driver);
        loansPage.open(baseUrl);

        assertTrue(loansPage.isPageLoaded(), "Loans page should be loaded");
        assertEquals("My loans", loansPage.getHeadingText(), "Header title should match 'My loans'");
    }
}
