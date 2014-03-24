package it.digisin.collabroute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by raffaele on 16/03/14.
 */
public class EmailValidator {

    private static Pattern pattern;
    private static Matcher matcher;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static boolean validate(final String mailToValidate) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(mailToValidate);
        return matcher.matches();

    }

}
