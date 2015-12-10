package com.atlassian.jira.util;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.RegexValidator;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs URL validation.
 *
 * Only accepts HTTP or HTTPS URLS.
 */
public final class UrlValidator
{
    private static org.apache.commons.validator.routines.UrlValidator urlValidator;

    /**
     * Because org.apache.commons.validator.routines.UrlValidator does not allow custom Top Level Domains
     * like ".local", ".colo", etc. which are often use by customers, we cannot directly use Apache Commons here.
     * Unfortuantely Apache UrlValidator is not configurable and everything there is hard-coded and made private final.
     * That's why we have to restort to this approach.
     * This class just overrides isValidAuthority *with needed dependencies) from its Apache Commons counterpart,
     * but just accepts any kind of top level domain.
     */
    private static class MyValidator extends org.apache.commons.validator.routines.UrlValidator {

        // all these constants are copied from org.apache.commons.validator.routines.UrlValidator
        // because they are private there

        private static final String AUTHORITY_CHARS_REGEX = "\\p{Alnum}\\-\\.";

        private static final String AUTHORITY_REGEX =
                "^([" + AUTHORITY_CHARS_REGEX + "]*)(:\\d*)?(.*)?";

        private static final Pattern AUTHORITY_PATTERN = Pattern.compile(AUTHORITY_REGEX);

        private static final int PARSE_AUTHORITY_HOST_IP = 1;

        private static final int PARSE_AUTHORITY_PORT = 2;

        private static final int PARSE_AUTHORITY_EXTRA = 3;

        private static final String DOMAIN_LABEL_REGEX = "\\p{Alnum}(?>[\\p{Alnum}-]*\\p{Alnum})*";

        private static final String TOP_LABEL_REGEX = "\\p{Alnum}{2,}";

        private static final String DOMAIN_NAME_REGEX =
                "^(?:" + DOMAIN_LABEL_REGEX + "\\.)+" + "(" + TOP_LABEL_REGEX + ")$";
        private static final String PORT_REGEX = "^:(\\d{1,5})$";

        private static final Pattern PORT_PATTERN = Pattern.compile(PORT_REGEX);


        private final RegexValidator domainRegex =
                new RegexValidator(DOMAIN_NAME_REGEX);

        private final RegexValidator hostnameRegex =
                new RegexValidator(DOMAIN_LABEL_REGEX);

        // end of constants copied from parent class


        public MyValidator(String[] schemes)
        {
            super(schemes);
        }

        private boolean isValidDomain(String domain) {
            String[] groups = domainRegex.match(domain);
            if (groups != null && groups.length > 0) {
                return true;
            } else {
                if (hostnameRegex.isValid(domain)) {
                    return true;
                }
            }
            return false;
        }


    /** Mostly copied from its parent class.
     *
     * Returns true if the authority is properly formatted.  An authority is the combination
     * of hostname and port.  A <code>null</code> authority value is considered invalid.
     * @param authority Authority value to validate.
     * @return true if authority (hostname and port) is valid.
     */
    @Override
    protected boolean isValidAuthority(String authority)
    {
        if (authority == null)
        {
            return false;
        }

        Matcher authorityMatcher = AUTHORITY_PATTERN.matcher(authority);
        if (!authorityMatcher.matches())
        {
            return false;
        }

        String hostLocation = authorityMatcher.group(PARSE_AUTHORITY_HOST_IP);
        // check if authority is hostname or IP address:
        // try a hostname first since that's much more likely
        if (!isValidDomain(hostLocation))
        { // this is the only change to the original version, where DomainValidator was used instead
            // try an IP address
            InetAddressValidator inetAddressValidator =
                    InetAddressValidator.getInstance();
            if (!inetAddressValidator.isValid(hostLocation))
            {
                // isn't either one, so the URL is invalid
                return false;
            }
        }

        String port = authorityMatcher.group(PARSE_AUTHORITY_PORT);
        if (port != null)
        {
            if (!PORT_PATTERN.matcher(port).matches())
            {
                return false;
            }
        }

        String extra = authorityMatcher.group(PARSE_AUTHORITY_EXTRA);
        if (extra != null && extra.trim().length() > 0)
        {
            return false;
        }

        return true;
    }
}

    static {
        String[] schemes = {"http", "https"};
        urlValidator = new MyValidator(schemes);
    }

    /**
     * Validates a URL.
     *
     * Handles internationalized domain names (IDNs) by converting them to ASCII, strips query strings and fragments,
     * returns {@code false} if {@code url} is {@code null}.
     *
     * @param url The URL to validate.
     * @return {@code true} if {@code url} is valid; {@code false} otherwise.
     */
    public static boolean isValid(String url)
    {
        if (url == null)
            return false;

        try
        {
            URI uri = new URI(url);

            // A NullPointerException will be thrown if the authority is null.
            String authority = uri.getAuthority();
            if (authority == null)
                return false;
            try
            {
                authority = IDN.toASCII(authority);
            }
            catch (IllegalArgumentException ex)
            {
                // * @throws IllegalArgumentException   if the input string doesn't conform to RFC 3490 specification
                return false;
            }

            // We only want to pass the authority through toASCII(), otherwise the whole URL gets prefixed with the ACE
            // and we get an invalid scheme like "xn--http://..." instead of "http://xn--...".
            url = new URI(uri.getScheme(), authority, uri.getPath(), null, null).toString();
            return urlValidator.isValid(url);
        }
        catch (URISyntaxException e)
        {
            return false;
        }
    }
}
