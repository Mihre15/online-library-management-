package com.library_management.library_project.selenium.tests;

import com.library_management.library_project.selenium.base.BaseSeleniumTest;
import com.library_management.library_project.selenium.pages.RegisterPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("selenium")
public class RegisterPageTest extends BaseSeleniumTest {

    @Test
    @DisplayName("Register page displays all registration input fields and submit button")
    public void testRegisterPageElementsDisplayed() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(baseUrl);

        assertTrue(registerPage.isPageLoaded(), "Registration form fields should be displayed");
        assertEquals("Create an account", registerPage.getPageHeading(), "Page heading should match");
        assertFalse(registerPage.isSubmitEnabled(), "Submit button should be disabled initially");
    }

    @Test
    @DisplayName("Password mismatch displays warning message and keeps submit button disabled")
    public void testPasswordMismatch() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(baseUrl);

        registerPage.enterFullName("Abebe Bikila");
        registerPage.enterEmail("abebe@aau.edu.et");
        registerPage.enterPassword("password123");
        registerPage.enterConfirmPassword("different123");

        assertTrue(registerPage.isPasswordMismatchWarningDisplayed(), "Password mismatch warning should be visible");
        assertFalse(registerPage.isSubmitEnabled(), "Submit button should remain disabled when passwords do not match");
    }

    @Test
    @DisplayName("Matching inputs enable the register submit button")
    public void testValidInputsEnableSubmit() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(baseUrl);

        registerPage.enterFullName("Abebe Bikila");
        registerPage.enterEmail("abebe@aau.edu.et");
        registerPage.enterPassword("password123");
        registerPage.enterConfirmPassword("password123");

        assertFalse(registerPage.isPasswordMismatchWarningDisplayed(), "Password mismatch warning should not appear");
        assertTrue(registerPage.waitForSubmitEnabled(), "Submit button should be enabled when form is valid");
    }

    @Test
    @DisplayName("Submit button is disabled during registration request after click")
    public void testSubmitButtonDisabledWhileLoading() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(baseUrl);

        registerPage.enterFullName("Abebe Bikila");
        registerPage.enterEmail("abebe.unique@aau.edu.et");
        registerPage.enterPassword("password123");
        registerPage.enterConfirmPassword("password123");
        assertTrue(registerPage.waitForSubmitEnabled(), "Submit button should be enabled before click");

        delayNetworkRequests(3000);
        registerPage.clickSubmit();
        assertTrue(registerPage.waitForSubmitDisabled() || registerPage.isSubmitLoading(),
                "Submit button should be disabled or show spinner while registration is resolving");
    }

    @Test
    @DisplayName("Clicking sign in link navigates to /login")
    public void testNavigateToLogin() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(baseUrl);

        registerPage.clickSignInLink();
        assertTrue(registerPage.waitForUrlContains("/login"), "Should navigate to /login");
    }

    @Test
    @DisplayName("Already logged in user is redirected from register to search")
    public void testRedirectWhenAlreadyLoggedIn() {
        setAuthSession(null, 1L, "STUDENT");

        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.open(baseUrl);

        assertTrue(registerPage.waitForUrlContains("/search"), "Logged in user should be redirected to /search");
    }
}
