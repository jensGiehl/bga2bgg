package de.agiehl.boardgame.b2btest;

/**
 * The tab separated BGA statistic from the task description, reused across tests.
 * Columns: Cosmo-Kramer, JensG83, Typischserg, Jakib.
 */
public final class SampleData {

    private SampleData() {
    }

    public static String bgaStatistic() {
        return String.join("\n",
                row("Cosmo-Kramer", "JensG83", "Typischserg", "Jakib"),
                row("Spielergebnis", "1. (36)", "2. (34)", "3. (33)", "4. (27)"),
                row("Bedenkzeit", "9h16", "8h29", "19h55", "14h56"),
                row("Punkte für Abenteuertafel A", "36", "34", "33", "27"),
                row("Punkte für Abenteuertafel B", "0", "0", "0", "0"),
                row("Punkte für Abenteuertafel C", "0", "0", "0", "0"),
                row("Münzen durch Navigatorinnen", "18", "0", "4", "2"),
                row("Münzen durch Smutjes", "4", "4", "4", "3"),
                row("Münzen durch Kanonierinnen", "0", "10", "5", "5"),
                row("Münzen durch Affen", "0", "3", "5", "1"),
                row("Münzen verloren durch Papageien", "-1", "-1", "-2", "-1"),
                row("Gestohlene Münze", "0", "0", "0", "0"));
    }

    private static String row(String... columns) {
        return String.join("\t", columns);
    }
}
