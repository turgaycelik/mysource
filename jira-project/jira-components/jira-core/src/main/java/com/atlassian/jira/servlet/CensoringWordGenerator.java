package com.atlassian.jira.servlet;

import com.google.common.base.Preconditions;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;

import java.util.Locale;

/**
 * A word generator that wraps around another word generator, and censors the words that
 * the wrapped generator produces.
 * <p>
 * What if offensive words are all that we can think of? Rather than churn away through a potentially
 * infinite sequence of bad words, we will give up trying to be nice after a set number of attempts.
 *
 * @since v5.1
 */
final class CensoringWordGenerator implements WordGenerator
{
    /**
     * The word generator whose output is censored by this censoring word generator.
     * Never null.
     */
    private final WordGenerator censoredWordGenerator;

    /**
     * The number of times this censoring word generator will reject a word
     * offered by the censored word generator before giving up and just accepting
     * whatever it is given.
     */
    private final int maxRejections;

    /**
     * Constructs a censoring word generator that censors the given word generator.
     *
     * @param censoredWordGenerator the word generator that will be censored. Must not be null.
     * @param maxRejections         the number of times this censoring word generator will reject a word
     *                              offered by the censored word generator before giving up and just accepting
     *                              whatever it is given.
     * @throws NullPointerException if the censored word generator is null
     */
    CensoringWordGenerator(WordGenerator censoredWordGenerator, int maxRejections)
    {
        this.censoredWordGenerator = Preconditions.checkNotNull(censoredWordGenerator);;
        this.maxRejections = maxRejections;
    }

    @Override
    public String getWord(Integer length)
    {
        Language language = Language.forDefaultLocale();
        String word = censoredWordGenerator.getWord(length);
        int rejections = 0;
        while (language.isOffensive(word) && (rejections < maxRejections))
        {
            word = censoredWordGenerator.getWord(length);
            ++rejections;
        }
        return word;
    }

    @Override
    public String getWord(Integer length, Locale locale)
    {
        Language language = Language.forLocale(locale);
        String word = censoredWordGenerator.getWord(length, locale);
        int rejections = 0;
        while (language.isOffensive(word) && (rejections < maxRejections))
        {
            word = censoredWordGenerator.getWord(length, locale);
            ++rejections;
        }
        return word;
    }
}
