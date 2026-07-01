package de.agiehl.boardgame.b2btest.parser;

import java.util.List;

/**
 * The structurally parsed BGA table before any name resolution, filtering or ordering.
 *
 * @param usernames the BGA usernames from the header line, in column order
 * @param rows      the statistic rows below the header
 */
public record RawStatistics(List<String> usernames, List<RawStatisticRow> rows) {

    public RawStatistics {
        usernames = List.copyOf(usernames);
        rows = List.copyOf(rows);
    }

    public static RawStatistics empty() {
        return new RawStatistics(List.of(), List.of());
    }

    public boolean isEmpty() {
        return usernames.isEmpty();
    }

    public int playerCount() {
        return usernames.size();
    }
}
