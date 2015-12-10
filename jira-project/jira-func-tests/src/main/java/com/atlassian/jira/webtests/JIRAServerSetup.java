package com.atlassian.jira.webtests;

import com.icegreen.greenmail.util.ServerSetup;

/**
 * Extends {@link com.icegreen.greenmail.util.ServerSetup} so that the port can be incremented/changed.
 * This is so that, if the port is already in use a different port can be used with the same instance
 * and not having to create a whole new one.
 * <p/> 
 * Defines a series of non-default ports for JIRA test purposes.
 * The ports for the various protocols are the default ones plus an offset of {@link #PORT_OFFSET}.
 * i.e.
 * <table>
 * <tr><td>smtp</td><td>25025</td></tr>
 * <tr><td>smtps</td><td>25465</td></tr>
 * <tr><td>pop3</td><td>25110</td></tr>
 * <tr><td>pop3s</td><td>25995</td></tr>
 * <tr><td>imap</td><td>25143</td></tr>
 * <tr><td>imaps</td><td>25993</td></tr>
 * </table>
 * Use {@link com.icegreen.greenmail.util.ServerSetup} for default ports
 *
 * @see com.icegreen.greenmail.util.ServerSetup
 */
public class JIRAServerSetup extends ServerSetup
{
    private int port;
    public static final String HOST = "localhost";
    public static final int PORT_OFFSET = 25000;

    public static final JIRAServerSetup SMTP = new JIRAServerSetup(25 + PORT_OFFSET, HOST, JIRAServerSetup.PROTOCOL_SMTP);
    public static final JIRAServerSetup SMTPS = new JIRAServerSetup(465 + PORT_OFFSET, HOST, JIRAServerSetup.PROTOCOL_SMTPS);
    public static final JIRAServerSetup POP3 = new JIRAServerSetup(110 + PORT_OFFSET, HOST, JIRAServerSetup.PROTOCOL_POP3);
    public static final JIRAServerSetup POP3S = new JIRAServerSetup(995 + PORT_OFFSET, HOST, JIRAServerSetup.PROTOCOL_POP3S);
    public static final JIRAServerSetup IMAP = new JIRAServerSetup(143 + PORT_OFFSET, HOST, JIRAServerSetup.PROTOCOL_IMAP);
    public static final JIRAServerSetup IMAPS = new JIRAServerSetup(993 + PORT_OFFSET, HOST, JIRAServerSetup.PROTOCOL_IMAPS);

    public static final JIRAServerSetup[] SMTP_POP3 = new JIRAServerSetup[] { SMTP, POP3 };
    public static final JIRAServerSetup[] SMTP_IMAP = new JIRAServerSetup[] { SMTP, IMAP };
    public static final JIRAServerSetup[] SMTP_POP3_IMAP = new JIRAServerSetup[] { SMTP, POP3, IMAP };

    public static final JIRAServerSetup[] SMTPS_POP3S = new JIRAServerSetup[] { SMTPS, POP3S };
    public static final JIRAServerSetup[] SMTPS_POP3S_IMAPS = new JIRAServerSetup[] { SMTPS, POP3S, IMAPS };
    public static final JIRAServerSetup[] SMTPS_IMAPS = new JIRAServerSetup[] { SMTPS, IMAPS };

    public static final JIRAServerSetup[] ALL = new JIRAServerSetup[] { SMTP, SMTPS, POP3, POP3S, IMAP, IMAPS };

    public JIRAServerSetup(int port, String bindAddress, String protocol)
    {
        super(port, bindAddress, protocol);
        this.port = port;
    }

    /**
     * Overriden so that it uses the local port value and not the super's
     * @return port
     */
    public int getPort()
    {
        return this.port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Increment the port by 1
     */
    public void incrementPort()
    {
        ++this.port;
    }
}
