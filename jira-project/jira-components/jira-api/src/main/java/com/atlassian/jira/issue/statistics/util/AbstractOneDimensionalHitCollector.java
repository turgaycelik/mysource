package com.atlassian.jira.issue.statistics.util;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldFinder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to traverse a query and collect if the specified one dimension is relevant, this will keep track of the
 * irrelevant count.
 *
 * This class implements {@link org.apache.lucene.search.Collector} which is a callback mechanism for use by 
 * {@link org.apache.lucene.search.IndexSearcher}, but it is widely abused and called directly from within JIRA.
 *
 * @since v4.0
 */
@Internal
public abstract class AbstractOneDimensionalHitCollector extends Collector
{
    private static final Logger log = Logger.getLogger(AbstractOneDimensionalHitCollector.class);

    private final String fieldId;
    private Collection<String>[] docToTerms;
    private Collection<String>[] docToProject;
    private Collection<String>[] docToIssueType;
    private String documentFieldId;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final ProjectManager projectManager;
    private final ReaderCache readerCache;
    private long irrelevantCount = 0;
    private boolean usingOldCallingConvention = false;

    private final CustomField customField;
    // ClusterSafe because we create new instance of this class for each request.
    // One day if get a request level cache in the FieldVisibilityManager etc this local cache could become redundant.
    private Map<String, Map<String, Boolean>> projectIdIssueTypeIdFieldVisibility = new HashMap<String, Map<String, Boolean>>();

    /**
     * Records the number of times the {@link #collect(int)} method was called. The method should be called once
     * for each issue.
     */
    private long hitCount = 0;
    private int docBase = 0;

    /**
     * @deprecated since v5.1.8. Use {@link #AbstractOneDimensionalHitCollector(String, FieldVisibilityManager, ReaderCache,
     * FieldManager, ProjectManager)}
     *        and call setNextReader for each reader segment, that is if you are calling {@link #collect(int) directly
     *        and not just passing the Collector to Lucene.
     */
    public AbstractOneDimensionalHitCollector(final String documentFieldId, final IndexReader indexReader,
            final FieldVisibilityManager fieldVisibilityManager, final ReaderCache readerCache,
            final FieldManager fieldManager, final ProjectManager projectManager)
    {
        this.documentFieldId = documentFieldId;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.readerCache = readerCache;
        this.fieldId = new HitCollectorUtil().getFieldId(documentFieldId);
        this.projectManager = projectManager;
        setNextReader(indexReader, 0);
        usingOldCallingConvention = true;
        // JRA-40989 Get the CF object once because it is expensive to get it 10,000 times
        if (fieldManager.isCustomField(fieldId))
        {
            customField = fieldManager.getCustomField(fieldId);
        }
        else
        {
            customField = null;
        }
    }

    /**
     * @since v5.1.8
     */
    public AbstractOneDimensionalHitCollector(final String documentFieldId,
            final FieldVisibilityManager fieldVisibilityManager, final ReaderCache readerCache, FieldManager fieldManager,
            final ProjectManager projectManager)
    {
        this.documentFieldId = documentFieldId;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.projectManager = projectManager;
        this.readerCache = readerCache;
        this.fieldId = new HitCollectorUtil().getFieldId(documentFieldId);
        // JRA-40989 Get the CF object once because it is expensive to get it 10,000 times
        if (fieldManager.isCustomField(fieldId))
        {
            customField = fieldManager.getCustomField(fieldId);
        }
        else
        {
            customField = null;
        }
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException
    {
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
    {
        if (usingOldCallingConvention)
        {
            log.warn("You constructed an instance of AbstractOneDimensionalHitCollector using the old calling convention "
                    + "and passed in an index reader.  You should not be calling setNextReader() on that instance "
                    + "as it will cause excessive double caching of data.  This is a BAD BAD THING!!!");
        }
        this.docBase = docBase;
        this.docToTerms = readCachedMultiValueField(reader, documentFieldId, readerCache);
        this.docToProject = readCachedSingleValueField(reader, SystemSearchConstants.forProject().getIndexField(), readerCache);
        this.docToIssueType = readCachedSingleValueField(reader, SystemSearchConstants.forIssueType().getIndexField(), readerCache);
    }

    @Override
    public boolean acceptsDocsOutOfOrder()
    {
        return true;
    }

    /**
     * Collect hits.
     * When accessing our value caches we do not need to increment i by the docBase as the value caches are built
     * against the current (segment) reader.
     * When calling the abstract collectXXX() methods these expect the index of the whole reader and so we need
     * to use (i + docBase)
     * @param i Index to the current reader
     */
    public void collect(int i)
    {
        ++hitCount;
        Collection<String> terms = docToTerms[i];
        if (terms == null)
        {
            // We know there will always be a project and issue type for each issue
            final String projectIdString = getSingleValue(docToProject[i]);
            final String issueTypeId = getSingleValue(docToIssueType[i]);

            // Distinguish between a visible field with an empty value and a field that is not relevant for this issue:
            if (fieldIsRelevant(projectIdString, issueTypeId))
            {
                collectWithTerms(docBase + i, null);
            }
            else
            {
                irrelevantCount++;
                collectIrrelevant(docBase + i);
            }
        }
        else
        {
            collectWithTerms(docBase + i, terms);
        }
    }

    private boolean fieldIsRelevant(final String projectIdString, final String issueTypeId)
    {
        // JRA-40989 use a local cache to speed up field visibility lookups
        // We use the Project ID as a String in our map because
        // a) It means we don't have to parse it to Long if we get a cache hit
        // b) Lucene is giving use the same instance of the String each time, so it is free to store memory-wise
        Map<String, Boolean> issueTypeIdFieldVisibility = projectIdIssueTypeIdFieldVisibility.get(projectIdString);
        if (issueTypeIdFieldVisibility == null)
        {
            issueTypeIdFieldVisibility = new HashMap<String, Boolean>();
            projectIdIssueTypeIdFieldVisibility.put(projectIdString, issueTypeIdFieldVisibility);
        }

        Boolean fieldRelevant = issueTypeIdFieldVisibility.get(issueTypeId);
        if (fieldRelevant == null)
        {
            // Cache miss - convert the projectId String to Long so we can work with it
            final Long projectId = Long.valueOf(projectIdString);
            fieldRelevant = fieldVisibilityManager.isFieldVisible(projectId, fieldId, issueTypeId) && isFieldInScope(projectId, issueTypeId);
            issueTypeIdFieldVisibility.put(issueTypeId, fieldRelevant);
        }
        return fieldRelevant;
    }

    private boolean isFieldInScope(final Long projectId, final String issueTypeId)
    {
        if (fieldId == null || "".equals(fieldId) || projectId == null || issueTypeId == null || "".equals(issueTypeId))
        {
            return false;
        }
        if (customField == null)
        {
            // System fields don't have "scope"
            return true;
        }
        Project project = projectManager.getProjectObj(projectId);
        if (project == null)
        {
            return false;
        }

        List<String> issueTypeIds = new ArrayList<String>();
        issueTypeIds.add(issueTypeId);
        return customField.isInScope(project, issueTypeIds);
    }

    // We should always have a List, but handle it gracefully if somehow we don't.
    // The purpose of this is to avoid the GC churn of creating thousands of
    // ephemeral iterators when get(0) would work.
    private static String getSingleValue(Collection<String> source)
    {
        return (source instanceof List<?>) ? ((List<String>)source).get(0) : source.iterator().next();
    }

    public long getIrrelevantCount()
    {
        return irrelevantCount;
    }

    /**
     * Returns the number of times the {@link #collect(int)} method was called. This should return the number of
     * unique issues that was matched during a search.
     *
     * @return number of times the {@link #collect(int)} method was called.
     */
    public long getHitCount()
    {
        return hitCount;
    }

    /**
     * Implement this if you would like to do something when the hit collector has encountered a docId that contains an
     * irrelevant data match
     *
     * @param docId the match we have found.  This is an index into the overall directory reader.
     */
    protected abstract void collectIrrelevant(final int docId);

    /**
     * Implement this if you would like to do something with the hit we have found.
     *
     * @param docId the doc id of the hit.  This is an index into the overall directory reader.
     * @param terms the terms for the fieldId for this document, pre-calculated so you may not need to call getDocument
     */
    protected abstract void collectWithTerms(int docId, Collection<String> terms);


    private static Collection<String>[] readCachedMultiValueField(final IndexReader indexReader, final String fieldId, final ReaderCache readerCache)
    {
        return readerCache.get(indexReader, fieldId, new Supplier<Collection<String>[]>()
        {
            public Collection<String>[] get()
            {
                try
                {
                    return JiraLuceneFieldFinder.getInstance().getMatches(indexReader, fieldId);
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        });
    }

    private static Collection<String>[] readCachedSingleValueField(final IndexReader indexReader, final String fieldId, final ReaderCache readerCache)
    {
        return readerCache.get(indexReader, fieldId, new Supplier<Collection<String>[]>()
        {
            public Collection<String>[] get()
            {
                try
                {
                    return JiraLuceneFieldFinder.getInstance().getUniqueMatches(indexReader, fieldId);
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        });
    }
}
