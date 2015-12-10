package com.atlassian.jira.issue.pager;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import javax.annotation.Nonnull;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.PagerFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_KEY;
import static java.util.Collections.singleton;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MT_CORRECTNESS", justification="TODO Needs to be fixed")
@Deprecated
public class NextPreviousPager implements Serializable
{
    private static final Logger log = Logger.getLogger(NextPreviousPager.class);

    static final int DEFAULT_CACHE_SIZE = 40;

    private static final FieldSelector FIELD_SELECTOR = new SetBasedFieldSelector(singleton(ISSUE_KEY), Collections.<String>emptySet());

    private int[] docIds;

    // Used for quick look up of a docs position

    // Issue key cache
    private List<String> issueKeys;

    private int currentKeyPos = -1;
    private int searchRequestHashCode = -1;

    // The version of the reader that was used to generate the docId list.  Doc ids are specific to a reader.
    private long readerVersion;

    // The offset of the cache from the beginning of the complete doc id list
    private int offset;

    private int cacheSize = DEFAULT_CACHE_SIZE;


    public NextPreviousPager(NextPreviousPager that)
    {
        this.currentKeyPos = that.currentKeyPos;
        this.searchRequestHashCode = that.searchRequestHashCode;
        this.readerVersion = that.readerVersion;
        this.offset = that.offset;
        this.cacheSize = that.cacheSize;

        this.docIds = that.docIds == null ? that.docIds : that.docIds.clone();
        this.issueKeys = that.issueKeys == null ? that.issueKeys : new ArrayList<String>(that.issueKeys);
    }

    public NextPreviousPager(final ApplicationProperties applicationProperties)
    {
        if (applicationProperties != null)
        {
            final String cacheSizeStr = applicationProperties.getDefaultBackedString(APKeys.JIRA_PREVIOUS_NEXT_CACHE_SIZE);
            if (!StringUtils.isBlank(cacheSizeStr))
            {
                try
                {
                    cacheSize = Integer.parseInt(cacheSizeStr);
                    if (cacheSize < 3)
                    {

                        log.warn("Issue key cache size can not be less than 3, Setting it to default.");
                        cacheSize = DEFAULT_CACHE_SIZE;
                    }
                }
                catch (final NumberFormatException nfe)
                {
                    log.warn(
                        "Exception thrown while trying to convert jira-application.properties key '" + APKeys.JIRA_PREVIOUS_NEXT_CACHE_SIZE + "'. Ignoring and setting to default: '" + DEFAULT_CACHE_SIZE + "'",
                        nfe);
                }
            }
        }
    }

    public boolean isHasCurrentKey()
    {
        return getCurrentKey() != null;
    }

    public String getCurrentKey()
    {
        return getKeyForPosition(currentKeyPos);
    }

    public int getCurrentPosition()
    {
        // must add 1 as it is not 0 based
        return currentKeyPos + 1;
    }

    public int getCurrentSize()
    {
        return docIds == null ? 0 : docIds.length;
    }

    int getCacheSize()
    {
        return cacheSize;
    }

    public String getNextKey()
    {
        return (getCurrentKey() == null) ? null : getKeyForPosition(currentKeyPos + 1);
    }

    public String getPreviousKey()
    {
        return (getCurrentKey() == null) ? null : getKeyForPosition(currentKeyPos - 1);
    }

    /**
     * IMPORTANT!!  This method must be called before current, previous or next issue is called.  Otherwise they will return null.
     * <p/>
     * This method keeps track of the current position in a search list and allows the user to easily navigate from issue to another.
     *
     * @param searchRequest The search request to keep track of.
     * @param user          The user performing the search
     * @param currentKey    the current issue that the user is browsing.
     * @throws IOException     if there is a problem while trying to read from the index
     * @throws SearchException thrown if there is an exception thrown during the search
     *
     * @deprecated As of JIRA 6.0, issue pager is no longer generated on the server side. This method will do nothing.
     */
    public void update(final SearchRequest searchRequest, final User user, final String currentKey) throws IOException, SearchException
    {
        log.warn("NextPreviousPager is no longer supported as of JIRA 6.0 because issue pager is no longer generated on the server side. This method will do nothing. Please stop using it.");
    }

    private String getKeyForPosition(final int position)
    {
        if ((position < 0) || (position > docIds.length - 1))
        {
            return null;
        }

        final int relativePos = position - offset;
        if ((relativePos < 0) || (relativePos > issueKeys.size() - 1))
        {
            return null;
        }
        return issueKeys.get(relativePos);
    }

    IndexReader getReader()
    {
        return ComponentAccessor.getComponentOfType(IssueIndexManager.class).getIssueSearcher().getIndexReader();
    }

    SearchProvider getSearchProvider()
    {
        return ComponentAccessor.getComponentOfType(SearchProvider.class);
    }


}
