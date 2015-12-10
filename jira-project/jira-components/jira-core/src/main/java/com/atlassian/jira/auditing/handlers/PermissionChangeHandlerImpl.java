package com.atlassian.jira.auditing.handlers;

import java.util.List;
import java.util.Locale;

import com.atlassian.fugue.Option;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.permission.GlobalPermissionAddedEvent;
import com.atlassian.jira.event.permission.GlobalPermissionDeletedEvent;
import com.atlassian.jira.event.permission.PermissionAddedEvent;
import com.atlassian.jira.event.permission.PermissionDeletedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEntityEvent;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.annotations.VisibleForTesting;

import static com.atlassian.jira.permission.LegacyProjectPermissionKeyMapping.getKey;

/**
 *
 * @since v6.2
 */
public class PermissionChangeHandlerImpl implements PermissionChangeHandler
{
    private final PermissionSchemeManager permissionSchemeManager;
    private final PermissionManager permissionManager;
    private final PermissionTypeManager permissionTypeManager;
    private final I18nHelper.BeanFactory i18n;

    public PermissionChangeHandlerImpl(
            final PermissionSchemeManager permissionSchemeManager,
            final PermissionManager permissionManager,
            final PermissionTypeManager permissionTypeManager,
            final I18nHelper.BeanFactory i18n)
    {
        this.permissionSchemeManager = permissionSchemeManager;
        this.permissionManager = permissionManager;
        this.permissionTypeManager = permissionTypeManager;
        this.i18n = i18n;
    }

    @Override
    public RecordRequest onPermissionAddedEvent(final PermissionAddedEvent event)
    {
        final Scheme scheme = permissionSchemeManager.getSchemeObject(event.getSchemeId());

        return new RecordRequest(AuditingCategory.PERMISSIONS, "jira.auditing.permission.scheme.updated")
                .forObject(AssociatedItem.Type.SCHEME, scheme.getName(), scheme.getId())
                .withChangedValues(computeChangedValues(event));
    }

    @Override
    public RecordRequest onPermissionDeletedEvent(final PermissionDeletedEvent event)
    {
        final Scheme scheme = permissionSchemeManager.getSchemeObject(event.getSchemeId());

        return new RecordRequest(AuditingCategory.PERMISSIONS, "jira.auditing.permission.scheme.updated")
                .forObject(AssociatedItem.Type.SCHEME, scheme.getName(), scheme.getId())
                .withChangedValues(computeChangedValues(event));
    }

    @Override
    public RecordRequest onGlobalPermissionAddedEvent(final GlobalPermissionAddedEvent event)
    {
        final String groupName = StringUtils.defaultString(event.getGroup(), getI18n().getText("admin.common.words.anyone"));
        String permissionName = getPermissionName(event.getGlobalPermissionType());
        return new RecordRequest(AuditingCategory.PERMISSIONS, "jira.auditing.global.permission.added")
                .forObject(AssociatedItem.Type.PERMISSIONS, getI18n().getText("jira.auditing.global.permissions"))
                .withChangedValues(new ChangedValuesBuilder()
                        .addIfDifferent("admin.common.words.permission", "", permissionName)
                        .addIfDifferent("admin.common.words.group", "", groupName).build());
    }

    @Override
    public RecordRequest onGlobalPermissionDeletedEvent(final GlobalPermissionDeletedEvent event)
    {
        final String groupName = StringUtils.defaultString(event.getGroup(), getI18n().getText("admin.common.words.anyone"));
        String permissionName = getPermissionName(event.getGlobalPermissionType());
        return new RecordRequest(AuditingCategory.PERMISSIONS, "jira.auditing.global.permission.deleted")
                .forObject(AssociatedItem.Type.PERMISSIONS, getI18n().getText("jira.auditing.global.permissions"))
                .withChangedValues(new ChangedValuesBuilder()
                        .addIfDifferent("admin.common.words.permission", permissionName, "")
                        .addIfDifferent("admin.common.words.group", groupName, "").build());
    }

    private String getPermissionName(final GlobalPermissionType globalPermissionType)
    {
        return getI18n().getText(globalPermissionType.getNameI18nKey());
    }

    @VisibleForTesting
    protected List<ChangedValue> computeChangedValues(final AbstractSchemeEntityEvent event)
    {
        final SecurityType schemeType = permissionTypeManager.getSchemeType(event.getType());
        final Long permissionId = unboxNumber(event.getEntityTypeId());

        final String permissionName = getPermissionName(permissionId);
        final String receiverType = schemeType.getDisplayName();
        String receiverName = StringUtils.defaultString(event.getParameter() != null ? schemeType.getArgumentDisplay(event.getParameter()) : "", "");

        if (GroupDropdown.DESC.equals(event.getType()) && StringUtils.isEmpty(receiverName))
        {
            // special case, group Anyone
            receiverName = getI18n().getText("common.sharing.shared.description.anyone");
        }

        final ChangedValuesBuilder builder = new ChangedValuesBuilder();

        if (event instanceof PermissionAddedEvent)
        {
            // set to
            builder.addIfDifferent("admin.common.words.permission", "", permissionName)
                    .addIfDifferent("admin.common.words.type", "", receiverType)
                    .addIfDifferent("admin.common.words.value", "", receiverName);
        }
        else if (event instanceof PermissionDeletedEvent)
        {
            // this is PermissionDeletedEvent, so set from
            builder.addIfDifferent("admin.common.words.permission", permissionName, "")
                    .addIfDifferent("admin.common.words.type", receiverType, "")
                    .addIfDifferent("admin.common.words.value", receiverName, "");
        } else {
            throw new UnsupportedOperationException("Missing handler for " + event.getClass().getSimpleName());
        }

        return builder.build();
    }

    @VisibleForTesting
    protected String getPermissionName(Long permissionId)
    {
        ProjectPermissionKey permissionKey = getKey(permissionId);
        Option<ProjectPermission> permission = permissionManager.getProjectPermission(permissionKey);
        return permission.isDefined() ? getI18n().getText(permission.get().getNameI18nKey()) : "";
    }

    protected I18nHelper getI18n()
    {
        // You must not cache I18nHelper
        return i18n.getInstance(Locale.ENGLISH);
    }

    private Long unboxNumber(Object number) {
        return Long.valueOf(number.toString());
    }
}
