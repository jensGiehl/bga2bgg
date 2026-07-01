package de.agiehl.boardgame.b2btest.theme;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Central lookup for all available {@link BggTheme}s. The first registered theme (the
 * {@link ColorfulBggTheme}, ordered first) is the fallback when an unknown id is requested.
 */
@Component
public class ThemeRegistry {

    private final Map<String, BggTheme> themesById = new LinkedHashMap<>();
    private final BggTheme defaultTheme;

    public ThemeRegistry(ColorfulBggTheme defaultTheme, List<BggTheme> themes) {
        this.defaultTheme = defaultTheme;
        themesById.put(defaultTheme.id(), defaultTheme);
        for (BggTheme theme : themes) {
            themesById.putIfAbsent(theme.id(), theme);
        }
    }

    public BggTheme byId(String id) {
        return themesById.getOrDefault(id, defaultTheme);
    }

    public BggTheme defaultTheme() {
        return defaultTheme;
    }

    public List<BggTheme> all() {
        return List.copyOf(themesById.values());
    }
}
