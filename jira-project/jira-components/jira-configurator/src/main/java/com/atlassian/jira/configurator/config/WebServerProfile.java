package com.atlassian.jira.configurator.config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum WebServerProfile
{
    Disabled(false, false, "Disabled"),
    HttpOnly(true, false, "HTTP only",
            ComplexConfigurationReason.MultipleHttpConnectors,
            ComplexConfigurationReason.SslEnabledAprConnector
    ),
    HttpRedirectedToHttps(true, true, "HTTP and HTTPs (redirect HTTP to HTTPs)",
            ComplexConfigurationReason.AnotherSecurityConstraintExisting,
            ComplexConfigurationReason.MultipleHttpConnectors,
            ComplexConfigurationReason.MultipleHttpsConnectors,
            ComplexConfigurationReason.SslEnabledAprConnector
    ),
    HttpsOnly(false, true, "HTTPs only",
            ComplexConfigurationReason.MultipleHttpsConnectors,
            ComplexConfigurationReason.SslEnabledAprConnector
    );

    private final boolean httpEnabled;
    private final boolean httpsEnabled;
    private final String label;
    private final List<ComplexConfigurationReason> problems;

    private WebServerProfile(final boolean httpEnabled, final boolean httpsEnabled, @Nonnull final String label, @Nonnull final ComplexConfigurationReason...problems) {
        this.httpEnabled = httpEnabled;
        this.httpsEnabled = httpsEnabled;
        this.label = label;
        this.problems = Arrays.asList(problems);
    }

    public boolean isHttpEnabled()
    {
        return httpEnabled;
    }

    public boolean isHttpsEnabled()
    {
        return httpsEnabled;
    }

    @Nonnull
    public String getLabel()
    {
        return label;
    }

    public boolean isPreventedBy(@Nonnull final List<ComplexConfigurationReason> complexConfigurationReasons)
    {
        return !Collections.disjoint(problems, complexConfigurationReasons);
    }

    public static WebServerProfile retrieveByFlags(final boolean httpEnabled, final boolean httpsEnabled)
    {
        for (final WebServerProfile profile : values())
        {
            if (profile.httpEnabled == httpEnabled && profile.httpsEnabled == httpsEnabled)
            {
                return profile;
            }
        }
        throw new IllegalArgumentException("unknown profile with httpEnabled " + httpEnabled + " and httpsEnabled " + httpsEnabled);
    }

    @Nonnull
    public static List<WebServerProfile> getUsableProfiles(@Nonnull final Settings settings)
    {
        final WebServerProfile[] allProfiles = values();
        final List<ComplexConfigurationReason> complexConfigurationReasons = settings.getComplexConfigurationReasons();
        final List<WebServerProfile> result = new ArrayList<WebServerProfile>(allProfiles.length);
        for (final WebServerProfile profile : allProfiles)
        {
            if (!profile.isPreventedBy(complexConfigurationReasons))
            {
                result.add(profile);
            }
        }
        return result;
    }
}
