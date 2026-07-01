package de.agiehl.boardgame.b2btest.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexPageRendersInputAndOutputAreas() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"bgaInput\"")))
                .andExpect(content().string(containsString("id=\"bggOutput\"")))
                .andExpect(content().string(containsString("id=\"copyButton\"")))
                .andExpect(content().string(containsString("id=\"clearButton\"")));
    }

    @Test
    void indexPageIsTranslatedToGerman() throws Exception {
        mockMvc.perform(get("/").param("lang", "de"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Leeren")))
                .andExpect(content().string(containsString("Kopieren")));
    }

    @Test
    void convertEndpointReturnsForumMarkup() throws Exception {
        String body = """
                {"text":"Jakib\\tCosmo\\nSpielergebnis\\t1. (26)\\t2. (25)","theme":"default"}""";

        mockMvc.perform(post("/api/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.output", containsString("g{Jakob}g")))
                .andExpect(jsonPath("$.output", containsString("g{BGA User Cosmo}g")));
    }

    @Test
    void convertEndpointReturnsLocalizedErrorForInconsistentInput() throws Exception {
        // 3 usernames, 4 values per row.
        String body = """
                {"text":"JensG83\\tTypischserg\\tJakib\\nSpielergebnis\\t1. (36)\\t2. (34)\\t3. (33)\\t4. (27)","theme":"default"}""";

        mockMvc.perform(post("/api/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.output").value(""))
                .andExpect(jsonPath("$.error", containsString("4")))
                .andExpect(jsonPath("$.error", containsString("3")))
                .andExpect(jsonPath("$.error", containsString("Inconsistent input")));
    }

    @Test
    void convertEndpointErrorIsTranslatedToGerman() throws Exception {
        String body = """
                {"text":"JensG83\\tTypischserg\\tJakib\\nSpielergebnis\\t1. (36)\\t2. (34)\\t3. (33)\\t4. (27)","theme":"default"}""";

        mockMvc.perform(post("/api/convert").param("lang", "de")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error", containsString("Nicht stimmige Eingabe")));
    }

    @Test
    void convertEndpointReturnsEmptyOutputForBlankInput() throws Exception {
        mockMvc.perform(post("/api/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"\",\"theme\":\"default\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.output").value(""));
    }
}
