package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.DefaultHackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.testkit.plugin.FieldConfigurationBackdoor;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A backdoor for manipulating field configurations. Functionality is split between the TestKit and the func-test
 * Backdoor.
 *
 * @since v5.2
 */
@AnonymousAllowed
@Path("fieldConfiguration")
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
public class FieldConfigurationBackdoorExt extends FieldConfigurationBackdoor
{
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final JiraAuthenticationContext authenticationContext;
    private final HackyFieldRendererRegistry hackyFieldRendererRegistry;

    public FieldConfigurationBackdoorExt(FieldLayoutManager fieldLayoutManager, CustomFieldManager customFieldManager, ProjectManager projectManager,
            FieldConfigSchemeManager fieldConfigSchemeManager, RendererManager rendererManager, JiraAuthenticationContext authenticationContext)
    {
        super(fieldLayoutManager, customFieldManager, projectManager, fieldConfigSchemeManager);
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.authenticationContext = authenticationContext;
        this.hackyFieldRendererRegistry = new DefaultHackyFieldRendererRegistry();
    }

    @GET
    @Path ("copy")
    public Response copyFieldConfiguration(@QueryParam ("name") String fieldConfigName, @QueryParam ("copyName") String newFieldConfigName)
    {
        EditableFieldLayout originalLayout = getFieldLayout(fieldConfigName);
        EditableFieldLayout editableFieldLayout = new EditableFieldLayoutImpl(null, originalLayout.getFieldLayoutItems());
        if (StringUtils.isBlank(newFieldConfigName))
        {
            editableFieldLayout.setName("Copy of " + originalLayout.getName());
        }
        else
        {
            editableFieldLayout.setName(newFieldConfigName);
        }
        editableFieldLayout.setDescription(originalLayout.getDescription());
        fieldLayoutManager.storeEditableFieldLayout(editableFieldLayout);

        return Response.ok().build();
    }

    @GET
    @Path ("renderer")
    public Response setRenderer(@QueryParam ("fieldConfigurationName") String fieldConfigName, @QueryParam ("fieldId") final String fieldId,
            @QueryParam ("renderer") final String renderer)
    {
        EditableFieldLayout editableFieldLayout = getFieldLayout(fieldConfigName);
        FieldLayoutItem item = editableFieldLayout.getFieldLayoutItem(fieldId);

        editableFieldLayout.setRendererType(item, getRendererType(item.getOrderableField(), renderer));

        fieldLayoutManager.storeEditableFieldLayout(editableFieldLayout);
        fieldLayoutManager.refresh();
        return Response.ok().build();
    }

    private String getRendererType(final OrderableField field, final String rendererName)
    {
        if (hackyFieldRendererRegistry.shouldOverrideDefaultRenderers(field))
        {
            return Iterables.getOnlyElement(Iterables.filter(hackyFieldRendererRegistry.getRendererTypes(field), new Predicate<HackyRendererType>()
            {
                @Override
                public boolean apply(@Nullable HackyRendererType input)
                {
                    return StringUtils.equals(rendererName, authenticationContext.getI18nHelper().getText(input.getDisplayNameI18nKey()));
                }
            })).getKey();
        }

        return Iterables.getOnlyElement(Iterables.filter(rendererManager.getAllActiveRenderers(), new Predicate<JiraRendererPlugin>()
        {
            @Override
            public boolean apply(@Nullable JiraRendererPlugin input)
            {
                return StringUtils.equals(rendererName, input.getDescriptor().getName());
            }
        })).getRendererType();
    }
}
