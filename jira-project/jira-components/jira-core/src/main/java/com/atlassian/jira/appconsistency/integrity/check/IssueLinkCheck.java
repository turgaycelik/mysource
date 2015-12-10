package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.transformer.DeleteEntityAmendmentTransformer;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import org.apache.commons.collections.CollectionUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Check that all Issue Links are associated with valid issues. ie. That the issue ids specified by the link exist
 * in the database.
 */
public class IssueLinkCheck extends EntityCheckImpl
{
    public IssueLinkCheck(final OfBizDelegator ofBizDelegator, final int id)
    {
        super(ofBizDelegator, id);
    }

    private List getCorruptIssueLinks() throws IntegrityException
    {
        final Set validIssueIds = new HashSet();

        OfBizListIterator issues = null;
        try
        {
            issues = getEntities("Issue");
            GenericValue issue = issues.next();
            while (issue != null)
            {
                validIssueIds.add(issue.getString("id"));
                issue = issues.next();
            }
        }
        finally
        {
            if (issues != null)
            {
                issues.close();
            }
        }

        OfBizListIterator issueLinks = null;
        final List corruptIssueLinks = new ArrayList();
        // Check if the relation exists for each issue
        try
        {
            issueLinks = getEntities("IssueLink");
            GenericValue issueLink = issueLinks.next();
            while (issueLink != null)
            {
                final String sourceId = issueLink.getString("source");
                final String destinationId = issueLink.getString("destination");
                if (!validIssueIds.contains(sourceId) || !validIssueIds.contains(destinationId))
                {
                    corruptIssueLinks.add(issueLink);
                }
                issueLink = issueLinks.next();
            }
        }
        catch (final Exception e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }
        finally
        {
            if (issueLinks != null)
            {
                // Close the iterator
                issueLinks.close();
            }

        }
        return corruptIssueLinks;
    }

    public List preview() throws IntegrityException
    {
        final Collection amendments = getCorruptIssueLinks();
        CollectionUtils.transform(amendments, new DeleteEntityAmendmentTransformer(Amendment.ERROR, getI18NBean().getText(
            "admin.integrity.check.issue.link.preview")));

        return new ArrayList(amendments);
    }

    public List correct() throws IntegrityException
    {
        final List corruptIssueLinks = getCorruptIssueLinks();
        if (!corruptIssueLinks.isEmpty())
        {
            try
            {
                ofBizDelegator.removeAll(corruptIssueLinks);
            }
            catch (final Exception e)
            {
                throw new IntegrityException(e);
            }
        }

        CollectionUtils.transform(corruptIssueLinks, new DeleteEntityAmendmentTransformer(Amendment.CORRECTION, getI18NBean().getText(
            "admin.integrity.check.issue.link.message")));
        return new ArrayList(corruptIssueLinks);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.issue.link.desc");
    }

    public boolean isAvailable()
    {
        return true;
    }

}