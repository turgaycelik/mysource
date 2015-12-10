package com.atlassian.jira.configurator.config;

import javax.annotation.Nonnull;

public enum ComplexConfigurationReason
{
    AnotherSecurityConstraintExisting("There are multiple security-constraint tags existing in the web.xml."),
    MultipleHttpConnectors("There are multiple HTTP connector tags existing in the server.xml."),
    MultipleHttpsConnectors("There are multiple SSL-enabled HTTP connector tags existing in the server.xml."),
    SslEnabledAprConnector("The SSL-enabled HTTP connector is configured to use the Apache Portable Runtime (APR). This"
            + " connector is currently not supported.");
    
    private final String description;

    private ComplexConfigurationReason(@Nonnull final String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
}
