package de.agiehl.boardgame.b2btest.theme;

import de.agiehl.boardgame.b2btest.domain.GameResult;
import de.agiehl.boardgame.b2btest.domain.Player;
import de.agiehl.boardgame.b2btest.domain.StatEntry;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

/**
 * The reference theme described by the task: an intro line followed by one block per player.
 * Every player is a bold, underlined geek-linked name and every statistic is printed as
 * {@code Label: [b]value[/b]}.
 */
@Component
public class DefaultBggTheme implements BggTheme {

    static final String INTRO = "[i]This game was played on boardgamearea.com[/i]";

    @Override
    public String id() {
        return "default";
    }

    @Override
    public String render(GameResult result) {
        StringJoiner blocks = new StringJoiner("\n\n");
        blocks.add(INTRO);
        if (!result.globalStatistics().isEmpty()) {
            blocks.add(renderGlobalStatistics(result));
        }
        for (Player player : result.players()) {
            blocks.add(renderPlayer(player));
        }
        return blocks.toString();
    }

    private String renderGlobalStatistics(GameResult result) {
        StringJoiner lines = new StringJoiner("\n");
        for (StatEntry entry : result.globalStatistics()) {
            lines.add(entry.label() + ": [b]" + entry.value() + "[/b]");
        }
        return lines.toString();
    }

    private String renderPlayer(Player player) {
        StringBuilder block = new StringBuilder();
        block.append("[b][u]g{").append(player.displayName()).append("}g[/u][/b]");
        for (StatEntry entry : player.entries()) {
            block.append('\n')
                    .append(entry.label()).append(": [b]").append(entry.value()).append("[/b]");
        }
        return block.toString();
    }
}
