package de.agiehl.boardgame.b2btest.web;

/**
 * JSON response of a live conversion request. Exactly one of {@code output} / {@code error}
 * carries content; {@code error} is a ready-to-display, localized message.
 *
 * @param output the boardgamegeek.com forum markup (empty when the input was invalid)
 * @param error  a localized error message, or {@code null} when the conversion succeeded
 */
public record ConvertResponse(String output, String error) {

    public static ConvertResponse success(String output) {
        return new ConvertResponse(output, null);
    }

    public static ConvertResponse failure(String error) {
        return new ConvertResponse("", error);
    }
}
