package com.atlassian.jira.configurator.ssl;

import javax.annotation.Nonnull;
import java.security.cert.X509Certificate;
import java.text.DateFormat;

import com.atlassian.jira.config.properties.JiraSystemProperties;

public class CertificatePrettyPrinter
{
    private static final String NEWLINE = JiraSystemProperties.getInstance().getProperty("line.separator");
    
    @Nonnull
    public static String prettyPrint(@Nonnull final X509Certificate cert)
    {
        final DateFormat dateFormater = DateFormat.getDateInstance(DateFormat.FULL);

        final StringBuilder builder = new StringBuilder();
        builder.append("SerialNumber: ").append(cert.getSerialNumber()).append(NEWLINE)
                .append("IssuerDN: ").append(cert.getIssuerDN().getName()).append(NEWLINE)
                .append("Start Date: ").append(dateFormater.format(cert.getNotBefore())).append(NEWLINE)
                .append("Final Date: ").append(dateFormater.format(cert.getNotAfter())).append(NEWLINE)
                .append("SubjectDN: ").append(cert.getSubjectDN().getName()).append(NEWLINE);
        return builder.toString();
    }
}
