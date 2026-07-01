package de.agiehl.boardgame.b2btest;

import de.agiehl.boardgame.b2btest.config.PlayerMappingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PlayerMappingProperties.class)
public class B2bTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(B2bTestApplication.class, args);
    }

}
