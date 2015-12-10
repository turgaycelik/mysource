package com.atlassian.jira.jql.parser.antlr;

import org.antlr.runtime.CharStream;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Simple test for the {@link com.atlassian.jira.jql.parser.antlr.AntlrPosition}.
 *
 * @since v4.0
 */
public class TestAntlrPosition
{
    @Test
    public void testCotr() throws Exception
    {
        int charPos = 79548;
        int index = 5;
        int line = 6;
        int tokenType = 628273;

        CharStream stream = EasyMock.createMock(CharStream.class);
        EasyMock.expect(stream.index()).andReturn(index);
        EasyMock.expect(stream.getLine()).andReturn(line);
        EasyMock.expect(stream.getCharPositionInLine()).andReturn(charPos);
        EasyMock.replay(stream);

        AntlrPosition positon = new AntlrPosition(tokenType, stream);
        assertEquals(tokenType, positon.getTokenType());
        assertEquals(index, positon.getIndex());
        assertEquals(line, positon.getLineNumber());
        assertEquals(charPos, positon.getCharNumber());

        EasyMock.verify(stream);
    }
}
