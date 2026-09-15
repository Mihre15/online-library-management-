package com.library_management.library_project.selenium.tests;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.HomePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("selenium")
public class HomePageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Home page redirects to login when user is unauthenticated")
    public void testHomePageRedirectsToLoginWhenUnauthenticated() {
        HomePage homePage = new HomePage(driver);
        homePage.open(baseUrl);

        assertTrue(homePage.isRedirectedToLogin(), "Unauthenticated user should be redirected to /login");
    }

    @Test
    @DisplayName("Home page redirects to search catalog when user is authenticated")
    public void testHomePageRedirectsToSearchWhenAuthenticated() {
        setAuthSession(null, 1L, "STUDENT");

        HomePage homePage = new HomePage(driver);
        homePage.open(baseUrl);

        assertTrue(homePage.isRedirectedToSearch(), "Authenticated user should be redirected to /search");
    }
}
