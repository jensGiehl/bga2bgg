package de.agiehl.boardgame.b2btest.parser;

/**
 * Thrown when the pasted BGA text is structurally inconsistent (e.g. a statistic row has a
 * different number of values than there are players).
 *
 * <p>The exception only carries a message code and its arguments; turning it into a human readable,
 * localized message is the responsibility of the web layer.
 */
public class StatisticFormatException extends RuntimeException {

    private final String messageCode;
    private final transient Object[] arguments;

    public StatisticFormatException(String messageCode, Object... arguments) {
        super(messageCode);
        this.messageCode = messageCode;
        this.arguments = arguments != null ? arguments.clone() : new Object[0];
    }

    public String getMessageCode() {
        return messageCode;
    }

    public Object[] getArguments() {
        return arguments.clone();
    }
}
