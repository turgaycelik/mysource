package com.atlassian.jira.issue;

import org.apache.commons.collections.Transformer;
import org.apache.lucene.document.Document;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * The IssueFactory is used for creating Issues in JIRA, as well as converting {@link GenericValue} issue objects
 * to proper {@link Issue} objects. It only handles creational tasks.  For update and retrieval see the
 * {@link IssueManager} interface.
 */
public interface IssueFactory
{
    /**
     * Creates a new blank issue.
     *
     * @return A {@link MutableIssue} object
     */
    MutableIssue getIssue();

    /**
     * Creates an issue object for an issue represented by the passed issueGV
     *
     * @param issueGV
     * @return A {@link MutableIssue} object that represents a copy of the issueGV
     */
    MutableIssue getIssue(GenericValue issueGV);

    /**
     * Creates an issue object for an issue represented by the passed issueGV.
     * This will return null if it is passed null, which is different behaviour to {@link #getIssue(org.ofbiz.core.entity.GenericValue)}
     *
     * @param issueGV
     * @return A {@link MutableIssue} object that represents a copy of the issueGV, or null.
     */
    MutableIssue getIssueOrNull(GenericValue issueGV);

    /**
     * Clones the Issue object which creates an editable copy.
     *
     * @param issue
     * @return A {@link MutableIssue} clone.
     */
    MutableIssue cloneIssue(Issue issue);

    /**
     * Convert a list of {@link GenericValue} objects into a list of {@link MutableIssue} objects.
     * This is just a convenience method that calls {@link #getIssue(org.ofbiz.core.entity.GenericValue)}
     *
     * @param issueGVs The issues to be retrieved
     * @return Return a list of {@link MutableIssue} objects
     * @see #getIssue(org.ofbiz.core.entity.GenericValue)
     */
    List<Issue> getIssues(Collection<GenericValue> issueGVs);

    /**
     * Creates an issue object for an issue represented by the Lucene Document
     *
     * @param issueDocument
     * @return
     */
    Issue getIssue(Document issueDocument);

    /**
     * Clone Issue for conversion from sub task to issue
     */
    MutableIssue cloneIssueNoParent(Issue issue);

    /**
     * Used for transforming collections of {@link Issue} objects to issue {@link GenericValue} objects.
     */
    static final Transformer TO_GENERIC_VALUE = new Transformer()
    {
        public Object transform(final Object object)
        {
            return ((Issue) object).getGenericValue();
        }
    };
}
