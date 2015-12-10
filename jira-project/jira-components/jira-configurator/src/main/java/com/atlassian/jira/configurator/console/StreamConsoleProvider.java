package com.atlassian.jira.configurator.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Console provider based on <code>System.in</code> and <code>System.out</code>.
 * The {@link DeviceConsoleProvider} should be preferred when <code>System.console()</code>
 * returns a non-<tt>null</tt> value.
 *
 * @since v5.1
 */
public class StreamConsoleProvider extends AbstractConsoleProvider
{
    private final BufferedReader in;
    final PrintStream out;

    public StreamConsoleProvider(InputStream in, PrintStream out)
    {
        super();
        this.in = new BufferedReader(new InputStreamReader(notNull("in", in)));
        this.out = notNull("out", out);
    }

    protected String readLineImpl() throws IOException
    {
        String line = in.readLine();
        if (line == null)
        {
            throw closed();
        }
        return line.trim();
    }

    @Override
    public String readLine(String prompt) throws IOException
    {
        showPrompt(prompt);
        return readLineImpl();
    }


    @Override
    public String readPassword(String prompt) throws IOException
    {
        // Unfortunately, there really isn't anything realiable that we can do
        // to mask the password.  Historically, Sun recommended using a thread
        // that does System.out.println("\010*");  sleep(1); in a tight loop,
        // but if your stdout is not a terminal, that's just going to fill up
        // the output with garbage.  Since we are on 1.6+, we will use the
        // System.console() if it is available, so this won't matter.  Otherwise,
        // we'll just have to let it show.
        return readLine(prompt);
    }

    @Override
    public void print(String text)
    {
        out.print(text);
    }

    @Override
    public void println()
    {
        out.println();
    }

    @Override
    public void println(String text)
    {
        out.println(text);
    }

    @Override
    public void flush()
    {
        out.flush();
    }
}


