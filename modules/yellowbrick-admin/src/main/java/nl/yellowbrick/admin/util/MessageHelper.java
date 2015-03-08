package nl.yellowbrick.admin.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Component
public class MessageHelper {

    private static MessageSource messageSource;

    @Autowired
    public MessageHelper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public static void flash(RedirectAttributes ra, String i18nKey, String... args) {
        if(messageSource == null)
            throw new IllegalStateException("Expected messageSource to already be initialized");

        ra.addFlashAttribute("message", messageSource.getMessage(i18nKey, args, LocaleContextHolder.getLocale()));
    }

    public static void flash(RedirectAttributes ra, String i18nKey) {
        flash(ra, i18nKey, new String[] {});
    }
}
