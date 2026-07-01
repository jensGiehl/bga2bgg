package de.agiehl.boardgame.b2btest.web;

/**
 * View model for a theme entry of the theme dropdown.
 *
 * @param id       technical id submitted to the API
 * @param labelKey message key resolved to a localized label in the template
 */
public record ThemeOption(String id, String labelKey) {
}
