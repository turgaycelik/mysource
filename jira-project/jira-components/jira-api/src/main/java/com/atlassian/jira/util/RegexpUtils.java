/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

public class RegexpUtils
{
    private static final Logger log = Logger.getLogger(RegexpUtils.class);

    /** Equivalent of JDK 1.4's {@link String#replaceAll(String regex, String replacement)}, usable in JDK 1.3
     *
     * @param str The string to apply operations to
     * @param regex The regex that str should match
     * @param replacement String to replace matched string with (using $1, $2 etc for substitutions).
     * @return A modified version of str, or null if the regexp was invalid
     */
    public static String replaceAll(final String str, String regex, String replacement)
    {
        Pattern pattern;
        try
        {
            pattern = new Perl5Compiler().compile(regex);
        }
        catch (MalformedPatternException e)
        {
            log.error("Error parsing regexp '"+regex+"' - "+e, e);
            return null;
        }
        return Util.substitute(new Perl5Matcher(), pattern, new Perl5Substitution(replacement), str, Util.SUBSTITUTE_ALL);

        //        return str.replaceAll(regex, replacement);
    }

    //

    /**
     * Convert a wildcard to a java.util.regexp (ie <code>'*'</code> becomes <code>'.*'</code> and <code>'?'</code> becomes <code>'.'</code>).
     * <p>Note that this converts wildcards to an exact match, so searching for <code>'a'</code> becomes <code>'^a$'</code>.
     * <p>Copied from <a href="http://www.rgagnon.com/javadetails/java-0515.html">http://www.rgagnon.com/javadetails/java-0515.html</a>
     *
     * @param wildcard  A wildcard which can contain <code>'*'</code> and <code>'?'</code> characters
     * @return A regular expression that can be passed to java.util.regexp
     */
    public static String wildcardToRegex(String wildcard){
        StringBuilder s = new StringBuilder(wildcard.length() + 4);
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                    // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return(s.toString());
    }
}
