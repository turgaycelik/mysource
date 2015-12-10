package com.atlassian.jira.mail;

import com.google.common.collect.ImmutableList;

import org.junit.Assert;
import org.junit.Test;

public class TestPasswordFilteringLogPrintStream
{
    @Test
    public void testFilterPassword() {
        DefaultMailLoggingManager.PasswordFilteringLogPrintStream filter = new DefaultMailLoggingManager.PasswordFilteringLogPrintStream("pass", null);
        Assert.assertEquals("fsdfsdpassxfds", filter.processLine("fsdfsdpassxfds"));
        Assert.assertEquals("fsdfsd pass xfds", filter.processLine("fsdfsd pass xfds"));
        Assert.assertEquals("fsdf PASS sd <hidden password> xfds", filter.processLine("fsdf PASS sd pass xfds"));
        Assert.assertEquals("LOGIN fsdfsd <hidden password>-xfds", filter.processLine("LOGIN fsdfsd pass-xfds"));
        Assert.assertEquals("xPASS fsdfsd <hidden password>", filter.processLine("xPASS fsdfsd pass"));
        Assert.assertEquals("PASS <hidden password>", filter.processLine("PASS pass"));
        Assert.assertEquals("pass", filter.processLine("pass"));
        Assert.assertEquals("PASS <hidden password> fsdfsdpassxfds pass", filter.processLine("PASS pass fsdfsdpassxfds pass"));
        Assert.assertEquals("PASSpass fsdfsdpassxfds <hidden password>", filter.processLine("PASSpass fsdfsdpassxfds pass"));

        DefaultMailLoggingManager.PasswordFilteringLogPrintStream filter2 = new DefaultMailLoggingManager.PasswordFilteringLogPrintStream("pa", null);
        Assert.assertEquals("fsdfsdpassxfds", filter2.processLine("fsdfsdpassxfds"));

        DefaultMailLoggingManager.PasswordFilteringLogPrintStream filter3 = new DefaultMailLoggingManager.PasswordFilteringLogPrintStream(null, null);
        Assert.assertEquals("xyz", filter3.processLine("xyz"));

        DefaultMailLoggingManager.PasswordFilteringLogPrintStream filter4 = new DefaultMailLoggingManager.PasswordFilteringLogPrintStream("-pa:/ss", null);
        Assert.assertEquals("fsdfsdpassxfds", filter4.processLine("fsdfsdpassxfds"));
        Assert.assertEquals("LOGIN fsdfsd <hidden password> xfds", filter4.processLine("LOGIN fsdfsd -pa:/ss xfds"));
        Assert.assertEquals("LOGIN fsdfsd <hidden password>", filter4.processLine("LOGIN fsdfsd -pa:/ss"));
        Assert.assertEquals("PASS <hidden password>", filter4.processLine("PASS -pa:/ss"));

        // testing funky regexp-like passwords
        for (String password : ImmutableList.of(".*\\d", "$$$", "[A-z][^a].*fsd", "???zfs[1-9]", "(abc)", "^ala"))
        {
            final DefaultMailLoggingManager.PasswordFilteringLogPrintStream filter5 = new DefaultMailLoggingManager.PasswordFilteringLogPrintStream(password, null);
            Assert.assertEquals("Handling password '" + password + "'", "PASS <hidden password> end", filter5.processLine("PASS " + password + " end"));
            // javamail escapes with " such strange regexp like passwords
            Assert.assertEquals("Handling password '" + password + "'", "PASS \"<hidden password>\"", filter5.processLine("PASS \"" + password + "\""));

        }

    }

}
