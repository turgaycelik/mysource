package com.atlassian.jira.bc.issue.link;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeDestroyer;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * @since v6.0
 */
public class DefaultIssueLinkTypeService implements IssueLinkTypeService
{
    private final PermissionManager permissionManager;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkTypeDestroyer issueLinkTypeDestroyer;
    private final I18nHelper.BeanFactory i18nFactory;
    private final ApplicationProperties applicationProperties;

    public DefaultIssueLinkTypeService(PermissionManager permissionManager, IssueLinkTypeManager issueLinkTypeManager, IssueLinkTypeDestroyer issueLinkTypeDestroyer, I18nHelper.BeanFactory i18nFactory, ApplicationProperties applicationProperties)
    {
        this.permissionManager = permissionManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueLinkTypeDestroyer = issueLinkTypeDestroyer;
        this.i18nFactory = i18nFactory;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public ServiceOutcome<IssueLinkType> createIssueLinkType(User user, String name, String outward, String inward)
    {
        final I18nHelper i18n = i18nFactory.getInstance(user);

        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled")), ErrorCollection.Reason.NOT_FOUND);
        }

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.errors.linking.error.create.no.permission"), ErrorCollection.Reason.FORBIDDEN);
        }

        if (!issueLinkTypeManager.getIssueLinkTypesByName(name).isEmpty())
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.errors.linking.error.create.duplicate"));
        }

        // User created link types always have a "style" of null. I don't know what "style" means.
        issueLinkTypeManager.createIssueLinkType(name, outward, inward, null);
        Collection<IssueLinkType> issueLinkTypesByName = issueLinkTypeManager.getIssueLinkTypesByName(name);
        if (issueLinkTypesByName.size() != 1)
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.errors.linking.error.create.failed.mysteriously"), ErrorCollection.Reason.SERVER_ERROR);
        }
        else
        {
            return ServiceOutcomeImpl.ok(issueLinkTypesByName.iterator().next());
        }
    }

    @Override
    public ServiceOutcome<IssueLinkType> deleteIssueLinkType(User user, IssueLinkType linkType)
    {
        final I18nHelper i18n = i18nFactory.getInstance(user);

        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled")), ErrorCollection.Reason.NOT_FOUND);
        }

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.errors.linking.error.delete.no.permission"), ErrorCollection.Reason.FORBIDDEN);
        }

        try
        {
            issueLinkTypeDestroyer.removeIssueLinkType(linkType.getId(), null, user);
            return ServiceOutcomeImpl.ok(linkType);
        }
        catch (RemoveException e)
        {
            return ServiceOutcomeImpl.error(e.getMessage(), ErrorCollection.Reason.SERVER_ERROR);
        }
    }

    @Override
    public ServiceOutcome<Collection<IssueLinkType>> getIssueLinkTypes(User user)
    {
        final I18nHelper i18n = i18nFactory.getInstance(user);

        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled")), ErrorCollection.Reason.NOT_FOUND);
        }

        return ServiceOutcomeImpl.ok(Collections.unmodifiableCollection(issueLinkTypeManager.getIssueLinkTypes()));
    }

    @Override
    public ServiceOutcome<IssueLinkType> updateIssueLinkType(User user, IssueLinkType linkType, String name, String outward, String inward)
    {
        final I18nHelper i18n = i18nFactory.getInstance(user);

        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled")), ErrorCollection.Reason.NOT_FOUND);
        }

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.errors.linking.error.update.no.permission"), ErrorCollection.Reason.FORBIDDEN);
        }

        // Trying to give this link type the same name as an existing link type is not allowed.
        if (!Objects.equal(name, linkType.getName()) && !issueLinkTypeManager.getIssueLinkTypesByName(name).isEmpty())
        {
            return ServiceOutcomeImpl.error(i18n.getText("admin.errors.linking.error.create.duplicate"));
        }

        issueLinkTypeManager.updateIssueLinkType(linkType,
                StringUtils.isEmpty(name) ? linkType.getName() : name,
                StringUtils.isEmpty(outward) ? linkType.getOutward() : outward,
                StringUtils.isEmpty(inward) ? linkType.getInward() : inward);
        return ServiceOutcomeImpl.ok(linkType);
    }
}
