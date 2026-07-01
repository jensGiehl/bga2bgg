package de.agiehl.boardgame.b2btest.web;

/**
 * JSON body of a live conversion request.
 *
 * @param text  the raw text pasted from boardgamearena.com
 * @param theme the id of the theme to render with (falls back to the default theme when unknown)
 */
public record ConvertRequest(String text, String theme) {
}
