package de.agiehl.boardgame.b2btest.theme;

import de.agiehl.boardgame.b2btest.domain.GameResult;
import de.agiehl.boardgame.b2btest.domain.Player;
import de.agiehl.boardgame.b2btest.domain.StatEntry;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

/**
 * A space saving variant: every player is condensed to a single line with the statistics
 * separated by a pipe, which keeps long games short in the forum.
 */
@Component
public class CompactBggTheme implements BggTheme {

    private static final String INTRO = "[i]Played on boardgamearena.com[/i]";

    @Override
    public String id() {
        return "compact";
    }

    @Override
    public String render(GameResult result) {
        StringJoiner lines = new StringJoiner("\n");
        lines.add(INTRO);
        if (!result.globalStatistics().isEmpty()) {
            lines.add(renderGlobalStatistics(result));
        }
        for (Player player : result.players()) {
            lines.add(renderPlayer(player));
        }
        return lines.toString();
    }

    private String renderGlobalStatistics(GameResult result) {
        StringJoiner stats = new StringJoiner(" | ");
        for (StatEntry entry : result.globalStatistics()) {
            stats.add(entry.label() + ": [b]" + entry.value() + "[/b]");
        }
        return stats.toString();
    }

    private String renderPlayer(Player player) {
        StringJoiner stats = new StringJoiner(" | ");
        for (StatEntry entry : player.entries()) {
            stats.add(entry.label() + ": [b]" + entry.value() + "[/b]");
        }
        return "[b]g{" + player.displayName() + "}g[/b] — " + stats;
    }
}
