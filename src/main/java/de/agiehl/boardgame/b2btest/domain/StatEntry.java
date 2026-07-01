package de.agiehl.boardgame.b2btest.domain;

/**
 * A single statistic of a player, e.g. {@code "Bedenkzeit" -> "9h16"}.
 *
 * @param label the (language dependent) name of the statistic as shown on BGA
 * @param value the raw value exactly as it appeared on BGA
 */
public record StatEntry(String label, String value) {
}
