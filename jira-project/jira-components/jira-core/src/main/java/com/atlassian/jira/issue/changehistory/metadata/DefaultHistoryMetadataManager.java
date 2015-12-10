package com.atlassian.jira.issue.changehistory.metadata;

import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.issue.changehistory.properties.ChangeHistoryPropertyService;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyOptions;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.DefaultChangeHistoryManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultHistoryMetadataManager implements HistoryMetadataManager
{
    private final HistoryMetadataMarshaller historyMetadataMarshaller;
    private final ChangeHistoryPropertyService changeHistoryPropertyService;

    public DefaultHistoryMetadataManager(final HistoryMetadataMarshaller historyMetadataMarshaller, final ChangeHistoryPropertyService changeHistoryPropertyService)
    {
        this.historyMetadataMarshaller = historyMetadataMarshaller;
        this.changeHistoryPropertyService = changeHistoryPropertyService;
    }

    @Override
    public void saveHistoryMetadata(@Nonnull final Long changeGroupId, @Nullable final ApplicationUser user, @Nonnull final HistoryMetadata historyMetadata)
    {
        Assertions.notNull("changeGroupId", changeGroupId);
        Assertions.notNull("historyMetadata", historyMetadata);

        changeHistoryPropertyService.setProperty(
                user,
                new EntityPropertyService.SetPropertyValidationResult(
                        new SimpleErrorCollection(),
                        Option.some(new EntityPropertyService.EntityPropertyInput(
                                        historyMetadataMarshaller.marshall(historyMetadata),
                                        DefaultChangeHistoryManager.HISTORY_METADATA_KEY,
                                        changeGroupId,
                                        EntityPropertyType.CHANGE_HISTORY_PROPERTY.getDbEntityName())
                        )
                )
        );
    }

    @Override
    public HistoryMetadataManager.HistoryMetadataResult getHistoryMetadata(@Nonnull final ChangeHistory changeHistory, @Nullable final ApplicationUser user)
    {
        Assertions.notNull("changeHistory", changeHistory);

        final EntityPropertyService.PropertyResult property = changeHistoryPropertyService.getProperty(user, changeHistory.getId(), DefaultChangeHistoryManager.HISTORY_METADATA_KEY);
        return convertResult(property);
    }

    @Override
    public HistoryMetadataManager.HistoryMetadataResult getHistoryMetadata(long changeHistoryId)
    {
        final EntityPropertyService.PropertyResult property =
                changeHistoryPropertyService.getProperty(null, changeHistoryId, DefaultChangeHistoryManager.HISTORY_METADATA_KEY,
                        new EntityPropertyOptions.Builder().skipPermissionChecks().build());
        return convertResult(property);
    }

    private HistoryMetadataResult convertResult(final EntityPropertyService.PropertyResult property)
    {
        if (!property.isValid())
        {
            return new HistoryMetadataResult(property.getErrorCollection());
        }

        final Option<EntityProperty> metadataProperty = property.getEntityProperty();
        if (metadataProperty.isDefined() && metadataProperty.get().getValue() != null)
        {
            final HistoryMetadata historyMetadata = historyMetadataMarshaller.unmarshall(metadataProperty.get().getValue());
            return new HistoryMetadataResult(historyMetadata);
        }

        return new HistoryMetadataResult((HistoryMetadata) null);
    }

}
