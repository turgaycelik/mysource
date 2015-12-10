package com.atlassian.jira.servlet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static java.util.ResourceBundle.Control;

/**
 * An encapsulation of a language in which some words are considered offensive.
 * <p>
 * Instances of this class appear immutable, so no external synchronisation is necessary when
 * using them concurrently.
 * <p>
 * Instances are cached by the static factory methods. Changes to the language definition files
 * (.properties files) will not be reflected in the runtime until the JVM is re-started.
 *
 * @since v5.1
 */
final class Language
{
    /** The base name of the resource bundle that contain the language definitions */
    private static final String BASE_NAME = "com.atlassian.jira.servlet.Language";

    /** 
     * A cache of languages corresponding to locales. This cache 
     * is managed by the {@link #forLocale(java.util.Locale)} method.
     */
    private static Map<Locale, Language> cache = new HashMap<Locale, Language>();

    /**
     * Provides a Language instance that is based on the default locale,
     * as returned by {@code Locale.getDefault()}.
     * 
     * @return a language corresponding to the default locale
     */
    static Language forDefaultLocale() 
    {
        return forLocale(Locale.getDefault());
    }

    /**
     * Provides a Language instance that is based on the given locale.
     *
     * @param locale the locale for which a corresponding Language is required
     * @return a language corresponding to the given locale
     */
    static Language forLocale(Locale locale)
    {
        // It's safe to do this within multiple threads because, at worst,
        // we'll get a language being loaded multiple times. That's not a
        // problem because the language definitions are static.
        if (!cache.containsKey(locale))
        {
            // Tells the resource bundle subsystem to avoid the overhead of searching for
            // class file resource bundles.
            Control propOnlyControl = Control.getControl(Control.FORMAT_PROPERTIES);
            ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, locale, propOnlyControl);
            String alphabet = bundle.getString("alphabet");
            String offensive = bundle.getString("offensive");
            cache.put(locale, new Language(alphabet, new HashSet<String>(Arrays.asList(offensive.split(";")))));
        }

        // at this point the locale is guaranteed to be a
        // valid key for the cache
        return cache.get(locale);
    }

    /**
     * The sequence of characters that comprise the alphabet of this language.
     * Never null.
     */
    private final CharSequence alphabet;

    /**
     * The set of words that are considered offensive in this language.
     * Never null.
     */
    private final Set<String> offensiveWords;

    /**
     * Constructs a language with the given alphabet and a given set of offensive words.
     *
     * @param alphabet       the alphabet of this language as a sequence of characters
     * @param offensiveWords the set of words that are considered offensive in this language
     */
    private Language(CharSequence alphabet, Set<String> offensiveWords)
    {
        this.alphabet = alphabet;
        this.offensiveWords = offensiveWords;
    }

    /**
     * Determines if the given word is offensive in this language.
     * <p>
     * A word is considered offensive if:
     * <ul>
     * <li>it is one of the offensive words listed in the corresponding language definition (.properties) file; or</li>
     * <li>it starts with an offensive word listed in the language definition file; or</li>
     * <li>it ends with an offensive word listed in the language definition file; or</li>
     * <li>it is within one Levenshtein distance unit from an offensive word in the language definition file.</li>
     * </ul>
     *
     * @param word the word to test for offensiveness. Must not be null
     * @return true if the word is offensive; false otherwise
     */
    boolean isOffensive(String word)
    {
        assert word != null : "Word must not be null";

        // The offensive words are expressed in lower case, and devoid
        // of leading and trailing white space; therefore, we transform the
        // argument likewise to ensure we're comparing apples with apples.
        String lowerCaseWord = word.trim().toLowerCase();

        // Words that are close to offensive words are themselves offensive
        for (String nearbyWord : Levenshtein.nearbyWords(lowerCaseWord, alphabet))
        {
            if (offensiveWords.contains(nearbyWord))
                return true;
        }

        // Words that start with, or end with, offensive words, are themselves offensive
        for (String naughtyWord : offensiveWords)
        {
            if (lowerCaseWord.startsWith(naughtyWord) || lowerCaseWord.endsWith(naughtyWord))
                return true;
        }

        // None of the above checks found the word to be offensive,
        // so we'll report it as being non-offensive.
        return false;
    }

}
