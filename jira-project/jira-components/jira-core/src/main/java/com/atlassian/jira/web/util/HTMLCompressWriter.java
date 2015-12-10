/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

public class HTMLCompressWriter extends Writer
{
    private final Writer target;

    public HTMLCompressWriter(Writer target)
    {
        this.target = target;
    }

    public void close() throws IOException
    {
        target.close();
    }

    public void flush() throws IOException
    {
        target.flush();
    }

    private static final int NORMAL = -1;
    private static final int REACHED_WHITESPACE = 1;
    private static final int REACHED_NEWLINE = 2;
    private static final char NEWLINE = '\n';
    private static final char WHITESPACE = ' ';
    private static final char TAB = '\t';

    public void write(char[] cbuf, int off, int len) throws IOException
    {
        CharArrayWriter caw = new CharArrayWriter(len - off);

        int state = NORMAL;

loop: for (int i = off; i < len; i++)
        {
            char c = cbuf[i];

            switch (state)
            {
            case NORMAL:
                break;

            case REACHED_NEWLINE:
                if (c == WHITESPACE || c == NEWLINE)
                {
                    continue loop;
                }
                else
                {
                    state = NORMAL;
                    break;
                }

            case REACHED_WHITESPACE:
                if (c == WHITESPACE)
                {
                    continue loop;
                }
                else if (c == NEWLINE) //we need to print the first newline that we come to, otherwise we concatenate lines
                {
                    caw.write(c);
                    state = REACHED_NEWLINE;
                    continue loop;
                }
                else
                {
                    state = NORMAL;
                    break;
                }
            }

            if (c == NEWLINE)
            {
                state = REACHED_NEWLINE;
                caw.write(c);
                continue loop;
            }
            else if (c == WHITESPACE)
            {
                state = REACHED_WHITESPACE;
                caw.write(c);
                continue loop;
            }
            else if (c == TAB)
            {
                state = REACHED_WHITESPACE;
                caw.write(WHITESPACE);
                continue loop;
            }
            else
            {
                caw.write(c);
            }
        }

        final char[] chars = caw.toCharArray();
        target.write(chars, 0, chars.length);
    }
}
