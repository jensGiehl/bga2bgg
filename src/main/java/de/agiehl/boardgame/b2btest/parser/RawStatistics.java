package de.agiehl.boardgame.b2btest.parser;

import java.util.List;

/**
 * The structurally parsed BGA table before any name resolution, filtering or ordering.
 *
 * @param globalStatistics optional game-wide statistics above the player table
 * @param usernames        the BGA usernames from the header line, in column order
 * @param rows             the statistic rows below the header
 */
public record RawStatistics(
        List<RawGlobalStatistic> globalStatistics,
        List<String> usernames,
        List<RawStatisticRow> rows) {

    public RawStatistics {
        globalStatistics = List.copyOf(globalStatistics);
        usernames = List.copyOf(usernames);
        rows = List.copyOf(rows);
    }

    public RawStatistics(List<String> usernames, List<RawStatisticRow> rows) {
        this(List.of(), usernames, rows);
    }

    public static RawStatistics empty() {
        return new RawStatistics(List.of(), List.of(), List.of());
    }

    public boolean isEmpty() {
        return usernames.isEmpty();
    }

    public int playerCount() {
        return usernames.size();
    }
}
