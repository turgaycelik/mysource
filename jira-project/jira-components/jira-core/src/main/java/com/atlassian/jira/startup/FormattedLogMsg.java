package com.atlassian.jira.startup;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.config.properties.JiraSystemProperties;

/**
 * This class will allow a series of messages to be formated/indented into the one log4j ouput.
 * <p/>
 * The idea is to build up a message and then call printMessage(), after which the contents of the message will be cleared
 * and the formatter can be used again to output more messages.
 *
 * @since v3.13
 */
public class FormattedLogMsg
{
    private static final String NEW_LINE = JiraSystemProperties.getInstance().getProperty("line.separator");
    private static final String INDENT_STR = "     "; // 5 spaces
    private static final int MAX_DESC_WIDTH = 45;
    private static final int MAX_VALUE_WIDTH = 55;
    private static final int MAX_STAR_WIDTH = (MAX_DESC_WIDTH + MAX_VALUE_WIDTH) * 2;

    private final Logger log;
    private final List<String> msgList = new ArrayList<String>();

    /**
     * Use this constructor if you dont intended to write to a Logger
     */
    public FormattedLogMsg()
    {
        this(null);
    }

    public FormattedLogMsg(final Logger log)
    {
        this.log = log;
    }

    public void add(final Object message)
    {
        add(message, 0);
    }

    public void add(final Object message, final int indentLevel)
    {
        final StringBuilder sb = new StringBuilder();

        if (indentLevel > 0)
        {
            final String indentStr = StringUtils.repeat(INDENT_STR, indentLevel);
            sb.append(indentStr);
        }
        sb.append(String.valueOf(message));

        msgList.add(sb.toString());
    }

    public void addAll(final Collection<String> collection)
    {
        msgList.addAll(collection);
    }

    public void outputProperty(final String propertyDesc)
    {
        outputPropertyImpl(propertyDesc, null, null, 1);
    }

    public void outputProperty(final String propertyDesc, final String propertyValue)
    {
        outputPropertyImpl(propertyDesc, propertyValue, null, 1);
    }

    public void outputProperty(final String propertyDesc, final String propertyValue, final String splitStr)
    {
        outputPropertyImpl(propertyDesc, propertyValue, splitStr, 1);
    }

    public void outputProperty(final String propertyDesc, final String propertyValue, final int indentLevel)
    {
        outputPropertyImpl(propertyDesc, propertyValue, null, indentLevel);
    }

    public void outputProperty(final String propertyDesc, final String propertyValue, final String splitStr, final int indentLevel)
    {
        outputPropertyImpl(propertyDesc, propertyValue, splitStr, indentLevel);
    }

    private void outputPropertyImpl(String propertyDesc, String propertyValue, final String splitStr, final int indentLevel)
    {
        propertyDesc = propertyDesc == null ? "" : propertyDesc.trim();
        propertyValue = propertyValue == null ? "" : propertyValue.trim();

        final String indentStr = StringUtils.repeat(INDENT_STR, indentLevel);

        final StringBuilder sb = new StringBuilder();
        sb.append(indentStr);
        sb.append(propertyDesc);
        // how many spaces do we want
        final int spacesLen = Math.max(MAX_DESC_WIDTH - propertyDesc.length(), 0);
        sb.append(StringUtils.repeat(" ", spacesLen));
        sb.append(" : ");

        // is the value
        final int splitIndex = splitStr == null ? -1 : propertyValue.indexOf(splitStr);
        if ((propertyValue.length() > MAX_VALUE_WIDTH) && (splitIndex != -1))
        {
            final String splitValue = indentValue(propertyValue, splitStr, true, indentLevel);
            sb.append(splitValue);
        }
        else
        {
            propertyValue = indentValue(propertyValue, NEW_LINE, false, indentLevel);
            sb.append(propertyValue);
        }
        msgList.add(sb.toString());
    }

    public void outputHeader(final String header)
    {
        final StringBuilder sb = new StringBuilder();
        // is the previous message a new line
        boolean addNewLine = !msgList.isEmpty();
        if (addNewLine)
        {
            final String prevMsg = String.valueOf(msgList.get(msgList.size() - 1));
            if (prevMsg.equals(NEW_LINE))
            {
                addNewLine = false;
            }
        }
        if (addNewLine)
        {
            sb.append(NEW_LINE);
        }
        sb.append("___ ");
        sb.append(header);
        sb.append(" _");
        final int spacesLen = Math.max(MAX_DESC_WIDTH - sb.length(), 0);
        sb.append(StringUtils.repeat("_", spacesLen));
        sb.append(NEW_LINE);
        msgList.add(sb.toString());
    }

    private String indentValue(final String propertyValue, final String splitStr, final boolean reappendSplitStr, final int indentlevel)
    {
        int splitIndex = propertyValue.indexOf(splitStr);
        if (splitIndex == -1)
        {
            return propertyValue;
        }
        int lastIndex = 0;
        final String indentStr = StringUtils.repeat(INDENT_STR, indentlevel);
        final StringBuilder sb = new StringBuilder();
        while (splitIndex != -1)
        {
            final int splitStrLen = splitStr.length();
            final String splitValue = propertyValue.substring(lastIndex, splitIndex);
            if (lastIndex > 0)
            {
                sb.append(NEW_LINE);
                sb.append(indentStr);
                sb.append(StringUtils.repeat(" ", MAX_DESC_WIDTH));
                sb.append("   "); // equals the " : "
            }
            sb.append(splitValue);
            if (reappendSplitStr)
            {
                sb.append(splitStr);
            }
            lastIndex = splitIndex + splitStrLen;
            splitIndex = propertyValue.indexOf(splitStr, lastIndex);
        }
        if (lastIndex < propertyValue.length())
        {
            final String splitValue = propertyValue.substring(lastIndex);
            sb.append(NEW_LINE);
            sb.append(indentStr);
            sb.append(StringUtils.repeat(" ", MAX_DESC_WIDTH));
            sb.append("   "); // equals the " : "
            sb.append(splitValue);
        }
        return sb.toString();
    }

    /**
     * Prints the contents of the current formatted messages to the Logger, using the specified
     * LogLevel. * stars will be placed around the text in the log.
     * <p/>
     * The messages data will be cleared after this call, ready for new message output.
     *
     * @param logLevel the log4j log level to output as
     */
    public void printMessage(final Level logLevel)
    {
        printMessageImpl(msgList, logLevel, true);
    }

    /**
     * Prints the contents of the current formatted messages to the Logger, using the specified
     * LogLevel. * stars will be placed around the text in the log if useStars is true.
     * <p/>
     * The messages data will be cleared after this call, ready for new output.
     *
     * @param logLevel the log4j log level to output as
     * @param useStars if true * characters will be placed around the message output
     */
    public void printMessage(final Level logLevel, final boolean useStars)
    {
        printMessageImpl(msgList, logLevel, useStars);
    }

    private void printMessageImpl(final Collection<String> messages, final Level logLevel, final boolean useStars)
    {
        final String line = toStringImpl(messages, useStars);
        if (log != null)
        {
            log.log(logLevel, line);
        }
        resetState();
    }

    /**
     * This will return the FormattedLogMsg as a String.  Calling this method does not reset the state of
     * the log message in the way that printMessage() does
     *
     * @return the the FormattedLogMsg as a String
     */
    @Override
    public String toString()
    {
        return toStringImpl(msgList, false);
    }

    private String toStringImpl(final Collection<String> messages, final boolean useStars)
    {
        if ((messages == null) || messages.isEmpty())
        {
            return "";
        }

        int maxLength = 0;
        for (final String message : messages)
        {
            maxLength = Math.max(message.length(), maxLength);
            // put some limit on it
            maxLength = Math.min(MAX_STAR_WIDTH, maxLength);
        }

        final StringBuilder line = new StringBuilder().append(NEW_LINE).append(NEW_LINE);
        if (useStars)
        {
            line.append(StringUtils.repeat("*", maxLength)).append(NEW_LINE);
        }
        for (final String message : messages)
        {
            line.append(message).append(NEW_LINE);
        }
        if (useStars)
        {
            line.append(StringUtils.repeat("*", maxLength)).append(NEW_LINE);
        }
        return line.toString();
    }

    private void resetState()
    {
        msgList.clear();
    }
}
