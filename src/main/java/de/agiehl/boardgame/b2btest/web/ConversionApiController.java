package de.agiehl.boardgame.b2btest.web;

import de.agiehl.boardgame.b2btest.conversion.StatisticConversionService;
import de.agiehl.boardgame.b2btest.parser.StatisticFormatException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint that the UI calls on every keystroke to update the live preview.
 * Invalid input is reported as a localized error message instead of markup.
 */
@RestController
@RequestMapping("/api")
public class ConversionApiController {

    private final StatisticConversionService conversionService;
    private final MessageSource messageSource;

    public ConversionApiController(StatisticConversionService conversionService,
                                   MessageSource messageSource) {
        this.conversionService = conversionService;
        this.messageSource = messageSource;
    }

    @PostMapping("/convert")
    public ConvertResponse convert(@RequestBody ConvertRequest request) {
        try {
            String output = conversionService.convert(request.text(), request.theme());
            return ConvertResponse.success(output);
        } catch (StatisticFormatException exception) {
            return ConvertResponse.failure(localize(exception));
        }
    }

    private String localize(StatisticFormatException exception) {
        return messageSource.getMessage(
                exception.getMessageCode(),
                exception.getArguments(),
                LocaleContextHolder.getLocale());
    }
}
