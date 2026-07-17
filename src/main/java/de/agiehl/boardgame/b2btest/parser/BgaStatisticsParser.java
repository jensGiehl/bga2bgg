package de.agiehl.boardgame.b2btest.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Turns the raw text a user copies from a boardgamearena.com statistics table into a
 * {@link RawStatistics} structure.
 *
 * <p>Columns on BGA are separated by tabs. Because copy&amp;paste sometimes turns tabs into
 * runs of spaces, a run of two or more whitespace characters is accepted as a separator as
 * well. A single space never separates columns, so multi-word labels such as
 * {@code "Punkte für Abenteuertafel A"} and multi-word values such as {@code "1. (36)"} stay
 * intact.
 */
@Component
public class BgaStatisticsParser {

    private static final Pattern COLUMN_SEPARATOR = Pattern.compile("\t+| {2,}");

    public RawStatistics parse(String rawText) {
        List<String> lines = nonBlankLines(rawText);
        if (lines.isEmpty()) {
            return RawStatistics.empty();
        }

        int headerIndex = findPlayerHeader(lines);
        List<RawGlobalStatistic> globalStatistics = parseGlobalStatistics(
                lines.subList(0, headerIndex));
        List<String> usernames = splitColumns(lines.get(headerIndex));
        List<RawStatisticRow> rows = parseRows(
                lines.subList(headerIndex + 1, lines.size()), usernames.size());
        return new RawStatistics(globalStatistics, usernames, rows);
    }

    /** The row below the player header has one additional column for its statistic label. */
    private int findPlayerHeader(List<String> lines) {
        for (int index = 0; index + 1 < lines.size(); index++) {
            int headerColumns = splitColumns(lines.get(index)).size();
            boolean allFollowingLinesMatch = lines.subList(index + 1, lines.size()).stream()
                    .allMatch(line -> splitColumns(line).size() == headerColumns + 1);
            if (allFollowingLinesMatch) {
                return index;
            }
        }
        return 0;
    }

    private List<RawGlobalStatistic> parseGlobalStatistics(List<String> lines) {
        if (lines.size() % 2 != 0) {
            throw new StatisticFormatException("error.globalStatisticFormat", lines.size());
        }

        List<RawGlobalStatistic> statistics = new ArrayList<>();
        for (int index = 0; index < lines.size(); index += 2) {
            List<String> label = splitColumns(lines.get(index));
            List<String> value = splitColumns(lines.get(index + 1));
            if (label.size() != 1 || value.size() != 1) {
                throw new StatisticFormatException("error.globalStatisticFormat", lines.size());
            }
            statistics.add(new RawGlobalStatistic(label.getFirst(), value.getFirst()));
        }
        return statistics;
    }

    private List<RawStatisticRow> parseRows(List<String> lines, int playerCount) {
        List<RawStatisticRow> rows = new ArrayList<>();
        for (String line : lines) {
            List<String> columns = splitColumns(line);
            String label = columns.getFirst();
            List<String> values = columns.subList(1, columns.size());
            requireValueCount(label, values.size(), playerCount);
            rows.add(new RawStatisticRow(label, values));
        }
        return rows;
    }

    /** Every statistic row must carry exactly one value per player, otherwise the input is invalid. */
    private void requireValueCount(String label, int valueCount, int playerCount) {
        if (valueCount != playerCount) {
            throw new StatisticFormatException(
                    "error.columnMismatch", label, valueCount, playerCount);
        }
    }

    private List<String> splitColumns(String line) {
        String[] parts = COLUMN_SEPARATOR.split(line.strip());
        List<String> columns = new ArrayList<>(parts.length);
        for (String part : parts) {
            columns.add(part.strip());
        }
        return columns;
    }

    private List<String> nonBlankLines(String rawText) {
        if (rawText == null) {
            return List.of();
        }
        return rawText.lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .toList();
    }
}
