package com.library_management.library_project.selenium.tests.admin;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.admin.AdminMembersPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("selenium")
public class AdminMembersPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Admin members page redirects unauthenticated visitor to /login")
    public void testRedirectsToLoginWhenUnauthenticated() {
        AdminMembersPage membersPage = new AdminMembersPage(driver);
        membersPage.open(baseUrl);

        assertTrue(membersPage.waitForUrlContains("/login"), "Unauthenticated user should be redirected to /login");
    }

    @Test
    @DisplayName("Admin members page loads for admin user")
    public void testAdminMembersPageLoadsForAdmin() {
        setAuthSession(null, 999L, "ADMIN");

        AdminMembersPage membersPage = new AdminMembersPage(driver);
        membersPage.open(baseUrl);

        assertTrue(membersPage.isPageLoaded(), "Admin members page should be loaded");
        assertEquals("Member Management", membersPage.getHeadingText());
    }

    @Test
    @DisplayName("Clicking Back to Admin navigates to /admin")
    public void testNavigateBackToAdmin() {
        setAuthSession(null, 999L, "ADMIN");

        AdminMembersPage membersPage = new AdminMembersPage(driver);
        membersPage.open(baseUrl);

        membersPage.clickBackToAdmin();
        assertTrue(membersPage.waitForUrlContains("/admin"), "Should navigate back to /admin");
    }
}
