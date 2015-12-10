package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;

import javax.annotation.concurrent.Immutable;

/**
 * Reply object used in the {@link IssueTabPanel2} SPI.
 *
 * @see IssueTabPanel2
 * @since v5.0
 */
@PublicApi
@Immutable
public final class ShowPanelReply
{
    /**
     * Creates a new ShowPanelReply.
     *
     * @param show a boolean indicating whether to show the panel
     * @return a new ShowPanelReply
     */
    public static ShowPanelReply create(boolean show)
    {
        return new ShowPanelReply(show);
    }

    /**
     * Whether to show the panel.
     */
    private final boolean show;

    /**
     * Creates a new ShowPanelReply.
     *
     * @param show a boolean indicating whether to show the panel
     */
    private ShowPanelReply(boolean show)
    {
        this.show = show;
    }

    /**
     * @return a boolean indicating whether to show the panel
     */
    @Internal
    public boolean isShow()
    {
        return show;
    }
}
