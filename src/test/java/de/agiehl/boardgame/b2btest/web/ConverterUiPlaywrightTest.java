package de.agiehl.boardgame.b2btest.web;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * End-to-end UI test driving the real page with a headless browser.
 *
 * <p>Tagged {@code playwright} so it is skipped by the default build. Run it with
 * {@code ./mvnw test -Pui-tests} (browsers are downloaded automatically on first run).
 */
@Tag("playwright")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConverterUiPlaywrightTest {

    private static Playwright playwright;
    private static Browser browser;

    @LocalServerPort
    private int port;

    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void openPage() {
        context = browser.newContext();
        context.grantPermissions(List.of("clipboard-read", "clipboard-write"));
        page = context.newPage();
        page.navigate("http://localhost:" + port + "/");
    }

    @org.junit.jupiter.api.AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void liveConvertsPastedStatisticIntoForumMarkup() {
        page.locator("#bgaInput").fill(
                "Jakib\tCosmo\nSpielergebnis\t1. (26)\t2. (25)\nBedenkzeit\t22h20\t7h18");

        // A <textarea> exposes its live content through its value, not its text node,
        // so assert on the value (regex hasValue waits for the debounced update).
        Locator output = page.locator("#bggOutput");
        assertThat(output).hasValue(Pattern.compile("g\\{Jakob\\}g"));
        assertThat(output).hasValue(Pattern.compile("g\\{BGA User Cosmo\\}g"));
        org.assertj.core.api.Assertions.assertThat(output.inputValue())
                .contains("Spielergebnis: [b]1. (26)[/b]");
    }

    @Test
    void clearButtonEmptiesBothAreas() {
        page.locator("#bgaInput").fill("Alice\tBob\nSpielergebnis\t1. (5)\t2. (3)");
        assertThat(page.locator("#bggOutput")).not().hasValue("");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Clear")).click();

        assertThat(page.locator("#bgaInput")).hasValue("");
        assertThat(page.locator("#bggOutput")).hasValue("");
    }

    @Test
    void showsErrorAlertForInconsistentInput() {
        Locator alert = page.locator("#errorAlert");
        assertThat(alert).isHidden();

        // 3 names, 4 values per row.
        page.locator("#bgaInput").fill(
                "JensG83\tTypischserg\tJakib\nSpielergebnis\t1. (36)\t2. (34)\t3. (33)\t4. (27)");

        assertThat(alert).isVisible();
        assertThat(alert).containsText("Inconsistent input");
        assertThat(page.locator("#bggOutput")).hasValue("");
    }

    @Test
    void copyButtonPutsOutputOntoClipboard() {
        page.locator("#bgaInput").fill("Alice\tBob\nSpielergebnis\t1. (5)\t2. (3)");
        assertThat(page.locator("#bggOutput")).not().hasValue("");

        page.locator("#copyButton").click();

        Object clipboard = page.evaluate("() => navigator.clipboard.readText()");
        org.assertj.core.api.Assertions.assertThat(clipboard.toString())
                .contains("g{BGA User Alice}g");
    }
}
