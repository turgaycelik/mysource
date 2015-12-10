package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.issue.MutableIssue;
import com.google.common.base.Preconditions;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Holder class for an issue's GenericValue and MutableIssue representations. When any of the methods in this class is
 * called for the first time it will attempt to read the issue and it will store the results (which may be null if the
 * issue does not exist). Subsequent calls will use the previously stored issue.
 * <p/>
 * This class always operates on the selected issue, as returned by the {@link com.atlassian.jira.web.action.issue.SelectedIssue.Getter}.
 */
final class SelectedIssue
{
    private final Getter getter;

    // null unless explicitly set or fetched from the database
    private GetterResult issueResult = null;

    /**
     * Creates a new SelectedIssue instance.
     *
     * @param getter a Getter that will be used to read the selected issue
     */
    SelectedIssue(@Nonnull Getter getter)
    {
        this.getter = Preconditions.checkNotNull(getter, "getter");
    }

    /**
     * @return the issue's GenericValue representation
     */
    @Nullable
    GenericValue genericValue()
    {
        return fetchIssueResult().genericValue;
    }

    /**
     * @return the issue's MutableObject representation
     */
    @Nullable
    MutableIssue object()
    {
        return fetchIssueResult().object;
    }

    /**
     * Sets the selected issue object to the given MutableIssue. If the <code>issueObject</code> parameter is null then
     * the selected issue will simply be reset, and will be fetched from the database the next time it is accessed.
     *
     * @param issueObject a MutableIssue
     */
    void setObject(@Nullable MutableIssue issueObject)
    {
        issueResult = issueObject != null ? new GetterResult(issueObject) : null;
    }

    /**
     * @return a boolean indicating whether the issue exists
     */
    boolean exists()
    {
        return fetchIssueResult().genericValue != null;
    }

    /**
     * Lazily reads the selected issue using {@link AbstractIssueSelectAction#getIssueResultFromRequestOrDatabase()}
     * into this SelectedIssue's <code>issueResult</code> field. The issue is added to the user's issue history when it
     * is first loaded from the database.
     *
     * @return a GetterResult
     */
    private GetterResult fetchIssueResult()
    {
        if (issueResult == null)
        {
            issueResult = getter.get();
        }

        return issueResult;
    }

    /**
     * SPI for the issue loader. Implementors of this interface need to know what the currently "selected" issue is and
     * how to load it from the database or from elsewhere.
     */
    interface Getter
    {
        /**
         * @return a GetterResult containing the selected issue (which may be null)
         */
        @Nonnull
        GetterResult get();
    }

    /**
     * Holder for memo-ised .
     */
    static final class GetterResult
    {
        final GenericValue genericValue;
        final MutableIssue object;

        GetterResult(@Nullable MutableIssue object)
        {
            this.object = object;
            this.genericValue = object != null ? object.getGenericValue() : null;
        }
    }
}
