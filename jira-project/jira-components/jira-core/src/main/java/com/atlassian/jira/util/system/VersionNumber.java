/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package com.atlassian.jira.util.system;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Version Number holder. Implementation from the Mylyn JIRA Connector project.
 * Added isGreaterThan etc. convenience methods.
 * <p/>
 * Doesn't handle things like 3.3rc1 (will actually parse as 3.31 which will be > 3.3)
 *
 * @author Eugene Kuleshov
 */
public final class VersionNumber implements Comparable<VersionNumber>
{
    private final int major;

    private final int minor;

    private final int micro;

    private final String qualifier;

    private transient int hashCode;

    private static final String SEPARATOR = ".";

    public VersionNumber(final String version)
    {
        this(version, "\\.");
    }

    public VersionNumber(final String version, final String splitterRegex)
    {
        final String[] segments = version == null ? new String[0] : version.split(splitterRegex);
        major = segments.length > 0 ? parse(segments[0]) : 0;
        minor = segments.length > 1 ? parse(segments[1]) : 0;
        micro = segments.length > 2 ? parse(segments[2]) : 0;
        qualifier = segments.length == 0 ? "" : getQualifier(segments[segments.length - 1]);
    }

    private int parse(final String segment)
    {
        try
        {
            return segment.length() == 0 ? 0 : Integer.parseInt(getVersion(segment));
        }
        catch (final NumberFormatException e)
        {
            final StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < segment.length(); i++)
            {
                final char c = segment.charAt(i);
                if (Character.isDigit(c))
                {
                    buffer.append(c);
                }
            }
            return parse(buffer.toString());
        }
    }

    private String getVersion(final String segment)
    {
        final int n = segment.indexOf('-');
        return n == -1 ? segment : segment.substring(0, n);
    }

    private String getQualifier(final String segment)
    {
        final int n = segment.indexOf('-');
        return n == -1 ? "" : segment.substring(n + 1);
    }

    public int compareTo(final VersionNumber versionNumber)
    {
        if (major < versionNumber.major)
        {
            return -1;
        }
        else if (major > versionNumber.major)
        {
            return 1;
        }

        if (minor < versionNumber.minor)
        {
            return -1;
        }
        else if (minor > versionNumber.minor)
        {
            return 1;
        }

        if (micro < versionNumber.micro)
        {
            return -1;
        }
        else if (micro > versionNumber.micro)
        {
            return 1;
        }

        // qualifier is not needed to compare with min version
        return qualifier.compareTo(versionNumber.qualifier);
    }

    public boolean isGreaterThan(final VersionNumber v)
    {
        return compareTo(v) > 0;
    }

    public boolean isGreaterThanOrEquals(final VersionNumber v)
    {
        return compareTo(v) >= 0;
    }

    public boolean isLessThan(final VersionNumber v)
    {
        return compareTo(v) < 0;
    }

    public boolean isLessThanOrEquals(final VersionNumber v)
    {
        return compareTo(v) <= 0;
    }

    //TODO: Perhaps remove this method once PLUG-314 has been implemented.
    /**
     * Returns this version formatted as a valid OSGI version.
     *
     * @see http://www.osgi.org/javadoc/r4v41/org/osgi/framework/Version.html
     * @return A valid OSGI version string.
     */
    public String getOSGIVersion()
    {
        final StringBuilder ret = new StringBuilder();
        ret.append(major).append(SEPARATOR).append(minor).append(SEPARATOR).append(micro);
        if(StringUtils.isNotEmpty(qualifier))
        {
            ret.append(SEPARATOR).append(qualifier);
        }
        return ret.toString();
    }

    @Override
    public boolean equals(final Object that)
    {
        if (this == that)
        {
            return true;
        }
        if (!(that instanceof VersionNumber))
        {
            return false;
        }
        return compareTo((VersionNumber) that) == 0;
    }

    @Override
    public int hashCode()
    {
        // no need to be thread-safe as race is benign, hashCode is idempotent
        if (hashCode == 0)
        {
            final HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(major);
            builder.append(minor);
            builder.append(micro);
            builder.append(qualifier);
            hashCode = builder.toHashCode();
        }

        return hashCode;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(major));
        sb.append(SEPARATOR).append(Integer.toString(minor));
        if (micro > 0)
        {
            sb.append(SEPARATOR).append(Integer.toString(micro));
        }
        if (qualifier.length() > 0)
        {
            sb.append("-").append(qualifier);
        }
        return sb.toString();
    }
}