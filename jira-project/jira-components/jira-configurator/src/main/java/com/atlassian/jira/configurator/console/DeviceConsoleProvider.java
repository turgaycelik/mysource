package com.atlassian.jira.configurator.console;

import java.io.Console;
import java.io.IOException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Implements a console as backed by a Java 1.6 <code>System.console()</code> device.
 * This is preferred over the {@link StreamConsoleProvider} when <code>System.console()</code>
 * provides a non-<code>null</code> value, because it can suppress the display of passwords.
 *
 * @since v5.1
 */
public class DeviceConsoleProvider extends AbstractConsoleProvider
{
    private final Console console;

    public DeviceConsoleProvider(Console console)
    {
        super();
        this.console = notNull("console", console);
    }

    @Override
    public String readLine(String prompt) throws IOException
    {
        String line = console.readLine("%s> ", (prompt != null) ? prompt : "");
        if (line == null)
        {
            throw closed();
        }
        return line.trim();
    }

    @Override
    public String readPassword(String prompt) throws IOException
    {
        char[] line = (prompt != null) ? console.readPassword("%s> ", prompt) : console.readPassword();
        if (line == null)
        {
            throw closed();
        }
        return new String(line).trim();
    }

    @Override
    public void println()
    {
        console.format("\n");
    }

    @Override
    public void print(String text)
    {
        console.format("%s", text);
    }

    @Override
    public void println(String text)
    {
        console.format("%s\n", text);
    }

    @Override
    public void printErrorMessage(String errorMessage)
    {
        println("*** " + errorMessage);
    }

    @Override
    public void printErrorMessage(Throwable ex)
    {
        final String message = ex.getMessage();
        printErrorMessage((message != null) ? message : ex.toString());
    }

    @Override
    public void flush()
    {
        console.flush();
    }
}

