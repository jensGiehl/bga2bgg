package de.agiehl.boardgame.b2btest.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.agiehl.boardgame.b2btest.SampleData;
import org.junit.jupiter.api.Test;

class BgaStatisticsParserTest {

    private final BgaStatisticsParser parser = new BgaStatisticsParser();

    @Test
    void parsesUsernamesFromHeaderLine() {
        RawStatistics raw = parser.parse(SampleData.bgaStatistic());

        assertThat(raw.usernames())
                .containsExactly("Cosmo-Kramer", "JensG83", "Typischserg", "Jakib");
    }

    @Test
    void keepsMultiWordLabelsAndMultiWordValuesIntact() {
        RawStatistics raw = parser.parse(SampleData.bgaStatistic());

        RawStatisticRow result = raw.rows().getFirst();
        assertThat(result.label()).isEqualTo("Spielergebnis");
        assertThat(result.values()).containsExactly("1. (36)", "2. (34)", "3. (33)", "4. (27)");
    }

    @Test
    void acceptsRunsOfSpacesAsColumnSeparator() {
        String pasted = "Alice     Bob\nSpielergebnis     1. (10)     2. (8)";

        RawStatistics raw = parser.parse(pasted);

        assertThat(raw.usernames()).containsExactly("Alice", "Bob");
        assertThat(raw.rows().getFirst().values()).containsExactly("1. (10)", "2. (8)");
    }

    @Test
    void parsesOptionalGameWideStatisticsBeforePlayerTable() {
        String pasted = String.join("\n",
                "Durchschnittliche Punktzahl",
                "0",
                "Getötete Schlurfer",
                "6",
                "JensG83\tJakib",
                "Spielergebnis\t1. (0)\t1. (0)",
                "Bedenkzeit\t2h38\t17h23");

        RawStatistics raw = parser.parse(pasted);

        assertThat(raw.globalStatistics()).containsExactly(
                new RawGlobalStatistic("Durchschnittliche Punktzahl", "0"),
                new RawGlobalStatistic("Getötete Schlurfer", "6"));
        assertThat(raw.usernames()).containsExactly("JensG83", "Jakib");
        assertThat(raw.rows()).hasSize(2);
    }

    @Test
    void throwsWhenARowHasMoreValuesThanPlayers() {
        // 3 usernames but the rows carry 4 values.
        String pasted = String.join("\n",
                "JensG83\tTypischserg\tJakib",
                "Spielergebnis\t1. (36)\t2. (34)\t3. (33)\t4. (27)");

        assertThatThrownBy(() -> parser.parse(pasted))
                .isInstanceOf(StatisticFormatException.class)
                .satisfies(thrown -> {
                    StatisticFormatException exception = (StatisticFormatException) thrown;
                    assertThat(exception.getMessageCode()).isEqualTo("error.columnMismatch");
                    assertThat(exception.getArguments()).containsExactly("Spielergebnis", 4, 3);
                });
    }

    @Test
    void throwsWhenARowHasFewerValuesThanPlayers() {
        String pasted = "Alice\tBob\tCarol\nBedenkzeit\t1h";

        assertThatThrownBy(() -> parser.parse(pasted))
                .isInstanceOf(StatisticFormatException.class);
    }

    @Test
    void returnsEmptyForBlankInput() {
        assertThat(parser.parse("   \n\n").isEmpty()).isTrue();
        assertThat(parser.parse(null).isEmpty()).isTrue();
    }
}
