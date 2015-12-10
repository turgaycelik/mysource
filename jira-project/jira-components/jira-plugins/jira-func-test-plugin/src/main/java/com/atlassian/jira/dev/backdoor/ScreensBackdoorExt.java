package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.action.screen.AddFieldToScreenUtil;
import com.atlassian.jira.action.screen.AddFieldToScreenUtilImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.issue.fields.screen.FieldScreenService;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.testkit.plugin.applinks.ScreensBackdoorResource;
import com.atlassian.jira.testkit.plugin.util.CacheControl;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Extended ScreensBackdoor.
 *
 * @since v5.2
 */
@Path ("screens")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ScreensBackdoorExt extends ScreensBackdoorResource
{
    private final FieldScreenManager fieldScreenManager;
    private final FieldScreenService fieldScreenService;
    private final FieldManager fieldManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public ScreensBackdoorExt(FieldScreenManager fieldScreenManager, FieldScreenService fieldScreenService, FieldManager fieldManager, JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(fieldScreenManager, fieldManager);
        this.fieldScreenManager = fieldScreenManager;
        this.fieldScreenService = fieldScreenService;
        this.fieldManager = fieldManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @GET
    @Path ("addField")
    public Response addFieldToScreen(@QueryParam ("screen") String screen, @QueryParam ("tab") String tabName, @QueryParam ("field") String field, @QueryParam ("position") String position)
    {
        final AddFieldToScreenUtil addFieldToScreenUtil = new AddFieldToScreenUtilImpl(jiraAuthenticationContext, fieldManager, fieldScreenManager);
        final Set<OrderableField> navigableFields = fieldManager.getOrderableFields();
        for (OrderableField navigableField : navigableFields)
        {
            if (navigableField.getName().equals(field))
            {
                final FieldScreen fieldScreen = getScreenByName(screen);
                FieldScreenTab tab = null;

                if (tabName != null)
                {
                    final List<FieldScreenTab> tabs = fieldScreen.getTabs();
                    for (FieldScreenTab fieldScreenTab : tabs)
                    {
                        if (fieldScreenTab.getName().equals(tabName))
                        {
                            tab = fieldScreenTab;
                        }
                    }
                }

                if (tab == null)
                {
                    tab = fieldScreen.getTab(0);
                }

                addFieldToScreenUtil.setFieldScreenId(fieldScreen.getId());
                addFieldToScreenUtil.setTabPosition(tab.getPosition());
                addFieldToScreenUtil.setFieldId(new String[] { navigableField.getId() });
                position = position == null ? "" + (tab.getFieldScreenLayoutItems().size() + 1) : position;
                addFieldToScreenUtil.setFieldPosition(position);
                final ErrorCollection errorCollection = addFieldToScreenUtil.validate();
                if (!errorCollection.hasAnyErrors())
                {
                    addFieldToScreenUtil.execute();
                }
                break;
            }
        }

        return Response.ok().cacheControl(CacheControl.never()).build();
    }


    @GET
    @Path("copyScreen")
    public Response copyScreen(@QueryParam("screen") String screen, @QueryParam("copyName") String copyName, @QueryParam("copyDescription") String copyDescription)
    {
        FieldScreen screenToCopy = getScreenByName(screen);

        ServiceOutcome<FieldScreen> result = fieldScreenService.copy(screenToCopy, copyName, copyDescription, ApplicationUsers.from(jiraAuthenticationContext.getLoggedInUser()));

        if (!result.isValid())
        {
            return Response.status(BAD_REQUEST).
                    entity((result.getErrorCollection().getErrorMessages().iterator().next())).
                    cacheControl(CacheControl.never()).build();
        }

        return Response.ok(result.getReturnedValue().getId()).cacheControl(CacheControl.never()).build();
    }
}
