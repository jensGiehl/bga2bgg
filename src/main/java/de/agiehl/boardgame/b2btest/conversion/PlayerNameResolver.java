package de.agiehl.boardgame.b2btest.conversion;

import de.agiehl.boardgame.b2btest.config.PlayerMappingProperties;
import org.springframework.stereotype.Component;

/**
 * Resolves a BGA username to the name that should be printed.
 *
 * <p>Known usernames (configured via {@link PlayerMappingProperties}) are replaced with their
 * mapped real name. Unknown usernames become {@code "BGA User <username>"}.
 */
@Component
public class PlayerNameResolver {

    private static final String UNKNOWN_PREFIX = "BGA User ";

    private final PlayerMappingProperties properties;

    public PlayerNameResolver(PlayerMappingProperties properties) {
        this.properties = properties;
    }

    public String resolve(String username) {
        String mapped = properties.getPlayerMapping().get(username);
        return mapped != null ? mapped : UNKNOWN_PREFIX + username;
    }
}
