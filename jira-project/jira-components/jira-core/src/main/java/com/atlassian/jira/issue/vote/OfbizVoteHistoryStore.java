package com.atlassian.jira.issue.vote;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.dbc.Assertions;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Store implementation for Avatars. Nearly all methods could throw a DataAccessException.
 *
 * @since v4.0
 */
public class OfbizVoteHistoryStore implements VoteHistoryStore
{

    static final String VOTE_HISTORY_ENTITY = "VoteHistory";
    static final String ISSUE = "issue";
    static final String VOTES = "votes";
    static final String TIMESTAMP = "timestamp";

    private OfBizDelegator ofBizDelegator;

    public OfbizVoteHistoryStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public List<VoteHistoryEntry> getHistory(final Long issueId)
    {
        Assertions.notNull("issueId", issueId);
        ArrayList<VoteHistoryEntry> history = new ArrayList<VoteHistoryEntry>();
        for (GenericValue gv : ofBizDelegator.findByAnd(VOTE_HISTORY_ENTITY, EasyMap.build(ISSUE, issueId), Collections.singletonList(TIMESTAMP)))
        {
            history.add(gvToHistoryEntry(gv));
        }
        return history;
    }

    @Override
    public void delete(String issueId) throws DataAccessException
    {
        Assertions.notNull("issueKey", issueId);
        ofBizDelegator.removeByAnd(VOTE_HISTORY_ENTITY, EasyMap.build(ISSUE, issueId));
    }

    @Override
    public void add(VoteHistoryEntry entry) throws DataAccessException
    {
        Assertions.notNull("entry", entry);
        Assertions.notNull("entry.issueKey", entry.getIssueId());
        Assertions.notNull("avatar.fileName", entry.getVotes());
        Assertions.notNull("avatar.contentType", entry.getTimestamp());

        ofBizDelegator.createValue(VOTE_HISTORY_ENTITY, getFields(entry));
    }

    private Map getFields(VoteHistoryEntry entry)
    {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(ISSUE, entry.getIssueId());
        fields.put(VOTES, entry.getVotes());
        fields.put(TIMESTAMP, entry.getTimestamp());
        return fields;
    }

    VoteHistoryEntry gvToHistoryEntry(final GenericValue gv)
    {
        return new VoteHistoryEntryImpl(gv.getLong(ISSUE),
               gv.getTimestamp(TIMESTAMP),
               gv.getLong(VOTES));
    }

}
