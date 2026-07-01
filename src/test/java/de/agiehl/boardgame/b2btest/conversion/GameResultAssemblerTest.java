package de.agiehl.boardgame.b2btest.conversion;

import static org.assertj.core.api.Assertions.assertThat;

import de.agiehl.boardgame.b2btest.SampleData;
import de.agiehl.boardgame.b2btest.config.PlayerMappingProperties;
import de.agiehl.boardgame.b2btest.domain.GameResult;
import de.agiehl.boardgame.b2btest.domain.Player;
import de.agiehl.boardgame.b2btest.domain.StatEntry;
import de.agiehl.boardgame.b2btest.parser.BgaStatisticsParser;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameResultAssemblerTest {

    private final BgaStatisticsParser parser = new BgaStatisticsParser();
    private final GameResultAssembler assembler = new GameResultAssembler(resolver());

    private static PlayerNameResolver resolver() {
        PlayerMappingProperties properties = new PlayerMappingProperties();
        properties.setPlayerMapping(Map.of("JensG83", "Jens", "Jakib", "Jakob"));
        return new PlayerNameResolver(properties);
    }

    private GameResult assembleSample() {
        return assembler.assemble(parser.parse(SampleData.bgaStatistic()));
    }

    @Test
    void ordersPlayersByFinishingPositionAndResolvesNames() {
        GameResult result = assembleSample();

        assertThat(result.players().stream().map(Player::displayName))
                .containsExactly("BGA User Cosmo-Kramer", "Jens", "BGA User Typischserg", "Jakob");
    }

    @Test
    void dropsStatisticsThatAreZeroForThatPlayer() {
        Player jens = playerNamed(assembleSample(), "Jens");

        // Jens has "0" for Navigatorinnen -> dropped, but a real value for Kanonierinnen -> kept.
        assertThat(labelsOf(jens)).doesNotContain("Münzen durch Navigatorinnen");
        assertThat(labelsOf(jens)).contains("Münzen durch Kanonierinnen");
    }

    @Test
    void keepsStatisticForPlayersThatHaveANonZeroValue() {
        Player cosmo = playerNamed(assembleSample(), "BGA User Cosmo-Kramer");

        // Cosmo has "18" for Navigatorinnen -> kept for Cosmo even though Jens dropped it.
        assertThat(labelsOf(cosmo)).contains("Münzen durch Navigatorinnen");
    }

    @Test
    void keepsNegativeValues() {
        Player cosmo = playerNamed(assembleSample(), "BGA User Cosmo-Kramer");

        assertThat(cosmo.entries())
                .contains(new StatEntry("Münzen verloren durch Papageien", "-1"));
    }

    @Test
    void dropsRowsThatAreZeroForEveryPlayer() {
        GameResult result = assembleSample();

        assertThat(result.players())
                .allSatisfy(player -> assertThat(labelsOf(player))
                        .doesNotContain("Punkte für Abenteuertafel B", "Gestohlene Münze"));
    }

    private Player playerNamed(GameResult result, String name) {
        return result.players().stream()
                .filter(player -> player.displayName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private java.util.List<String> labelsOf(Player player) {
        return player.entries().stream().map(StatEntry::label).toList();
    }
}
