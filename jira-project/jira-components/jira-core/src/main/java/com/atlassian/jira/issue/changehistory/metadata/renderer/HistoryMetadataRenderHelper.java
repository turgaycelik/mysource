package com.atlassian.jira.issue.changehistory.metadata.renderer;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataParticipant;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.Nullable;

public class HistoryMetadataRenderHelper
{
    private final ApplicationProperties applicationProperties;
    private final I18nHelper i18n;

    public HistoryMetadataRenderHelper(final ApplicationProperties applicationProperties, final I18nHelper i18nHelper)
    {
        this.applicationProperties = applicationProperties;
        this.i18n = i18nHelper;
    }

    /**
     * @return the avatar url for the participant.
     */
    @Nullable
    public String getParticipantAvatarUrl(@Nullable HistoryMetadataParticipant participant)
    {
        if (participant != null && participant.getAvatarUrl() != null)
        {
            try
            {
                URI avatarUri = new URI(participant.getAvatarUrl());
                if (!avatarUri.isAbsolute())
                {
                    return StringUtils.stripEnd(getBaseUrl(), "/") + "/" + StringUtils.stripStart(participant.getAvatarUrl(), "/");
                }
            }
            catch (URISyntaxException e)
            {
                // ignored, just return the url as is
            }
            return participant.getAvatarUrl();
        }
        return null;
    }

    private String getBaseUrl()
    {
        return applicationProperties.getString(APKeys.JIRA_BASEURL);
    }

    /**
     * @return the participant's displayname
     */
    @Nullable
    public String getParticipantName(@Nullable HistoryMetadataParticipant participant)
    {
        if (participant != null)
        {
            if (i18n.isKeyDefined(participant.getDisplayNameKey()))
            {
                return i18n.getText(participant.getDisplayNameKey(), participant.getId());
            }
            else if (!Strings.isNullOrEmpty(participant.getDisplayName()))
            {
                return participant.getDisplayName();
            }

            return participant.getId();
        }

        return null;
    }
}
