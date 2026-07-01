package de.agiehl.boardgame.b2btest.conversion;

import de.agiehl.boardgame.b2btest.domain.GameResult;
import de.agiehl.boardgame.b2btest.parser.BgaStatisticsParser;
import de.agiehl.boardgame.b2btest.parser.RawStatistics;
import de.agiehl.boardgame.b2btest.theme.BggTheme;
import de.agiehl.boardgame.b2btest.theme.ThemeRegistry;
import org.springframework.stereotype.Service;

/**
 * Entry point of the conversion: takes the pasted BGA text plus a theme id and returns the
 * boardgamegeek.com forum markup. Empty input yields empty output.
 */
@Service
public class StatisticConversionService {

    private final BgaStatisticsParser parser;
    private final GameResultAssembler assembler;
    private final ThemeRegistry themeRegistry;

    public StatisticConversionService(BgaStatisticsParser parser,
                                      GameResultAssembler assembler,
                                      ThemeRegistry themeRegistry) {
        this.parser = parser;
        this.assembler = assembler;
        this.themeRegistry = themeRegistry;
    }

    public String convert(String bgaText, String themeId) {
        RawStatistics raw = parser.parse(bgaText);
        GameResult result = assembler.assemble(raw);
        if (result.isEmpty()) {
            return "";
        }
        BggTheme theme = themeRegistry.byId(themeId);
        return theme.render(result);
    }
}
