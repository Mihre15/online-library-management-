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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class BaseSeleniumTest {

    protected WebDriver driver;
    protected String baseUrl;
    protected String apiBaseUrl;

    @BeforeEach
    public void setUp() {
        baseUrl = System.getProperty("baseUrl", "http://localhost:3000");
        apiBaseUrl = System.getProperty("apiBaseUrl", "http://localhost:8080");
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
     * When {@code token} is null, logs in for real against the running backend (using one of
     * the seeded demo accounts matching {@code role}) so the session is accepted by the real
     * JWT signature/claims checks — the app under test isn't a mock, so the token can't be
     * either. The {@code studentId} parameter is only used as a fallback if that login fails.
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

        String effectiveToken = token;
        Long effectiveStudentId = studentId;
        if (effectiveToken == null) {
            String email = "ADMIN".equalsIgnoreCase(role) ? "admin@library.test" : "ada@library.test";
            LoginResult result = loginForRealToken(email, "Password123");
            effectiveToken = result.token();
            effectiveStudentId = result.studentId();
        }

        js.executeScript("localStorage.setItem('library.token', arguments[0]);", effectiveToken);
        js.executeScript("localStorage.setItem('library.studentId', arguments[0]);", String.valueOf(effectiveStudentId));
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

    private record LoginResult(String token, Long studentId) {}

    /**
     * Calls the real {@code POST /api/auth/login} endpoint against the running backend and
     * returns a genuinely valid, signed token — a fabricated token is rejected outright by
     * JwtAuthenticationFilter's signature check, so it never reaches the tests that actually
     * exercise an authenticated API call (e.g. borrowing a book).
     */
    private LoginResult loginForRealToken(String email, String password) {
        try {
            String requestBody = new ObjectMapper().createObjectNode()
                    .put("email", email)
                    .put("password", password)
                    .toString();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBaseUrl + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Login failed with status " + response.statusCode() + ": " + response.body());
            }
            JsonNode json = new ObjectMapper().readTree(response.body());
            return new LoginResult(json.get("token").asText(), json.get("studentId").asLong());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not log in as " + email + " against " + apiBaseUrl
                            + " — is the backend running? Override its URL with -DapiBaseUrl=...",
                    e);
        }
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
