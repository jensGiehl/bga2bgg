package de.agiehl.boardgame.b2btest.theme;

import de.agiehl.boardgame.b2btest.domain.GameResult;
import de.agiehl.boardgame.b2btest.domain.Player;
import de.agiehl.boardgame.b2btest.domain.StatEntry;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

/**
 * A colorful variant: player names are colored by their finishing position (gold / silver /
 * bronze / blue), giving the post a livelier, ranking oriented look. No emoji or place markers are
 * used, as BoardGameGeek does not render emoji.
 */
@Component
public class ColorfulBggTheme implements BggTheme {

    private static final String INTRO = "[i]This game was played on boardgamearena.com[/i]";

    @Override
    public String id() {
        return "colorful";
    }

    @Override
    public String render(GameResult result) {
        StringJoiner blocks = new StringJoiner("\n\n");
        blocks.add(INTRO);
        int position = 1;
        for (Player player : result.players()) {
            blocks.add(renderPlayer(player, position++));
        }
        return blocks.toString();
    }

    private String renderPlayer(Player player, int position) {
        StringBuilder block = new StringBuilder();
        block.append("[color=").append(color(position)).append("]")
                .append("[b][u]g{").append(player.displayName()).append("}g[/u][/b]")
                .append("[/color]");
        for (StatEntry entry : player.entries()) {
            block.append('\n')
                    .append("[color=#808080]").append(entry.label()).append("[/color]: ")
                    .append("[b]").append(entry.value()).append("[/b]");
        }
        return block.toString();
    }

    private String color(int position) {
        return switch (position) {
            case 1 -> "#E8A317"; // gold
            case 2 -> "#A9A9A9"; // silver
            case 3 -> "#CD7F32"; // bronze
            default -> "#4169E1"; // royal blue
        };
    }
}
