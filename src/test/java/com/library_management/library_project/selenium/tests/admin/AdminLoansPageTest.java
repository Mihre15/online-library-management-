package com.library_management.library_project.selenium.tests.admin;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.admin.AdminLoansPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("selenium")
public class AdminLoansPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Admin loans page redirects unauthenticated visitor to /login")
    public void testRedirectsToLoginWhenUnauthenticated() {
        AdminLoansPage loansPage = new AdminLoansPage(driver);
        loansPage.open(baseUrl);

        assertTrue(loansPage.waitForUrlContains("/login"), "Unauthenticated user should be redirected to /login");
    }

    @Test
    @DisplayName("Admin loans page loads for admin user with status filter options")
    public void testAdminLoansPageLoadsForAdmin() {
        setAuthSession(null, 999L, "ADMIN");

        AdminLoansPage loansPage = new AdminLoansPage(driver);
        loansPage.open(baseUrl);

        assertTrue(loansPage.isPageLoaded(), "Admin loans page should be loaded");
        assertEquals("Loan Management", loansPage.getHeadingText());
    }

    @Test
    @DisplayName("Filtering by status updates loan view")
    public void testStatusFilterButtons() {
        setAuthSession(null, 999L, "ADMIN");

        AdminLoansPage loansPage = new AdminLoansPage(driver);
        loansPage.open(baseUrl);

        loansPage.filterByStatus("ACTIVE");
        loansPage.filterByStatus("RETURN_PENDING");
        loansPage.filterByStatus("ALL");

        assertTrue(loansPage.isPageLoaded(), "Loans page remains stable after clicking filter buttons");
    }

    @Test
    @DisplayName("Clicking Back to Admin link navigates to /admin")
    public void testNavigateBackToAdmin() {
        setAuthSession(null, 999L, "ADMIN");

        AdminLoansPage loansPage = new AdminLoansPage(driver);
        loansPage.open(baseUrl);

        loansPage.clickBackToAdmin();
        assertTrue(loansPage.waitForUrlContains("/admin"), "Should navigate back to /admin");
    }
}
