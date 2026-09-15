package com.library_management.library_project.selenium.tests;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("selenium")
public class LoginPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Login page displays required input fields, submit button, and heading")
    public void testLoginPageElementsDisplayed() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl);

        assertTrue(loginPage.isPageLoaded(), "Login page fields should be visible");
        assertEquals("Sign in", loginPage.getPageHeading(), "Page heading should match");
        assertFalse(loginPage.isSubmitEnabled(), "Submit button should be disabled when fields are empty");
    }

    @Test
    @DisplayName("Login submit button enables when fields are filled")
    public void testSubmitButtonEnablesOnInput() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl);

        loginPage.enterEmail("student@test.com");
        loginPage.enterPassword("password123");

        assertTrue(loginPage.waitForSubmitEnabled(), "Submit button should be enabled after entering credentials");
    }

    @Test
    @DisplayName("Submit button is disabled during request processing after click")
    public void testSubmitButtonDisabledWhileLoading() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl);

        loginPage.enterEmail("student@test.com");
        loginPage.enterPassword("password123");
        assertTrue(loginPage.waitForSubmitEnabled(), "Submit button should be enabled before click");

        delayNetworkRequests(3000);
        loginPage.clickSubmit();
        assertTrue(loginPage.waitForSubmitDisabled() || loginPage.isSubmitLoading(),
                "Submit button should be disabled or show spinner while request is resolving");
    }

    @Test
    @DisplayName("Clicking register link navigates to /register")
    public void testNavigateToRegister() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl);
        assertTrue(loginPage.isPageLoaded(), "Login page fields should be loaded before clicking register link");

        loginPage.clickRegisterLink();
        assertTrue(loginPage.waitForUrlContains("/register"), "Should navigate to /register");
    }

    @Test
    @DisplayName("Invalid credentials show error banner")
    public void testLoginWithInvalidCredentialsShowsError() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl);

        loginPage.login("nonexistent@domain.com", "wrongpassword");

        assertTrue(loginPage.isErrorMessageDisplayed(), "Error banner should be displayed on failed login");
    }

    @Test
    @DisplayName("Already logged in user is redirected from login to search")
    public void testRedirectWhenAlreadyLoggedIn() {
        setAuthSession(null, 1L, "STUDENT");

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open(baseUrl);

        assertTrue(loginPage.waitForUrlContains("/search"), "Logged in user should be redirected to /search");
    }
}
