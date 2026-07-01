package de.agiehl.boardgame.b2btest.domain;

import java.util.List;

/**
 * The fully assembled result of a single game: an ordered list of players, each already
 * carrying only the statistics that should be printed.
 *
 * @param players the players ordered by their finishing position (best first)
 */
public record GameResult(List<Player> players) {

    public GameResult {
        players = List.copyOf(players);
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }
}
