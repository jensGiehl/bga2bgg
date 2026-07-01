package de.agiehl.boardgame.b2btest.parser;

import java.util.List;

/**
 * One line of the pasted BGA table: a label followed by one value per player.
 *
 * @param label  the statistic name (first column)
 * @param values the values, one per player and aligned with {@link RawStatistics#usernames()}
 */
public record RawStatisticRow(String label, List<String> values) {

    public RawStatisticRow {
        values = List.copyOf(values);
    }
}
