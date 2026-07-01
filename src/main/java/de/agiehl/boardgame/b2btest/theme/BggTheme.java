package de.agiehl.boardgame.b2btest.theme;

import de.agiehl.boardgame.b2btest.domain.GameResult;

/**
 * Renders a {@link GameResult} into boardgamegeek.com forum markup.
 *
 * <p>Themes differ both visually (colors, separators) and in content. A theme is a Spring bean;
 * all beans are collected by the {@link ThemeRegistry}.
 */
public interface BggTheme {

    /** Stable technical id used in the UI and API (e.g. {@code "default"}). */
    String id();

    /** Message key used to render a localized, human readable name in the UI. */
    default String labelKey() {
        return "theme." + id();
    }

    String render(GameResult result);
}
