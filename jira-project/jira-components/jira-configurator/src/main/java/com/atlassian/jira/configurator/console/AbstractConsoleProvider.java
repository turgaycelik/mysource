package com.atlassian.jira.configurator.console;

import java.io.IOException;

/**
 * Provides a few tools for the console provider's to use and
 * implements some of the convenience methods.
 *
 * @since v5.1
 */
abstract class AbstractConsoleProvider implements ConsoleProvider
{
    protected IOException closed()
    {
        return new IOException("Console input stream has been closed");
    }

    protected void showPrompt(String prompt)
    {
        if (prompt != null)
        {
            print(prompt);
        }
        print("> ");
        flush();
    }

    @Override
    public String readLine() throws IOException
    {
        return readLine(null);
    }

    @Override
    public char readFirstChar(String prompt) throws IOException
    {
        final String value = readLine(prompt);
        return (value.length() > 0) ? value.charAt(0) : '\n';
    }


    @Override
    public boolean readYesNo(String prompt, boolean defaultValue) throws IOException
    {
        if (prompt != null)
        {
            prompt += ' ';
        }
        else
        {
            prompt = "";
        }
        prompt += defaultValue ? " ([Y]/N)? " : " (Y/[N])? ";
        while (true)
        {
            switch (readFirstChar(prompt))
            {
                case '\r':
                case '\n':
                    return defaultValue;
                case 'Y':
                case 'y':
                case 'T':
                case 't':
                case '1':
                    return true;
                case 'N':
                case 'n':
                case 'F':
                case 'f':
                case '0':
                    return false;
            }
            println("*** Sorry, but I did not understand that.  Please enter Yes or No.");
        }
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
}
