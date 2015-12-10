package com.atlassian.jira.util.xml;

import com.atlassian.jira.imports.project.util.XMLEscapeUtil;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Reader that filters(replaces) all characters coming from another reader.
 * Every character that is XML illegal will be replaced with a Java-like unicode escape sequence '\\u[0-9][0-9][0-9][0-9]'.
 * Additionally backslash character is also escaped to ensure that decoding encoded text will ne the same.
 *
 * It tries to preserve memory. When no escaping is needed it will not use any additional buffer.
 * If some escaping is needed it will use Queue for overflow characters as output will have more characters than original.
 *
 * @since v6.0
 */
public class XMLEscapingReader extends Reader
{
    private final Reader in;
    private final Queue<Character> overflow = new ArrayDeque<Character>();

    public XMLEscapingReader(final Reader in)
    {
        this.in = in;
    }


    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException
    {
        //first fill from overflow what was read earlier
        int lengthPutFromOverflow = 0;
        for (int i = off; i < off + len; i++)
        {
            if (overflow.isEmpty())
            {
                break;
            }

            lengthPutFromOverflow++;
            cbuf[i] = overflow.remove();
        }

        //if overflow filled whole buffer we're done
        if (len == lengthPutFromOverflow)
        {
            return len;
        }

        //at this point overflow should be empty
        if (!overflow.isEmpty())
        {
            throw new IllegalStateException("Reader in inconsistent state: overflow should be empty but it contained the following value: " + Arrays.toString(overflow.toArray()));

        }

        //read the rest from original reade
        int lengthRead = in.read(cbuf, off + lengthPutFromOverflow, len - lengthPutFromOverflow);

        if (lengthRead > 0) {
            //if anything more could be read escape what was read with possible new overflow
            return lengthPutFromOverflow + XMLEscapeUtil.unicodeInPlaceEncode(cbuf, off + lengthPutFromOverflow, lengthRead, len - lengthPutFromOverflow, overflow);
        } else {
            //original reader is empty
            if (lengthPutFromOverflow > 0)
            {
                //there was some overflow to return
                return lengthPutFromOverflow;
            } else
            {
                //there is nothing more so return -1
                return -1;
            }

        }

    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }
}
