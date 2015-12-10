package com.atlassian.jira.issue.index;

import com.atlassian.fugue.Option;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.util.LuceneUtils;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * @since v4.3
 */
public class DefaultChangeHistoryDocumentFactory implements ChangeHistoryDocumentFactory
{
    private final SearchExtractorRegistrationManager searchExtractorManager;

    public DefaultChangeHistoryDocumentFactory(final SearchExtractorRegistrationManager searchExtractorManager)
    {
        this.searchExtractorManager = searchExtractorManager;
    }

    public Option<Document> apply(final ChangeHistoryGroup changeHistoryGroup)
    {
        if (changeHistoryGroup == null)
        {
            return Option.none();
        }
        final String changeItemUser = changeHistoryGroup.getUserKey();
        final Builder builder =
                new Builder(changeHistoryGroup)
                        .addField(DocumentConstants.PROJECT_ID, String.valueOf(changeHistoryGroup.getProjectId()), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS)
                        .addField(DocumentConstants.ISSUE_ID, String.valueOf(changeHistoryGroup.getIssueId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)
                        .addField(DocumentConstants.ISSUE_KEY, String.valueOf(changeHistoryGroup.getIssueKey()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)
                        .addField(DocumentConstants.CHANGE_ACTIONER, encodeProtocolPreservingCase(changeItemUser), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS)
                        .addField(DocumentConstants.CHANGE_DATE, LuceneUtils.dateToString(changeHistoryGroup.getCreated()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)
                        .addAllExtractors(searchExtractorManager.findExtractorsForEntity(ChangeHistoryGroup.class));

        for (final ChangeHistoryItem changeItem : changeHistoryGroup.getChangeItems())
        {
            final String changedField = changeItem.getField();

            builder.addField(encodeChangedField(changedField, DocumentConstants.CHANGE_DURATION), String.valueOf(changeItem.getDuration()), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS)
                   .addField(encodeChangedField(changedField, DocumentConstants.NEXT_CHANGE_DATE), LuceneUtils.dateToString(changeItem.getNextChangeCreated()), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
            for (final String from : changeItem.getFroms().values())
            {
                builder.addField(encodeChangedField(changedField, DocumentConstants.CHANGE_FROM), encodeProtocol(from), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
            }
            for (final String to : changeItem.getTos().values())
            {
                builder.addField(encodeChangedField(changedField, DocumentConstants.CHANGE_TO), encodeProtocol(to), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
            }
            for (final String fromValue : changeItem.getFroms().keySet())
            {
                builder.addField(encodeChangedField(changedField, DocumentConstants.OLD_VALUE), encodeProtocolPreservingCase(fromValue), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
            }
            for (final String toValue : changeItem.getTos().keySet())
            {
                builder.addField(encodeChangedField(changedField, DocumentConstants.NEW_VALUE), encodeProtocolPreservingCase(toValue), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
            }
        }
        return builder.build();
    }

    private static String encodeChangedField(String changedField, String docConstant)
    {
        return changedField + '.' + docConstant;
    }

    private static String encodeProtocol(final String changeItem)
    {
        return DocumentConstants.CHANGE_HISTORY_PROTOCOL + (changeItem == null ? "" : changeItem.toLowerCase());
    }

    private static String encodeProtocolPreservingCase(final String changeItem)
    {
        return DocumentConstants.CHANGE_HISTORY_PROTOCOL + (changeItem == null ? "" : changeItem);
    }

    private static class Builder extends EntityDocumentBuilder<ChangeHistoryGroup, Builder>
    {
        private Builder(final ChangeHistoryGroup entity)
        {
            super(entity, SearchProviderFactory.CHANGE_HISTORY_INDEX);
        }
    }
}
