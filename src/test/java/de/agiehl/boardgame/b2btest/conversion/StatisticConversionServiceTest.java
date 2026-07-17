package de.agiehl.boardgame.b2btest.conversion;

import static org.assertj.core.api.Assertions.assertThat;

import de.agiehl.boardgame.b2btest.config.PlayerMappingProperties;
import de.agiehl.boardgame.b2btest.parser.BgaStatisticsParser;
import de.agiehl.boardgame.b2btest.theme.ColorfulBggTheme;
import de.agiehl.boardgame.b2btest.theme.CompactBggTheme;
import de.agiehl.boardgame.b2btest.theme.DefaultBggTheme;
import de.agiehl.boardgame.b2btest.theme.ThemeRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StatisticConversionServiceTest {

    private final StatisticConversionService service = newService();

    private static StatisticConversionService newService() {
        PlayerMappingProperties properties = new PlayerMappingProperties();
        properties.setPlayerMapping(Map.of("Jakib", "Jakob"));
        GameResultAssembler assembler = new GameResultAssembler(new PlayerNameResolver(properties));
        ColorfulBggTheme defaultTheme = new ColorfulBggTheme();
        ThemeRegistry registry = new ThemeRegistry(
                defaultTheme, List.of(new DefaultBggTheme(), defaultTheme, new CompactBggTheme()));
        return new StatisticConversionService(new BgaStatisticsParser(), assembler, registry);
    }

    private static String twoPlayerInput() {
        return String.join("\n",
                "Jakib\tCosmo-Kramer",
                "Spielergebnis\t1. (26)\t2. (25)",
                "Bedenkzeit\t22h20\t7h18",
                "Punkte für Abenteuertafel A\t26\t25",
                "Münzen durch Smutjes\t9\t2");
    }

    @Test
    void producesTheExpectedDefaultForumMarkup() {
        String expected = String.join("\n",
                "[i]This game was played on boardgamearea.com[/i]",
                "",
                "[b][u]g{Jakob}g[/u][/b]",
                "Spielergebnis: [b]1. (26)[/b]",
                "Bedenkzeit: [b]22h20[/b]",
                "Punkte für Abenteuertafel A: [b]26[/b]",
                "Münzen durch Smutjes: [b]9[/b]",
                "",
                "[b][u]g{BGA User Cosmo-Kramer}g[/u][/b]",
                "Spielergebnis: [b]2. (25)[/b]",
                "Bedenkzeit: [b]7h18[/b]",
                "Punkte für Abenteuertafel A: [b]25[/b]",
                "Münzen durch Smutjes: [b]2[/b]");

        assertThat(service.convert(twoPlayerInput(), "default")).isEqualTo(expected);
    }

    @Test
    void fallsBackToDefaultThemeForUnknownThemeId() {
        assertThat(service.convert(twoPlayerInput(), "does-not-exist"))
                .isEqualTo(service.convert(twoPlayerInput(), "colorful"));
    }

    @Test
    void rendersCompactThemeAsOneLinePerPlayer() {
        String compact = service.convert(twoPlayerInput(), "compact");

        assertThat(compact).contains("[b]g{Jakob}g[/b] — Spielergebnis: [b]1. (26)[/b] | ");
        assertThat(compact.lines().count()).isEqualTo(3); // intro + 2 players
    }

    @Test
    void returnsEmptyStringForBlankInput() {
        assertThat(service.convert("", "default")).isEmpty();
    }

    @Test
    void rendersGameWideStatisticsOnceAndKeepsZeroValues() {
        String input = String.join("\n",
                "Durchschnittliche Punktzahl",
                "0",
                "Getötete Schlurfer",
                "6",
                "JensG83\tJakib",
                "Spielergebnis\t1. (0)\t1. (0)",
                "Bedenkzeit\t2h38\t17h23");

        String output = service.convert(input, "default");

        assertThat(output)
                .contains("Durchschnittliche Punktzahl: [b]0[/b]")
                .contains("Getötete Schlurfer: [b]6[/b]")
                .contains("[b][u]g{BGA User JensG83}g[/u][/b]")
                .contains("[b][u]g{Jakob}g[/u][/b]");
        assertThat(output.split("Getötete Schlurfer", -1)).hasSize(2);
    }
}
