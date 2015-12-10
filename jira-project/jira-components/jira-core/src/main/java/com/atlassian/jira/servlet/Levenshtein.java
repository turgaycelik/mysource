package com.atlassian.jira.servlet;

import java.util.HashSet;
import java.util.Set;

/**
 * An encapsulation of certain aspects of the <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein
 * distance algorithm</a>.
 * <p/>
 * This class is not designed to be instantiated, and all of it's accessible methods are static.
 *
 * @since v5.1
 */
final class Levenshtein
{
    /** Prevents instantiation */
    private Levenshtein() {}

    /**
     * Produces the set of words that are within one Levenshtein distance unit from the given word.
     * The returned set includes the word passed in as an argument.
     *
     * @param word     the word from which the distance is measured. Must not be null.
     * @param alphabet the alphabet will be uses to generate words. Must not be null.
     * @return a non-empty (since the word itself will be included) set of nearby words
     */
    static Set<String> nearbyWords(String word, CharSequence alphabet)
    {
        assert word != null : "Word must not be null";
        assert alphabet != null : "Alphabet must not be null";
        Set<String> nearbySet = new HashSet<String>();
        oneCharSubstitutions(word, alphabet, nearbySet);
        oneCharDeletions(word, nearbySet);
        oneCharAdditions(word, alphabet, nearbySet);
        return nearbySet;
    }

    /**
     * Adds words to the given bucket, substituting all of the letters of the alphabet at each character position within
     * the given seed word.
     *
     * @param seedWord the word in which substitutions are to be made
     * @param alphabet the alphabet will be uses to generate words. Must not be null.
     * @param bucket   the set in which the new words are collected
     */
    private static void oneCharSubstitutions(String seedWord, CharSequence alphabet, Set<String> bucket)
    {
        for (int i = 0; i < seedWord.length(); i++)
        {
            String prefix = seedWord.substring(0, i);
            String postfix = seedWord.substring(i + 1);
            sandwichAlphas(prefix, postfix, alphabet, bucket);
        }
    }

    /**
     * Adds words to the given bucket, where each word added is the same as the seed word with one of its characters
     * deleted.
     *
     * @param seedWord the word in which the deletions are to be made
     * @param bucket   the set in which the new words are collected
     */
    private static void oneCharDeletions(String seedWord, Set<String> bucket)
    {
        for (int i = 0; i < seedWord.length(); i++)
        {
            String prefix = seedWord.substring(0, i);
            String postfix = seedWord.substring(i + 1);
            bucket.add(prefix + postfix);
        }
    }

    /**
     * Adds words to the given bucket, where each word added is the same as the seed word with one character added to
     * it.
     *
     * @param seedWord the word to which the additions will be made
     * @param alphabet the alphabet will be uses to generate words. Must not be null.
     * @param bucket   the set in which the new words are collected
     */
    private static void oneCharAdditions(String seedWord, CharSequence alphabet, Set<String> bucket)
    {
        for (int i = 0; i <= seedWord.length(); i++)
        {
            String prefix = seedWord.substring(0, i);
            String postfix = seedWord.substring(i);
            sandwichAlphas(prefix, postfix, alphabet, bucket);
        }
    }

    /**
     * Adds 26 words to the given bucket; each one sandwiches a different letter of the alphabet between the given
     * prefix and postfix.
     *
     * @param prefix   the string that will be the prefix of each word added to the bucket
     * @param postfix  the string that will be the postfix of each word adde to the bucket
     * @param alphabet the characters that will, one by one, be sandwiched between the prefix and postfix
     * @param bucket   the set to which this method will add the words it constructs
     */
    private static void sandwichAlphas(String prefix, String postfix, CharSequence alphabet, Set<String> bucket)
    {
        for (int j = 0; j < alphabet.length(); j++)
        {
            bucket.add(prefix + alphabet.charAt(j) + postfix);
        }
    }
}