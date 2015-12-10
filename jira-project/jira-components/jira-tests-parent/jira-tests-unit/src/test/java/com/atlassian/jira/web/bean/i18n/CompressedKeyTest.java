package com.atlassian.jira.web.bean.i18n;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA. User: luis Date: 23/08/13 Time: 12:06 PM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings ("RedundantStringConstructorCall")
public class CompressedKeyTest
{
    static final String WORD1 = "word1";
    static final String WORD1_WORD2 = WORD1 + ".word2";
    static final String WORD1_WORD3 = WORD1 + ".word3";
    static final String WORD1_WORD2_WORD3 = WORD1_WORD2 + ".word3";

    CompressedKey word1;
    CompressedKey word1_word2;
    CompressedKey word1_word3;
    CompressedKey word1_word2_word3;

    @Before
    public void setUp() throws Exception
    {
        word1 = CompressedKey.fromString(new String(WORD1));
        word1_word2 = CompressedKey.fromString(new String(WORD1_WORD2));
        word1_word3 = CompressedKey.fromString(new String(WORD1_WORD3));
        word1_word2_word3 = CompressedKey.fromString(new String(WORD1_WORD2_WORD3));
    }

    @Test
    public void checkToStringWorks() throws Exception
    {
        assertThat(word1.toString(), equalTo(WORD1));
        assertThat(word1_word2.toString(), equalTo(WORD1_WORD2));
    }

    @Test
    public void everythingIsInterned() throws Exception
    {
        assertThat(CompressedKey.fromString(WORD1), is(word1));
        assertThat(CompressedKey.fromString(WORD1_WORD2), is(word1_word2));
        assertThat(CompressedKey.fromString(WORD1_WORD2_WORD3), is(word1_word2_word3));

        assertThat(word1_word2.parent(), is(word1));
        assertThat(word1_word3.parent(), is(word1));
        assertThat(word1_word2_word3.parent(), is(word1_word2));
    }
}
