package com.atlassian.jira.util.system;

import com.atlassian.jira.util.IOUtil;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.StringWriter;

/**
 * Utilty for displaying some random text associated with a particular release.
 * <p>
 * For instance we need to display whether a release is Standalone, WAR or source.
 * We may in the future want to display information for bespoke customisations.
 */
public class ReleaseInfo
{
    private static final Logger log = Logger.getLogger(ReleaseInfo.class);

    private static final String FILE_NAME = "release.info";

    public static ReleaseInfo getReleaseInfo(Class whereToLook)
    {
        try
        {
            final InputStream resourceStream = whereToLook.getResourceAsStream(FILE_NAME);
            if (resourceStream == null)
            {
                return new ReleaseInfo("unknown", false);
            }
            StringWriter writer = new StringWriter();
            IOUtil.copy(resourceStream, writer);
            return new ReleaseInfo(writer.toString(), true);
        }
        catch (Exception e)
        {
            log.warn(e);
            return new ReleaseInfo("Could not get release info: " + e.toString(), false);
        }
    }

    private final String releaseInfo;
    private final boolean infoSet;

    private ReleaseInfo(String releaseInfo, boolean infoSet)
    {
        this.releaseInfo = releaseInfo;
        this.infoSet = infoSet;
    }

    /**
     * Gets the contents of a file in this package called "release.info".
     */
    public String getInfo()
    {
        return releaseInfo;
    }

    /**
     * Does the release.info file exist?
     */
    public boolean hasInfo()
    {
        return infoSet;
    }
}