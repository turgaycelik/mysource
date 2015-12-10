/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.StringTokenizer;

public class ChangeHistoryUtils
{
    private static final Logger LOG = Logger.getLogger(ChangeHistoryUtils.class);
    public static final String CHANGED_FROM = "] changed from [";
    public static final String TO = "] to [";
    public static final String SET_TO = "] set to [";
    public static final String WAS_ADDED = "] was added";
    public static final String WAS_REMOVED = "] was removed";
    public static final String LINE_ENDING = "]";
    public static final String TERMINATOR = "\n";
    public static final String TERMINATOR_BR = "<br>\n";

    /**
     * Highlight a change log by breaking it into separate lines and highlighting each one
     */
    public static String highlight(String body, String colour)
    {
        String htmlBody = TextUtils.plainTextToHtml(body);
        StringTokenizer st = new StringTokenizer(htmlBody, "\n");

        StringBuilder buff = new StringBuilder(htmlBody.length());

        while (st.hasMoreTokens())
        {
            buff.append(highlightLine(st.nextToken() + "\n", colour));
        }

        return TextUtils.closeTags(buff.toString());
    }

    /**
     * There are a few different types of change logs, each appearing on separate lines:
     *
     * [Field] changed from [foo] to [bar]
     * [Field] set to [foo]
     * [Version] [X] was added
     * [Version] [Y] was removed
     */
    public static String highlightLine(String line, String colour)
    {
        boolean terminatorBr = false;

        // sometimes it ends with <br>\n and sometimes just \n - standardise and readd at the end of the line later
        if (line.endsWith(TERMINATOR_BR))
        {
            line = line.substring(0, line.length() - 5);
            terminatorBr = true;
        }
        else
        {
            line = line.substring(0, line.length() - 1);
        }

        // check if the string starts with '[xxx] '
        if (line.length() > 0)
        {
            if (line.charAt(0) == '[')
            {
                int firstCloseBracket = line.indexOf(']');
                if (firstCloseBracket > 0 && firstCloseBracket < 15)
                {
                    if (line.charAt(firstCloseBracket + 1) == ' ')
                    {
                        line = "[<font color=" + colour + ">" + line.substring(1, firstCloseBracket) + "</font>" + line.substring(firstCloseBracket);
                    }
                }
            }
        }

        // check if the text contains ] changed from [ .... ] to [    ]
        int changedFrom = line.indexOf(CHANGED_FROM);
        int to = line.indexOf(TO);

        if (changedFrom > 0 || to > 0)
        {
            if (changedFrom > 0)
            {
                line = line.substring(0, changedFrom) + CHANGED_FROM + "<font color=" + colour + ">" + line.substring(changedFrom + CHANGED_FROM.length());
                to = line.indexOf(TO); // update to if we changed it
            }

            if (to > 0)
            {
                line = line.substring(0, to) + "</font>" + TO + "<font color=" + colour + ">" + line.substring(to + TO.length());
            }
        }
        else // not [x] changed from [foo] to [bar] - try other possibilities
        {
            int setTo = line.indexOf(SET_TO);

            if (setTo > 0)
            {
                line = line.substring(0, setTo) + SET_TO + "<font color=" + colour + ">" + line.substring(setTo + SET_TO.length());
            }
            else if (line.endsWith(WAS_ADDED))
            {
                int secondBracket = line.indexOf('[', line.indexOf('[') + 1);
                if (secondBracket > 0)
                {
                    line = line.substring(0, secondBracket + 1) + "<font color=" + colour + ">" + line.substring(secondBracket + 1, line.lastIndexOf(WAS_ADDED)) + "</font>" + WAS_ADDED;
                }
            }
            else if (line.endsWith(WAS_REMOVED))
            {
                int secondBracket = line.indexOf('[', line.indexOf('[') + 1);
                if (secondBracket > 0)
                {
                    line = line.substring(0, secondBracket + 1) + "<font color=" + colour + ">" + line.substring(secondBracket + 1, line.lastIndexOf(WAS_REMOVED)) + "</font>" + WAS_REMOVED;
                }
            }
        }

        // close any font tag at the line ending
        if (line.endsWith(LINE_ENDING))
        {
            line = line.substring(0, line.length() - LINE_ENDING.length()) + "</font>" + LINE_ENDING;
        }

        if (terminatorBr)
            line += TERMINATOR_BR;
        else
            line += TERMINATOR;

        return line;
    }
}
