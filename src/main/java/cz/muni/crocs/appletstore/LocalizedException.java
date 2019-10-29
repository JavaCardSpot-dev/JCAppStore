package cz.muni.crocs.appletstore;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizedException extends Exception {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private String translated;
    private boolean isKey = true;

    public LocalizedException(String cause) {
        super(cause);
        this.translated = cause;
        isKey = false;
    }

    public LocalizedException(Throwable cause) {
        super(cause);
    }

    public LocalizedException(String cause, Throwable ex) {
        super(cause, ex);
    }

    public LocalizedException(String cause, String translated) {
        super(cause);
        this.translated = translated;
    }

    public LocalizedException(Throwable cause, String translated) {
        super(cause);
        this.translated = translated;
    }

    public LocalizedException(String cause, String translated, Throwable ex) {
        super(cause, ex);
        this.translated = translated;
    }

    @Override
    public String getLocalizedMessage() {
        if (!isKey)
            return translated;
        if (translated != null)
            return textSrc.getString(translated) + "<br>" + getMessage();
        return getMessage();
    }

    public String getLocalizedMessageWithoutCause() {
        if (!isKey)
            return translated;
        if (translated != null)
            return textSrc.getString(translated);
        //todo do not return empty, it creates empty message boxes
        return "";
    }
}