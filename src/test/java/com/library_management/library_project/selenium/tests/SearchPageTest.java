package com.library_management.library_project.selenium.tests;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.SearchPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("selenium")
public class SearchPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Search page redirects unauthenticated visitors to /login via AuthGuard")
    public void testRedirectsToLoginWhenUnauthenticated() {
        SearchPage searchPage = new SearchPage(driver);
        searchPage.open(baseUrl);

        assertTrue(searchPage.waitForUrlContains("/login"), "AuthGuard should redirect unauthenticated user to /login");
    }

    @Test
    @DisplayName("Search page loads successfully with catalog search bar for authenticated student")
    public void testSearchPageLoadsForAuthenticatedUser() {
        setAuthSession(null, 1L, "STUDENT");

        SearchPage searchPage = new SearchPage(driver);
        searchPage.open(baseUrl);

        assertTrue(searchPage.isPageLoaded(), "Search page input and button should be loaded");
        assertEquals("Currently Available Books", searchPage.getPageHeading());
    }

    @Test
    @DisplayName("Entering search term executes query on catalog")
    public void testPerformSearchQuery() {
        setAuthSession(null, 1L, "STUDENT");

        SearchPage searchPage = new SearchPage(driver);
        searchPage.open(baseUrl);

        searchPage.search("Algorithms");
        assertTrue(searchPage.getCurrentUrl().contains("/search"), "Should remain on /search page after querying");
    }
}
