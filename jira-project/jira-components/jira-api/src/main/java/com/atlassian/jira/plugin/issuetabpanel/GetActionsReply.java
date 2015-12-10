package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Reply object used in the {@link IssueTabPanel2} SPI. This class is immutable as long as the IssueActions that it
 * contains are also immutable.
 * <p/>
 * Example usage:
 * <pre>
 *     return GetActionsReply.create(myPluginActions);
 * </pre>
 *
 * @see IssueTabPanel2
 * @since v5.0
 */
@PublicApi
@Immutable
final public class GetActionsReply
{
    /**
     * Creates a new GetActionsReply containing the provided actions.
     *
     * @param actions the IssueAction that the response will contain
     * @return a new GetActionsReply
     */
    public static GetActionsReply create(@Nullable IssueAction... actions)
    {
        return new GetActionsReply(actions != null ? Arrays.asList(actions) : null);
    }

    /**
     * Creates a new GetActionsReply containing the provided actions.
     *
     * @param actions the IssueActions that the response will contain
     * @return a new GetActionsReply
     */
    public static GetActionsReply create(@Nullable Iterable<? extends IssueAction> actions)
    {
        return new GetActionsReply(actions);
    }

    /**
     * The actions.
     */
    private final ImmutableList<IssueAction> actions;

    /**
     * Creates an new GetActionsReply.
     *
     * @param actions the IssueAction instances to add to the created GetActionsReply
     */
    private GetActionsReply(@Nullable Iterable<? extends IssueAction> actions)
    {
        this.actions = actions != null ? ImmutableList.copyOf(actions) : null;
    }

    /**
     * @return a list of IssueAction.
     */
    @Nullable
    @Internal
    public ImmutableList<IssueAction> actions()
    {
        return actions;
    }
}
