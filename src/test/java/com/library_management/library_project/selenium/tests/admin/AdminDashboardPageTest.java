package com.library_management.library_project.selenium.tests.admin;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.admin.AdminDashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("selenium")
public class AdminDashboardPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Admin dashboard redirects unauthenticated visitor to /login")
    public void testRedirectsToLoginWhenUnauthenticated() {
        AdminDashboardPage dashboard = new AdminDashboardPage(driver);
        dashboard.open(baseUrl);

        assertTrue(dashboard.waitForUrlContains("/login"), "Unauthenticated user should be redirected to /login");
    }

    @Test
    @DisplayName("Admin dashboard redirects non-admin (STUDENT) away from /admin")
    public void testRedirectsToLoginWhenNotAdmin() {
        setAuthSession(null, 1L, "STUDENT");

        AdminDashboardPage dashboard = new AdminDashboardPage(driver);
        dashboard.open(baseUrl);

        // Non-admin student is redirected away from /admin (/admin -> /login -> /search)
        assertTrue(dashboard.waitForUrlContains("/search") || dashboard.waitForUrlContains("/login"),
                "Non-admin student should be redirected away from /admin");
        org.junit.jupiter.api.Assertions.assertFalse(dashboard.getCurrentUrl().endsWith("/admin"),
                "Non-admin student should not have access to /admin");
    }

    @Test
    @DisplayName("Admin dashboard loads management cards when user is ADMIN")
    public void testDashboardLoadsForAdmin() {
        setAuthSession(null, 999L, "ADMIN");

        AdminDashboardPage dashboard = new AdminDashboardPage(driver);
        dashboard.open(baseUrl);

        assertTrue(dashboard.isPageLoaded(), "Admin dashboard management cards should be loaded");
        assertEquals("Library Admin Dashboard", dashboard.getHeadingText());
    }

    @Test
    @DisplayName("Clicking Manage Books card navigates to /admin/books")
    public void testNavigateToManageBooks() {
        setAuthSession(null, 999L, "ADMIN");

        AdminDashboardPage dashboard = new AdminDashboardPage(driver);
        dashboard.open(baseUrl);

        dashboard.clickManageBooks();
        assertTrue(dashboard.waitForUrlContains("/admin/books"), "Should navigate to /admin/books");
    }

    @Test
    @DisplayName("Clicking Manage Members card navigates to /admin/members")
    public void testNavigateToManageMembers() {
        setAuthSession(null, 999L, "ADMIN");

        AdminDashboardPage dashboard = new AdminDashboardPage(driver);
        dashboard.open(baseUrl);

        dashboard.clickManageMembers();
        assertTrue(dashboard.waitForUrlContains("/admin/members"), "Should navigate to /admin/members");
    }

    @Test
    @DisplayName("Clicking Manage Loans card navigates to /admin/loans")
    public void testNavigateToManageLoans() {
        setAuthSession(null, 999L, "ADMIN");

        AdminDashboardPage dashboard = new AdminDashboardPage(driver);
        dashboard.open(baseUrl);

        dashboard.clickManageLoans();
        assertTrue(dashboard.waitForUrlContains("/admin/loans"), "Should navigate to /admin/loans");
    }
}
