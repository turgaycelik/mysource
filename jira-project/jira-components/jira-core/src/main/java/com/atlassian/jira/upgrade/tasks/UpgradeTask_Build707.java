package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.ofbiz.core.entity.GenericValue;

import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

/**
 * Copying trackbacks to remote issue links
 *
 * @since v5.0
 */
public class UpgradeTask_Build707 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build707.class);

    public static final String JIRA_OPTION_TRACKBACK_RECEIVE = "jira.option.trackback.receive";

    public static final String RELATIONSHIP = "Trackbacks";
    public static final String APPLICATION_TYPE = "legacy-trackbacks";
    public static final String GLOBAL_ID_PREFIX = "com.atlassian.jira:legacy-trackbacks-";
    public static final String TRACKBACK_ENTITY_NAME = "TrackbackPing";
    private IncomingTrackbacks incomingTrackbacks;

    public UpgradeTask_Build707(final ApplicationPropertiesStore applicationPropertiesStore)
    {
        super(false);
        this.incomingTrackbacks = new IncomingTrackbacks(applicationPropertiesStore);
    }

    @Override
    public String getShortDescription()
    {
        return "Copying all trackbacks into remote issue links";
    }

    @Override
    public String getBuildNumber()
    {
        return "707";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        if (!incomingTrackbacks.setting().wasOn())
        {
            log.info("Trackbacks disabled so not trying to convert to remote issue links.");
            return;
        }

        final OfBizDelegator delegator = getOfBizDelegator();
        OfBizListIterator trackbackIterator = null;

        try
        {
            long currentIndex = 0;

            trackbackIterator = delegator.findListIteratorByCondition("TrackbackPing", null);
            for (GenericValue trackback = trackbackIterator.next(); trackback != null; trackback = trackbackIterator.next())
            {
                final Long trackbackId = trackback.getLong("id");
                log.debug("Converting trackback: " + trackbackId.toString());

                final MapBuilder<String, Object> builder = MapBuilder.newBuilder();
                builder.add("issueid", trackback.getLong("issue"));
                builder.add("globalid", GLOBAL_ID_PREFIX + trackbackId.toString());
                builder.add("title", trackback.getString("title"));
                builder.add("summary", trackback.getString("excerpt"));
                final String url = trackback.getString("url");
                builder.add("url", url);
                builder.add("relationship", RELATIONSHIP);
                builder.add("applicationtype", APPLICATION_TYPE);
                builder.add("applicationname", trackback.getString("blogname"));

                // lets check to ensure that the url is valid and only createthe link if it is.
                if (isValidUrl(url))
                {
                    log.debug("Trackback url is valid, creating remote issue link");
                    delegator.createValue("RemoteIssueLink", builder.toMap());
                }
                else
                {
                    log.warn("Could not convert url string into URI. Skipping trackback. trackbackId:'" + trackbackId + "' url:'" + url + "'");
                }

                // log progress
                currentIndex++;
                if (currentIndex % 1000 == 0)
                {
                    log.info("Converted " + currentIndex + " trackbacks.");
                }
            }
        }
        finally
        {
            if (trackbackIterator != null)
            {
                trackbackIterator.close();
            }
        }
    }

    private static class IncomingTrackbacks
    {
        private final ApplicationPropertiesStore applicationPropertiesStore;
        private final Setting setting;

        private IncomingTrackbacks(final ApplicationPropertiesStore applicationPropertiesStore)
        {
            this.applicationPropertiesStore = applicationPropertiesStore;
            setting = new Setting();
        }

        public Setting setting()
        {
            return setting;
        }
        
        private class Setting
        {
            /**
             * Whether the JIRA instance being upgraded had incoming trackbacks set to &quot;ON&quot;
             *
             * @return <tt>true</tt>, if the JIRA instance being upgraded had incoming trackbacks set to &quot;ON&quot;;
             * otherwise, <tt>false</tt>.
             */
            public boolean wasOn()
            {
                if (applicationPropertiesStore.existsInDb(JIRA_OPTION_TRACKBACK_RECEIVE))
                {
                    return applicationPropertiesStore.getOption(JIRA_OPTION_TRACKBACK_RECEIVE);
                }
                else
                {
                   return Boolean.valueOf
                           (
                                   defaultIfEmpty
                                           (
                                                   applicationPropertiesStore.getOverlayedString
                                                           (JIRA_OPTION_TRACKBACK_RECEIVE), 
                                                   "true"
                                           )
                           );
                }
            }
        }
    }

    /**
     * Pretty much a copy from {@link com.atlassian.jira.bc.issue.link.DefaultRemoteIssueLinkService}
     *
     * @param url The url to validate
     * @return true if valid, otherwise - false
     */
    private boolean isValidUrl(final String url)
    {
        if (StringUtils.isBlank(url))
        {
            return true;
        }

        try
        {
            final URI uri = new URI(url);

            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())))
            {
                return false;
            }
            if (uri.getHost() == null)
            {
                return false;
            }
        }
        catch (final URISyntaxException e)
        {
            return false;
        }
        return true;
    }
}
