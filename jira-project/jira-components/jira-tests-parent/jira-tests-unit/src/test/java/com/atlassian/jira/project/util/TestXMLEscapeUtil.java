package com.atlassian.jira.project.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.atlassian.jira.imports.project.util.XMLEscapeUtil;
import com.atlassian.jira.util.xml.XMLEscapingReader;

import com.google.common.io.CharStreams;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.atlassian.jira.imports.project.util.XMLEscapeUtil.ESCAPING_CHAR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class TestXMLEscapeUtil
{

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String getEscapingCharHexCode()
    {
        return String.format("%04X", (int)ESCAPING_CHAR);
    }

    @Test
    public void encodingStringThatDoesNotNeedEscapingReturnsStringEqualsToOriginal()
    {
        assertThat(XMLEscapeUtil.unicodeEncode("AlaMaKota"), equalTo("AlaMaKota"));
    }

    @Test
    public void encodingOneBadCharacterShouldReturnItsEscapedValue()
    {
        assertThat(XMLEscapeUtil.unicodeEncode("\u000C"), equalTo(ESCAPING_CHAR + "000C"));
    }

    @Test
    public void decodingOneEscapedValueShouldReturnItsCharacter()
    {
        assertThat(XMLEscapeUtil.unicodeDecode(ESCAPING_CHAR + "000C"), equalTo("\u000C"));
    }

    @Test
    public void encodingBackslashShouldEscapeIt()
    {
        assertThat(XMLEscapeUtil.unicodeEncode("" + ESCAPING_CHAR), equalTo(ESCAPING_CHAR + getEscapingCharHexCode()));
    }

    @Test
    public void decodingEscapedEscapingCharacterShouldReturnEscapingCharacter()
    {
        assertThat(XMLEscapeUtil.unicodeDecode(ESCAPING_CHAR + getEscapingCharHexCode()), equalTo("" + ESCAPING_CHAR));
    }

    @Test
    public void unnecessaryEscapedCharacterIsDecodedAnyway()
    {
        assertThat(XMLEscapeUtil.unicodeDecode(ESCAPING_CHAR + "0041"), equalTo("A"));
    }

    @Test
    public void testEncodingComplex()
    {
        assertThat(XMLEscapeUtil.unicodeEncode("A\u0000\u0001B"+ ESCAPING_CHAR + "X\uFFFFD"),
                equalTo("A" + ESCAPING_CHAR + "0000" + ESCAPING_CHAR + "0001B" + ESCAPING_CHAR + getEscapingCharHexCode() +  "X" + ESCAPING_CHAR + "FFFFD"));
    }

    @Test
    public void testDecodingComplex()
    {
        assertThat(XMLEscapeUtil.unicodeDecode("A" + ESCAPING_CHAR + "0000" + ESCAPING_CHAR + "0001B" + ESCAPING_CHAR + getEscapingCharHexCode() +  "X" + ESCAPING_CHAR + "FFFFD"),
                equalTo("A\u0000\u0001B"+ ESCAPING_CHAR + "X\uFFFFD"));
    }

    @Test
    public void decodingNotEscapedBadCharacterJustReturnsIt()
    {
        assertThat(XMLEscapeUtil.unicodeDecode("\u000C"), equalTo("\u000C"));
    }

    @Test
    public void decodingSomethingThatIsBadlyEncodedJustReturnsIt()
    {
        assertThat(XMLEscapeUtil.unicodeDecode(ESCAPING_CHAR +"00ZZ"), equalTo(ESCAPING_CHAR + "00ZZ"));
    }
    @Test
    public void decodingSomethingThatIsCutEncodedJustReturnsIt()
    {
        assertThat(XMLEscapeUtil.unicodeDecode(ESCAPING_CHAR +"000"), equalTo(ESCAPING_CHAR +"000"));
    }


    @Test
    public void encodingReaderHasStopCondition() throws IOException
    {
        final String simpleText = "A";
        Reader readerToEncode = new XMLEscapingReader(new StringReader(simpleText));
        assertThat(XMLEscapeUtil.unicodeDecode(CharStreams.toString(readerToEncode)), equalTo(simpleText));
    }

    @Test
    public void encodingReaderEncodesStringThatAfterDecodingEqualsToOriginal() throws IOException
    {
        StringBuilder input = new StringBuilder();
        for (char c = 0; c < 2000; c++)
        {
            input.append(c);
        }
        for (char c = 0xFF00; c <= 0xFFFE; c++)
        {
            input.append(c);
        }
        Reader readerToEncode = new XMLEscapingReader(new StringReader(input.toString()));
        assertThat(XMLEscapeUtil.unicodeDecode(CharStreams.toString(readerToEncode)), equalTo(input.toString()));
    }

    @Test
    public void encodingReaderEncodesEachCharacterThatAfterDecodingEqualsToOriginal() throws IOException
    {
        for (int i = 0; i < 0x10000; i++)
        {
            StringBuilder input = new StringBuilder();
            input.append((char)i);
            Reader readerToEncode = new XMLEscapingReader(new StringReader(input.toString()));
            assertThat(XMLEscapeUtil.unicodeDecode(CharStreams.toString(readerToEncode)), equalTo(input.toString()));
        }
    }

    @Test
    public void encodingReaderEncodesStringThatHasOverflowBiggerThanReadBufferAndAfterDecodingEqualsToOriginal() throws IOException
    {
        StringBuilder input = new StringBuilder();
        for (int times = 0; times < 1000; times++) {
            for (char c = 0; c < 20; c++)
            {
                input.append(c);
            }
        }
        Reader readerToEncode = new XMLEscapingReader(new StringReader(input.toString()));
        assertThat(XMLEscapeUtil.unicodeDecode(CharStreams.toString(readerToEncode)), equalTo(input.toString()));
    }

}
