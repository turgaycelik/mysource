package com.atlassian.jira.util;

import net.sourceforge.jwebunit.WebTester;

public class ProgressPageControl
{
    public static void waitAndReload(final WebTester tester, final String formName, final String reloadButton,
                                     final String confirmButton)
    {
        final int maxWaitTimeMs = 2 * 60 * 1000;
        final int snoozeMs = 100;
        int timeWaitedMs = 0;
        while (true)
        {
            if (formName != null)
            {
                tester.setWorkingForm(formName);
            }
            if (tester.getDialog().hasSubmitButton("Acknowledge"))
            {
                break;
            }
            if (timeWaitedMs > maxWaitTimeMs)
            {
                throw new RuntimeException("Waiting timed out.");
            }
            try
            {
                Thread.sleep(snoozeMs);
                timeWaitedMs += snoozeMs;
                tester.submit(reloadButton);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
        tester.submit(confirmButton);
    }
}
