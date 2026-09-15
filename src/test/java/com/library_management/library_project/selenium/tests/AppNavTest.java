package com.library_management.library_project.selenium.tests;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.AppNavComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("selenium")
public class AppNavTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Header navigation is not displayed on login page")
    public void testNavNotRenderedOnLoginPage() {
        navigateTo("/login");
        AppNavComponent nav = new AppNavComponent(driver);

        assertFalse(nav.isCatalogLinkVisible(), "Catalog link should not be present on login page");
    }

    @Test
    @DisplayName("Header navigation shows Catalog, My Loans and Sign Out for authenticated student")
    public void testNavItemsForStudent() {
        setAuthSession(null, 1L, "STUDENT");
        navigateTo("/search");

        AppNavComponent nav = new AppNavComponent(driver);
        assertTrue(nav.isCatalogLinkVisible(), "Catalog link should be visible");
        assertTrue(nav.isLoansLinkVisible(), "My Loans link should be visible");
        assertTrue(nav.isSignOutButtonVisible(), "Sign Out button should be visible");
        assertFalse(nav.isAdminLinkVisible(), "Admin link should not be visible for normal students");
    }

    @Test
    @DisplayName("Header navigation displays Admin link when logged in as ADMIN")
    public void testNavAdminLinkVisibleForAdmin() {
        setAuthSession(null, 999L, "ADMIN");
        navigateTo("/search");

        AppNavComponent nav = new AppNavComponent(driver);
        assertTrue(nav.isAdminLinkVisible(), "Admin link should be visible when user has ADMIN role");
    }

    @Test
    @DisplayName("Clicking Sign Out button logs user out and redirects to /login")
    public void testSignOutAction() {
        setAuthSession(null, 1L, "STUDENT");
        navigateTo("/search");

        AppNavComponent nav = new AppNavComponent(driver);
        nav.clickSignOut();

        assertTrue(nav.waitForUrlContains("/login"), "User should be redirected to /login after clicking Sign Out");
    }
}
