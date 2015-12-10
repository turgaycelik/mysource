package com.atlassian.jira.bc.issue.fields;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.EditableDefaultColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableUserColumnLayout;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.I18nBean;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;

import static com.atlassian.jira.bc.ServiceOutcomeImpl.ok;
import static com.atlassian.jira.security.Permissions.ADMINISTER;

public class ColumnServiceImpl implements ColumnService
{
    private final Function<String, NavigableField> toNavigableField = new Function<String, NavigableField>()
    {
        @Override
        public NavigableField apply(final String fieldId)
        {
            return fieldManager.getNavigableField(fieldId);
        }
    };

    private I18nHelper.BeanFactory beanFactory;
    private ColumnLayoutManager columnLayoutManager;
    private FieldManager fieldManager;
    private GlobalPermissionManager globalPermissionManager;
    private SearchRequestService searchRequestService;

    public ColumnServiceImpl(
            I18nBean.BeanFactory beanFactory, ColumnLayoutManager columnLayoutManager,
            FieldManager fieldManager, GlobalPermissionManager globalPermissionManager,
            SearchRequestService searchRequestService)
    {
        this.beanFactory = beanFactory;
        this.columnLayoutManager = columnLayoutManager;
        this.fieldManager = fieldManager;
        this.globalPermissionManager = globalPermissionManager;
        this.searchRequestService = searchRequestService;
    }

    @Override
    public ServiceOutcome<ColumnLayout> getColumnLayout(ApplicationUser serviceUser, ApplicationUser userWithColumns)
    {
        final I18nHelper i18nHelper = beanFactory.getInstance(serviceUser);
        if (permissionForUsersColumns(serviceUser, userWithColumns))
        {
            try
            {
                final ColumnLayout editableUserColumnLayout = columnLayoutManager.getColumnLayout(userWithColumns.getDirectoryUser());
                return ok(editableUserColumnLayout);

            }
            catch (ColumnLayoutStorageException e)
            {
                return fail(i18nHelper, e);
            }

        }
        else
        {
            final String mesg = i18nHelper.getText("admin.generic.permission");
            return ServiceOutcomeImpl.error(mesg, ErrorCollection.Reason.FORBIDDEN);
        }
    }

    @Override
    public ServiceOutcome<ColumnLayout> getColumnLayout(ApplicationUser serviceUser, Long filterId)
    {
        final JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(serviceUser);
        final SearchRequest filter = searchRequestService.getFilter(jiraServiceContext, filterId);
        final ErrorCollection errors = jiraServiceContext.getErrorCollection();
        if (errors.hasAnyErrors())
        {
            return new ServiceOutcomeImpl<ColumnLayout>(errors);
        }
        else
        {
            try
            {
                if (columnLayoutManager.hasColumnLayout(filter))
                {
                    return ok(columnLayoutManager.getColumnLayout(serviceUser.getDirectoryUser(), filter));
                }
                else
                {
                    return ok((ColumnLayout) null);
                }
            }
            catch (ColumnLayoutStorageException e)
            {
                return fail(beanFactory.getInstance(serviceUser), e);

            }
        }
    }

    @Override
    public ServiceOutcome<ColumnLayout> getDefaultColumnLayout(final ApplicationUser serviceUser)
    {
        final I18nHelper i18nHelper = beanFactory.getInstance(serviceUser);
        if (serviceUser != null && globalPermissionManager.hasPermission(ADMINISTER, serviceUser))
        {
            try
            {
                ColumnLayout editableDefaultColumnLayout = columnLayoutManager.getEditableDefaultColumnLayout();
                return ok(editableDefaultColumnLayout);
            }
            catch (ColumnLayoutStorageException e)
            {
                return fail(i18nHelper, e);
            }
        }
        else
        {
            final String msg = i18nHelper.getText("admin.generic.permission");
            return ServiceOutcomeImpl.error(msg, ErrorCollection.Reason.FORBIDDEN);
        }
    }

    @Override
    public ServiceResult setColumns(ApplicationUser serviceUser, ApplicationUser userWithColumns, List<String> fieldIds)
    {
        final I18nHelper i18nHelper = beanFactory.getInstance(serviceUser);
        if (permissionForUsersColumns(serviceUser, userWithColumns))
        {
            try
            {
                final EditableUserColumnLayout editableUserColumnLayout = columnLayoutManager.getEditableUserColumnLayout(userWithColumns.getDirectoryUser());
                editableUserColumnLayout.setColumns(Lists.transform(fieldIds, toNavigableField));
                columnLayoutManager.storeEditableUserColumnLayout(editableUserColumnLayout);

                return new ServiceResultImpl(new SimpleErrorCollection());
            }
            catch (ColumnLayoutStorageException e)
            {
                return fail(i18nHelper, e);
            }
        }
        else
        {
            final String mesg = i18nHelper.getText("admin.generic.permission");
            return ServiceOutcomeImpl.error(mesg, ErrorCollection.Reason.FORBIDDEN);
        }
    }

    @Override
    public ServiceResult setColumns(ApplicationUser serviceUser, Long filterId, List<String> fieldIds)
    {
        final I18nHelper i18nHelper = beanFactory.getInstance(serviceUser);
        final JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(serviceUser);
        final SearchRequest filter = searchRequestService.getFilter(jiraServiceContext, filterId);
        final ErrorCollection errors = jiraServiceContext.getErrorCollection();
        if (errors.hasAnyErrors())
        {
            return new ServiceOutcomeImpl<ColumnLayout>(errors);
        }
        else
        {
            try
            {
                if (serviceUser.equals(filter.getOwner()))
                {
                    final EditableSearchRequestColumnLayout columnLayout =
                            columnLayoutManager.getEditableSearchRequestColumnLayout(serviceUser.getDirectoryUser(), filter);
                    columnLayout.setColumns(Lists.transform(fieldIds, toNavigableField));
                    columnLayoutManager.storeEditableSearchRequestColumnLayout(columnLayout);

                    return new ServiceResultImpl(new SimpleErrorCollection());
                }
                else
                {
                    final String mesg = i18nHelper.getText("admin.generic.permission");
                    return ServiceOutcomeImpl.error(mesg, ErrorCollection.Reason.FORBIDDEN);
                }
            }
            catch (ColumnLayoutStorageException e)
            {
                return fail(i18nHelper, e);
            }
        }
    }

    @Override
    public ServiceResult setDefaultColumns(final ApplicationUser serviceUser, final List<String> fieldIds)
    {
        final I18nHelper i18nHelper = beanFactory.getInstance(serviceUser);
        if (serviceUser != null && globalPermissionManager.hasPermission(ADMINISTER, serviceUser))
        {
            try
            {
                final EditableDefaultColumnLayout editableDefaultColumnLayout =
                        columnLayoutManager.getEditableDefaultColumnLayout();
                editableDefaultColumnLayout.setColumns(Lists.transform(fieldIds, toNavigableField));
                columnLayoutManager.storeEditableDefaultColumnLayout(editableDefaultColumnLayout);

                return new ServiceResultImpl(new SimpleErrorCollection());
            }
            catch (ColumnLayoutStorageException e)
            {
                return fail(i18nHelper, e);
            }
        }
        else
        {
            final String mesg = i18nHelper.getText("admin.generic.permission");
            return ServiceOutcomeImpl.error(mesg, ErrorCollection.Reason.FORBIDDEN);
        }
    }

    @Override
    public ServiceResult resetColumns(ApplicationUser serviceUser, ApplicationUser userWithColumns)
    {
        final I18nHelper i18nHelper = beanFactory.getInstance(serviceUser);
        if (permissionForUsersColumns(serviceUser, userWithColumns))
        {
            try
            {
                columnLayoutManager.restoreUserColumnLayout(userWithColumns.getDirectoryUser());
                return new ServiceResultImpl(new SimpleErrorCollection());
            }
            catch (ColumnLayoutStorageException e)
            {
                return fail(i18nHelper, e);
            }

        }
        else
        {
            final String mesg = i18nHelper.getText("admin.generic.permission");
            return ServiceOutcomeImpl.error(mesg, ErrorCollection.Reason.FORBIDDEN);
        }
    }

    @Override
    public ServiceResult resetColumns(ApplicationUser serviceUser, Long filterId)
    {
        final I18nHelper i18nHelper = beanFactory.getInstance(serviceUser);
        final JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(serviceUser);
        final SearchRequest filter = searchRequestService.getFilter(jiraServiceContext, filterId);
        final ErrorCollection errors = jiraServiceContext.getErrorCollection();
        if (errors.hasAnyErrors())
        {
            return new ServiceOutcomeImpl<ColumnLayout>(errors);
        }
        else
        {
            try
            {
                if (serviceUser.equals(filter.getOwner()))
                {
                    columnLayoutManager.restoreSearchRequestColumnLayout(filter);
                    return new ServiceResultImpl(new SimpleErrorCollection());
                }
                else
                {
                    final String mesg = i18nHelper.getText("admin.generic.permission");
                    return ServiceOutcomeImpl.error(mesg, ErrorCollection.Reason.FORBIDDEN);
                }
            }
            catch (ColumnLayoutStorageException e)
            {
                return fail(i18nHelper, e);
            }
        }
    }

    /**
     * Does the service user have permission so they can edit the default columns for userWithColumns? Any user can edit
     * their own columns, but only admins can edit other users colunns.
     *
     * @param serviceUser the user invoking the service request.
     * @param userWithColumns the user whose columns are to be edited.
     * @return true only if serviceUser is admin or the same user as userWithColumns.
     */
    private boolean permissionForUsersColumns(ApplicationUser serviceUser, ApplicationUser userWithColumns)
    {
        return serviceUser != null && (serviceUser.equals(userWithColumns) || globalPermissionManager.hasPermission(ADMINISTER, serviceUser));
    }

    private ServiceOutcomeImpl<ColumnLayout> fail(I18nHelper i18nHelper, Exception e)
    {
        return ServiceOutcomeImpl.error(i18nHelper.getText("generic.error", e.getMessage()), ErrorCollection.Reason.SERVER_ERROR);
    }
}
