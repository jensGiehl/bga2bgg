package de.agiehl.boardgame.b2btest.domain;

import java.util.List;

/**
 * A player of a game together with the statistics that are relevant for output.
 *
 * @param displayName the resolved name (mapped real name or {@code "BGA User <username>"})
 * @param rank        the finishing position parsed from the result row, used for ordering
 *                    ({@link Integer#MAX_VALUE} when it could not be determined)
 * @param entries     the statistics of this player that are worth printing, in original row order
 */
public record Player(String displayName, int rank, List<StatEntry> entries) {

    public Player {
        entries = List.copyOf(entries);
    }
}
