package de.agiehl.boardgame.b2btest.web;

import de.agiehl.boardgame.b2btest.theme.BggTheme;
import de.agiehl.boardgame.b2btest.theme.ThemeRegistry;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the single page UI.
 */
@Controller
public class PageController {

    private final ThemeRegistry themeRegistry;

    public PageController(ThemeRegistry themeRegistry) {
        this.themeRegistry = themeRegistry;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("themes", themeOptions());
        model.addAttribute("defaultThemeId", themeRegistry.defaultTheme().id());
        return "index";
    }

    private List<ThemeOption> themeOptions() {
        return themeRegistry.all().stream()
                .map(theme -> new ThemeOption(theme.id(), theme.labelKey()))
                .toList();
    }
}
