package com.atlassian.jira.util;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.lang.StringUtils;

/**
 * Class that validates the value of a TCP/UDP port.
 *
 * @since v4.0
 */
@PublicApi
public final class PortUtil
{
    private PortUtil()
    {
    }

    public static final int MIN_PORT = 0;
    public static final int MAX_PORT = 0xFFFF;

    /**
     * Return the TCP/UDP port contained in the passed port string. Returns -1 if the passed string is not a valid port.
     *
     * @param port the string to get the port from.
     * @return a valid port if the string contains a valid port, or -1 otherwise.
     */
    public static int parsePort(String port)
    {
        if (StringUtils.isBlank(port))
        {
            return -1;
        }
        
        try
        {
            final int portInt = Integer.parseInt(port.trim());
            if (isValidPort(portInt))
            {
                return portInt;
            }
            else
            {
                return -1;
            }
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }

    /**
     * Tells the caller of the passed string represents a valid TCP/UDP port.
     *
     * @param port the string to check.
     * @return true if the passed string is a valid port or false otherwise.
     */
    public static boolean isValidPort(String port)
    {
        return parsePort(port) >= 0;
    }

    /**
     * Tells the caller of the passed int represents a valid TCP/UDP port.
     *
     * @param port the int to check.
     * @return true if the passed string is a valid port or false otherwise.
     */
    public static boolean isValidPort(int port)
    {
        return port >= MIN_PORT && port <= MAX_PORT;
    }
}
