package com.atlassian.jira.webtests;

/**
 * A convinience holder for an array of {@link com.atlassian.jira.webtests.JIRAServerSetup} to increment each
 * setup's port by 1 when needed. You would increment the ports by 1 if any of the ports are already in use.
 * <p/>
 * You can also get specific server setups for the protocols: {@link JIRAServerSetup#PROTOCOL_SMTP smtp},
 * {@link JIRAServerSetup#PROTOCOL_POP3 pop3}, {@link JIRAServerSetup#PROTOCOL_IMAP imap} by the appropriate
 * getter methods.
 */
public class JIRAGreenMailSetup
{
    private final JIRAServerSetup[] serverSetups;

    public JIRAGreenMailSetup(JIRAServerSetup[] jiraServerSetups)
    {
        this.serverSetups = jiraServerSetups;
    }

    /**
     * Increment each {@link com.atlassian.jira.webtests.JIRAServerSetup JIRAServerSetup's} port by 1
     */
    public void incrementPorts()
    {
        for (JIRAServerSetup jiraServerSetup : serverSetups)
        {
            jiraServerSetup.incrementPort();
        }
    }

    public JIRAServerSetup[] getServerSetups()
    {
        return serverSetups;
    }

    public JIRAServerSetup getSmtpSetup()
    {
        return getServerSetup(JIRAServerSetup.PROTOCOL_SMTP);
    }

    public JIRAServerSetup getPop3Setup()
    {
        return getServerSetup(JIRAServerSetup.PROTOCOL_POP3);
    }

    public JIRAServerSetup getImapSetup()
    {
        return getServerSetup(JIRAServerSetup.PROTOCOL_IMAP);
    }

    public JIRAServerSetup getServerSetup(String protocol)
    {
        for (JIRAServerSetup serverSetup : serverSetups)
        {
            if (protocol.equals(serverSetup.getProtocol()))
            {
                return serverSetup;
            }
        }
        return null;
    }
}
