package de.agiehl.boardgame.b2btest.domain;

import java.util.List;

/**
 * The fully assembled result of a single game: an ordered list of players, each already
 * carrying only the statistics that should be printed.
 *
 * @param globalStatistics optional statistics that apply to the whole game
 * @param players          the players ordered by their finishing position (best first)
 */
public record GameResult(List<StatEntry> globalStatistics, List<Player> players) {

    public GameResult {
        globalStatistics = List.copyOf(globalStatistics);
        players = List.copyOf(players);
    }

    public GameResult(List<Player> players) {
        this(List.of(), players);
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }
}
