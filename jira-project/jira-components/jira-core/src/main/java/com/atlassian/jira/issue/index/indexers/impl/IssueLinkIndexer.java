package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Collection;

import static com.atlassian.jira.issue.link.Direction.IN;
import static com.atlassian.jira.issue.link.Direction.OUT;
import static org.apache.lucene.document.Field.Index.NOT_ANALYZED;
import static org.apache.lucene.document.Field.Store.NO;

public class IssueLinkIndexer implements FieldIndexer
{
    private static final String PREFIX_LINK_TYPE = "l:";
    private static final String PREFIX_ISSUE = "i:";
    private static final String TERM_OUT = "d:o";
    private static final String TERM_IN = "d:i";
    private static final String SEPARATOR = ",";

    /**
     * Creates a term query value based only on the link type.
     *
     * @param issueLinkTypeId the id of the issue link type.
     * @return the value as found in the index.
     */
    public static String createValue(Long issueLinkTypeId)
    {
        return PREFIX_LINK_TYPE + issueLinkTypeId;
    }

    /**
     * Creates a term query value based on the link type and direction.
     *
     * @param issueLinkTypeId the id of the issue link type.
     * @param direction the direction of the link.
     * @return the value as found in the index
     */
    public static String createValue(Long issueLinkTypeId, Direction direction)
    {
        return createValue(issueLinkTypeId) + SEPARATOR + (direction == IN ? TERM_IN : TERM_OUT);
    }

    /**
     * Creates a term query value based on the link type, direction and destination issue.
     *
     * @param issueLinkTypeId the id of the issue link type.
     * @param direction the direction of the link.
     * @param otherIssueId the id of issue the link links to.
     * @return the value as found in the index
     */
    public static String createValue(Long issueLinkTypeId, Direction direction, Long otherIssueId)
    {
        return createValue(issueLinkTypeId, direction) + SEPARATOR + PREFIX_ISSUE + otherIssueId;
    }

    private final IssueLinkManager issueLinkManager;

    public IssueLinkIndexer(IssueLinkManager issueLinkManager)
    {
        this.issueLinkManager = issueLinkManager;
    }

    public String getId()
    {
        return IssueFieldConstants.ISSUE_LINKS;
    }

    public String getDocumentFieldId()
    {
        return DocumentConstants.ISSUE_LINKS;
    }

    /**
     * We have to always index the link because we want to support the use of links for system-level issue relationships
     * implemented by system links without linking enabled.
     *
     * @param issue the issue with the link (unused).
     * @return true always.
     */
    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return true;
    }

    /**
     * We index the links in 3 ways. First, we index just the link type, so queries can be done like see if something is
     * a duplicate of another issue. Second, we index the link type with a direction flag which enables queries of all
     * issues that are blockers of other issues. Third, we link the link type plus direction plus the OTHER issue id so
     * that we can do a search like "all cloners of this issue" (or all cloners of other issues!).
     *
     * @param doc the lucene document that should be modified by adding relevant fields to.
     * @param issue the issue that contains the data that will be indexed and which can be used to determine the
     * project/issue type context that will allow you to determine if we should add the value as searchable
     */
    public void addIndex(Document doc, Issue issue)
    {
        Long issueId = issue.getId();
        addFieldsToDoc(doc, issueLinkManager.getInwardLinks(issueId), IN);
        addFieldsToDoc(doc, issueLinkManager.getOutwardLinks(issueId), OUT);
    }

    private void addFieldsToDoc(Document doc, Collection<IssueLink> issueLinks, Direction direction)
    {
        for (IssueLink issueLink : issueLinks)
        {
            Long linkTypeId = issueLink.getLinkTypeId();
            doc.add(new Field(getDocumentFieldId(), createValue(linkTypeId), NO, NOT_ANALYZED));
            doc.add(new Field(getDocumentFieldId(), createValue(linkTypeId, direction), NO, NOT_ANALYZED));
            Long otherIssue = direction == Direction.OUT ? issueLink.getDestinationId() : issueLink.getSourceId();
            //  IN links have OTHER issue at SOURCE, OUT links have OTHER issue at DEST
            doc.add(new Field(getDocumentFieldId(), createValue(linkTypeId, direction, otherIssue), NO, NOT_ANALYZED));
        }
    }
}
