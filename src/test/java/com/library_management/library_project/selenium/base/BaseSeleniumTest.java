package com.library_management.library_project.selenium.base;

import org.openqa.selenium.PageLoadStrategy;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public abstract class BaseSeleniumTest {

    protected WebDriver driver;
    protected String baseUrl;

    @BeforeEach
    public void setUp() {
        baseUrl = System.getProperty("baseUrl", "http://localhost:3000");
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));

        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            if (headless) {
                options.addArguments("--headless=new");
            }
            options.addArguments(
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--window-size=1920,1080",
                    "--remote-allow-origins=*"
            );
            driver = new ChromeDriver(options);
        } catch (Exception e) {
            // Fallback to Edge if Chrome is unavailable
            WebDriverManager.edgedriver().setup();
            EdgeOptions edgeOptions = new EdgeOptions();
            if (headless) {
                edgeOptions.addArguments("--headless=new");
            }
            edgeOptions.addArguments(
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--window-size=1920,1080",
                    "--remote-allow-origins=*"
            );
            driver = new EdgeDriver(edgeOptions);
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(45));
        driver.manage().window().maximize();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Navigates to a relative path using the configured baseUrl (http://localhost:3000 by default).
     */
    protected void navigateTo(String path) {
        String fullUrl = baseUrl + (path.startsWith("/") ? path : "/" + path);
        try {
            driver.get(fullUrl);
        } catch (org.openqa.selenium.TimeoutException e) {
            // If Next.js dev server causes renderer timeout on initial bundle compilation, retry once
            try {
                driver.get(fullUrl);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Injects authentication tokens directly into localStorage for authenticated page tests.
     */
    protected void setAuthSession(String token, Long studentId, String role) {
        // Only navigate to domain if not already there
        try {
            String current = driver.getCurrentUrl();
            if (current == null || !current.startsWith(baseUrl)) {
                navigateTo("/login");
            }
        } catch (Exception e) {
            navigateTo("/login");
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;

        String effectiveToken = token != null ? token : createMockJwtToken(studentId, role);
        js.executeScript("localStorage.setItem('library.token', arguments[0]);", effectiveToken);
        js.executeScript("localStorage.setItem('library.studentId', arguments[0]);", String.valueOf(studentId));
        if (role != null) {
            js.executeScript("localStorage.setItem('library.role', arguments[0]);", role);
        }
    }

    /**
     * Clears all session tokens from localStorage.
     */
    protected void clearAuthSession() {
        if (driver != null) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            try {
                js.executeScript("localStorage.clear();");
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Helper to create a dummy signed/unsigned JWT with role payload for tests.
     */
    protected String createMockJwtToken(Long studentId, String role) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payloadJson = String.format("{\"sub\":\"student%d@library.edu\",\"studentId\":%d,\"role\":\"%s\"}",
                studentId, studentId, role);
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("mock_signature".getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + "." + signature;
    }

    /**
     * Injects a delay into window.fetch so that asynchronous in-flight requests and button loading states
     * can be reliably observed and verified.
     */
    protected void delayNetworkRequests(int delayMs) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "var delay = arguments[0];" +
                "if (!window.__originalFetch) { window.__originalFetch = window.fetch; }" +
                "window.fetch = async function(...args) {" +
                "  await new Promise(r => setTimeout(r, delay));" +
                "  return window.__originalFetch.apply(this, args);" +
                "};", delayMs);
    }
}
