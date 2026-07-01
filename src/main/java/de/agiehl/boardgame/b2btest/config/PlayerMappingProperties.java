package de.agiehl.boardgame.b2btest.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Maps BGA usernames to the real names that should be printed instead.
 *
 * <p>Configured in {@code application.properties} using bracket notation so that the case of the
 * username is preserved, e.g. {@code bga.player-mapping[JensG83]=Jens}.
 */
@ConfigurationProperties(prefix = "bga")
public class PlayerMappingProperties {

    /** Keyed by BGA username, valued with the display name. */
    private Map<String, String> playerMapping = new LinkedHashMap<>();

    public Map<String, String> getPlayerMapping() {
        return playerMapping;
    }

    public void setPlayerMapping(Map<String, String> playerMapping) {
        this.playerMapping = playerMapping;
    }
}
