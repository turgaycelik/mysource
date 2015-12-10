package com.atlassian.jira.mail;

import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import java.util.regex.Pattern;

/**
 * This is a wrapper class around the Email class from atlassian-mail. Its purpose is to
 * ensure that the proper headers are present or absent as per JIRA configuration <em>on construction</em>.
 *
 * e.g. Precedence Bulk and X-JIRA-FingerPrint
 */
public class Email extends com.atlassian.mail.Email
{
    /**
     * JIRA's custom mail header used to contain a magic fingerprint string "unique" to a JIRA instance, used for
     * identification purposes.
     */
    public static final String HEADER_JIRA_FINGER_PRINT = "X-JIRA-FingerPrint";
    private static final Pattern LINE_BREAKS = Pattern.compile("[\r\n]");

    private ConfigurationDependencies configurationDependencies;

    /**
     * Dependencies that are resolved dynamically by the {@link ComponentAccessor}.
     */
    private static final ConfigurationDependencies PRODUCTION_DEPENDENCIES = new ConfigurationDependencies()
    {
        public ApplicationProperties getApplicationProperties()
        {
            return ComponentAccessor.getApplicationProperties();
        }

        public JiraApplicationContext getJiraApplicationContext()
        {
            return ComponentAccessor.getComponentOfType(JiraApplicationContext.class);
        }
    };


    /**
     * Constructor specifying only the recipient.
     *
     * @param to recipient.
     */
    public Email(String to)
    {
        this(to, PRODUCTION_DEPENDENCIES);
    }

    /**
     * Constructor including cc and bcc addresses.
     *
     * @param to  recipient.
     * @param cc  carbon copy recipient.
     * @param bcc blind carbon copy recipient.
     */
    public Email(String to, String cc, String bcc)
    {
        this(to, cc, bcc, PRODUCTION_DEPENDENCIES);
    }

    Email(String to, ConfigurationDependencies configurationDependencies)
    {
        super(to);
        this.configurationDependencies = configurationDependencies;
        removePrecedenceHeaderIfNeeded();
        addFingerPrintHeader();
        setEncoding(getMailEncoding());
    }

    Email(String to, String cc, String bcc, ConfigurationDependencies configurationDependencies)
    {
        super(to, cc, bcc);
        this.configurationDependencies = configurationDependencies;
        removePrecedenceHeaderIfNeeded();
        addFingerPrintHeader();
        setEncoding(getMailEncoding());
    }

    private void removePrecedenceHeaderIfNeeded()
    {
        if (isExcludePrecedenceHeader())
        {
            removeHeader("Precedence");
            removeHeader("Auto-Submitted"); // see JRA-15325
        }
    }

    private String getMailEncoding()
    {
        return configurationDependencies.getApplicationProperties().getMailEncoding();
    }

    private boolean isExcludePrecedenceHeader()
    {
        ApplicationProperties properties = configurationDependencies.getApplicationProperties();
        return properties.getOption(APKeys.JIRA_OPTION_EXCLUDE_PRECEDENCE_EMAIL_HEADER);
    }

    private void addFingerPrintHeader()
    {
        addHeader(HEADER_JIRA_FINGER_PRINT, configurationDependencies.getJiraApplicationContext().getFingerPrint());
    }

    @Override
    public com.atlassian.mail.Email setSubject(String subject)
    {
        // MAIL-79 JRA-23494  Kill line breaks here to prevent corrupted e-mails
        if (subject != null)
        {
            subject = LINE_BREAKS.matcher(subject).replaceAll(" ");
        }
        return super.setSubject(subject);
    }

    interface ConfigurationDependencies
    {
        ApplicationProperties getApplicationProperties();
        JiraApplicationContext getJiraApplicationContext();
    }

}
