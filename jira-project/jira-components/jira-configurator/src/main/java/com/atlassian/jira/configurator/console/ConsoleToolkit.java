package com.atlassian.jira.configurator.console;

import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.config.Validator;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ConsoleToolkit
{
    private final ConsoleProvider console;

    public ConsoleToolkit(@Nonnull final ConsoleProvider console)
    {
        this.console = console;
    }

    public String menuItemAndValue(String label, Object value)
    {
        final StringBuilder sb = new StringBuilder(128).append(label);
        while (sb.length() < 40)
        {
            sb.append(' ');
        }
        sb.append(": ");
        if (value != null)
        {
            sb.append(value);
        }
        else
        {
            sb.append("(default)");
        }
        return (value != null) ? (label + " (" + value + ")") : label;
    }

    public void showMenuItem(char key, String label)
    {
        console.println("  [" + key + "] " + label);
    }

    public void showDefaultMenuItem(char key, String label)
    {
        console.println("* [" + key + "] " + label);
    }

    public void showMenuItem(char key, String label, char defaultKey)
    {
        if (key == defaultKey)
        {
            showDefaultMenuItem(key, label);
        }
        else
        {
            showMenuItem(key, label);
        }
    }

    public char readMenuChoice(String menuName) throws IOException
    {
        return Character.toUpperCase(console.readFirstChar(menuName));
    }

    public <T> T askForPassword(@Nonnull final String label, @Nonnull final Validator<T> validator) throws IOException
    {
        while (true)
        {
            try
            {
                return validator.apply(label, console.readPassword(label));
            }
            catch (ValidationException e)
            {
                console.printErrorMessage(e);
            }
        }
    }

    public <T> T askFor(@Nonnull final String label, @Nonnull final Validator<T> validator) throws IOException
    {
        while (true)
        {
            try
            {
                return validator.apply(label, console.readLine(label));
            }
            catch (ValidationException e)
            {
                console.printErrorMessage(e);
            }
        }
    }
}
