package com.atlassian.jira.configurator.console;

import java.io.Console;
import java.io.IOException;

/**
 * @since v5.1
 */
public interface ConsoleProvider
{
    public String readLine() throws IOException;
    public String readLine(String prompt) throws IOException;
    public boolean readYesNo(String prompt, boolean defaultValue) throws IOException;
    public String readPassword(String prompt) throws IOException;
    public char readFirstChar(String prompt) throws IOException;
    public void print(String text);
    public void println();
    public void println(String text);
    public void printErrorMessage(String errorMessage);
    public void printErrorMessage(Throwable ex);
    public void flush();

    public static class Factory
    {
        private static final ConsoleProvider INSTANCE = initInstance();

        public static ConsoleProvider getInstance()
        {
            return INSTANCE;
        }

        private static ConsoleProvider initInstance()
        {
            final Console console = System.console();
            if (console != null)
            {
                return new DeviceConsoleProvider(console);
            }
            return new StreamConsoleProvider(System.in, System.out);
        }
    }
}
