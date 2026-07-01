package de.agiehl.boardgame.b2btest.conversion;

import de.agiehl.boardgame.b2btest.domain.GameResult;
import de.agiehl.boardgame.b2btest.domain.Player;
import de.agiehl.boardgame.b2btest.domain.StatEntry;
import de.agiehl.boardgame.b2btest.parser.RawStatisticRow;
import de.agiehl.boardgame.b2btest.parser.RawStatistics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Assembles a printable {@link GameResult} from the raw parsed table:
 * <ul>
 *     <li>resolves usernames to display names,</li>
 *     <li>keeps for every player only the statistics that carry a value (a value that is blank,
 *         {@code "-"} or {@code "0"} is treated as "not set" and dropped for that player),</li>
 *     <li>orders players by their finishing position (best first), which is read from the first
 *         statistic row (the result row on BGA).</li>
 * </ul>
 */
@Component
public class GameResultAssembler {

    private static final Pattern LEADING_RANK = Pattern.compile("^\\s*(\\d+)");

    private final PlayerNameResolver nameResolver;

    public GameResultAssembler(PlayerNameResolver nameResolver) {
        this.nameResolver = nameResolver;
    }

    public GameResult assemble(RawStatistics raw) {
        if (raw.isEmpty()) {
            return new GameResult(List.of());
        }

        List<Player> players = new ArrayList<>(raw.playerCount());
        for (int column = 0; column < raw.playerCount(); column++) {
            players.add(buildPlayer(raw, column));
        }
        players.sort(Comparator.comparingInt(Player::rank));
        return new GameResult(players);
    }

    private Player buildPlayer(RawStatistics raw, int column) {
        String displayName = nameResolver.resolve(raw.usernames().get(column));
        List<StatEntry> entries = entriesFor(raw, column);
        int rank = rankFor(raw, column);
        return new Player(displayName, rank, entries);
    }

    private List<StatEntry> entriesFor(RawStatistics raw, int column) {
        List<StatEntry> entries = new ArrayList<>();
        for (RawStatisticRow row : raw.rows()) {
            String value = row.values().get(column);
            if (hasValue(value)) {
                entries.add(new StatEntry(row.label(), value));
            }
        }
        return entries;
    }

    private int rankFor(RawStatistics raw, int column) {
        if (raw.rows().isEmpty()) {
            return Integer.MAX_VALUE;
        }
        String resultValue = raw.rows().getFirst().values().get(column);
        Matcher matcher = LEADING_RANK.matcher(resultValue);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : Integer.MAX_VALUE;
    }

    /** A value counts as "set" unless it is blank, a dash or a plain zero. */
    private boolean hasValue(String value) {
        String trimmed = value.strip();
        return !trimmed.isEmpty() && !trimmed.equals("-") && !trimmed.equals("0");
    }
}
