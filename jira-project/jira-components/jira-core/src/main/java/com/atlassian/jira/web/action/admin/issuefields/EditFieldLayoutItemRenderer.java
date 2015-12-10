package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.issuefields.enterprise.FieldLayoutSchemeHelper;
import com.atlassian.query.Query;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Action used to set a renderer for a field layout item
 */
@WebSudoRequired
public class EditFieldLayoutItemRenderer extends AbstractConfigureFieldLayout
{
    private EditableFieldLayout editableFieldLayout;
    private String selectedRendererType;
    private Integer rendererEdit;
    private Query query;

    private static final String FIELD_LAYOUT_SCHEME_ASSOCIATION = "FieldLayoutScheme";
    private SearchProvider searchProvider;
    private final SearchService searchService;

    public EditFieldLayoutItemRenderer(final FieldScreenManager fieldScreenManager, final RendererManager rendererManager,
            final SearchProvider searchProvider, final SearchService searchService,
            final ReindexMessageManager reindexMessageManager,final FieldManager fieldManager, final FieldLayoutSchemeHelper fieldLayoutSchemeHelper,
            final HackyFieldRendererRegistry hackyFieldRendererRegistry, final ManagedConfigurationItemService managedConfigurationItemService)
    {
        super(fieldScreenManager, rendererManager, reindexMessageManager, fieldLayoutSchemeHelper, fieldManager, hackyFieldRendererRegistry, managedConfigurationItemService);
        this.searchProvider = searchProvider;
        this.searchService = searchService;
    }

    protected void doValidation()
    {
        final OrderableField field = getSelectedLayoutItem().getOrderableField();
        if (isFieldLocked(getSelectedLayoutItem().getOrderableField()))
        {
            addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.alter.renderer.locked", field.getName()), Reason.FORBIDDEN);
            return;
        }


        if (hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(field))
        {
            if (HackyRendererType.fromKey(getSelectedRendererType()) == null)
            {
                addErrorMessage(getText("admin.errors.fieldlayout.cannot.add.renderer.type",getSelectedRendererType()));
            }
        }
        else
        {
            if(!(field instanceof RenderableField && ((RenderableField) field).isRenderable()) )
            {
                addErrorMessage(getText("admin.errors.fieldlayout.cannot.set.renderer","'" + getText(field.getNameKey()) + "'"));
            }
            if(!getRendererManager().getRendererForType(getSelectedRendererType()).getRendererType().equals(getSelectedRendererType()))
            {
                addErrorMessage(getText("admin.errors.fieldlayout.cannot.add.renderer.type",getSelectedRendererType()));
            }
        }
    }

    public String doDefault() throws Exception
    {
        final OrderableField orderableField = getSelectedLayoutItem().getOrderableField();
        if (isFieldLocked(orderableField))
        {
            addErrorMessage(getText("admin.managed.configuration.items.customfield.error.cannot.alter.renderer.locked", orderableField.getName()), Reason.FORBIDDEN);
            return Action.ERROR;
        }

        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getFieldLayout().setRendererType(getSelectedLayoutItem(), getSelectedRendererType());
        store();
        return getFieldRedirect();
    }

    public boolean isSelectedFieldLocked()
    {
        return isFieldLocked(getSelectedLayoutItem().getOrderableField());
    }

    public List<String> getAllActiveRenderers()
    {
        final OrderableField field = getSelectedLayoutItem().getOrderableField();
        final List<String> ret = new ArrayList<String>();
        if(hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(field))
        {
            final Set<HackyRendererType> rendererTypeSet = hackyFieldRendererRegistry.getRendererTypes(field);
            for (HackyRendererType rendererType : rendererTypeSet)
            {
                ret.add(rendererType.getKey());
            }
        }
        else
        {
            final List<JiraRendererPlugin> rendererPlugins = getRendererManager().getAllActiveRenderers();
            for (JiraRendererPlugin rendererPlugin : rendererPlugins)
            {
                ret.add(rendererPlugin.getRendererType());
            }
        }
        return ret;
    }

    public String getFieldName()
    {
        return getSelectedLayoutItem().getOrderableField().getName();
    }

    public String getCancelUrl()
    {
        if(getId() != null)
        {
            return "ConfigureFieldLayout!default.jspa?id=" + getId();
        }
        else
        {
            return "ViewIssueFields.jspa";
        }
    }

    public String getCurrentRendererType()
    {
        return getSelectedLayoutItem().getRendererType();
    }

    public String getSelectedRendererType()
    {
        return selectedRendererType;
    }

    public void setSelectedRendererType(String selectedRendererType)
    {
        this.selectedRendererType = selectedRendererType;
    }

    public Integer getRendererEdit()
    {
        return rendererEdit;
    }

    public void setRendererEdit(Integer rendererEdit)
    {
        this.rendererEdit = rendererEdit;
    }

    protected FieldLayoutItem getSelectedLayoutItem()
    {
        List fieldLayoutItems = getOrderedList();
        return (FieldLayoutItem) fieldLayoutItems.get(getRendererEdit().intValue());
    }

    public EditableFieldLayout getFieldLayout()
    {
        if (editableFieldLayout == null)
        {
            try
            {
                if(getId() == null)
                {
                    editableFieldLayout = getFieldLayoutManager().getEditableDefaultFieldLayout();
                }
                else
                {
                    editableFieldLayout = getFieldLayoutManager().getEditableFieldLayout(getId());
                }
            }
            catch (DataAccessException e)
            {
                log.error("Error while retrieving field layout.", e);
                addErrorMessage(getText("view.issue.error.retrieving.field.layout"));
            }
        }
        return editableFieldLayout;
    }

    public long getEffectedIssuesCount() throws SearchException
    {
        long result = 0;
        if(getEffectedIssuesQuery() != null)
        {
            result = searchProvider.searchCountOverrideSecurity(getEffectedIssuesQuery(), getLoggedInUser());
        }
        return result;
    }

    public String getEffectedIssuesQueryString()
    {
        final Query effectedIssuesQuery = getEffectedIssuesQuery();
        if (effectedIssuesQuery != null)
        {
            return searchService.getQueryString(getLoggedInUser(), effectedIssuesQuery);
        }
        return "";
    }

    protected Query getEffectedIssuesQuery()
    {
        if(query == null)
        {
            // Create a search request for all effected projects
            List<Long> projectsIds = new ArrayList<Long>();
            for (GenericValue project : getFieldLayoutManager().getRelatedProjects(getFieldLayout()))
            {
                projectsIds.add(project.getLong("id"));
            }
            if(!projectsIds.isEmpty())
            {
                query = JqlQueryBuilder.newBuilder().where().project().inNumbers(projectsIds).buildQuery();
            }
        }
        return query;
    }

    protected String getFieldRedirect() throws Exception
    {
        return getRedirect(getCancelUrl());
    }

    protected void store()
    {
        try
        {
            getFieldLayoutManager().storeEditableDefaultFieldLayout((EditableDefaultFieldLayout) getFieldLayout());
        }
        catch (DataAccessException e)
        {
            addErrorMessage(getText("admin.errors.fieldlayout.error.storing"));
        }
    }
}
