package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.action.screen.AddFieldToScreenUtil;
import com.atlassian.jira.action.screen.AddFieldToScreenUtilImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.ProjectFieldScreenHelper;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v5.2
 */
@Path ("screens")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ScreensResource
{
    private final FieldScreenManager fieldScreenManager;
    private final FieldManager fieldManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final I18nHelper i18n;
    private final CustomFieldService customFieldService;
    private ProjectManager projectManager;
    private ProjectFieldScreenHelper projectFieldScreenHelper;

    public ScreensResource(final FieldScreenManager fieldScreenManager, final FieldManager fieldManager,
            final JiraAuthenticationContext jiraAuthenticationContext, final PermissionManager permissionManager,
            final I18nHelper i18n, final CustomFieldService customFieldService, final ProjectManager projectManager,
            final ProjectFieldScreenHelper projectFieldScreenHelper)
    {
        this.fieldScreenManager = fieldScreenManager;
        this.fieldManager = fieldManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.i18n = i18n;
        this.customFieldService = customFieldService;
        this.projectManager = projectManager;
        this.projectFieldScreenHelper = projectFieldScreenHelper;
    }

    /**
     * Returns a list of all tabs for the given screen
     *
     * @param screenId id of screen
     * @param projectKey the key of the project; this parameter is optional
     * @return a response containing all tabs for screen
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Contains a full representation of all visible tabs in JSON.
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen does not exist
     */
    @GET
    @Path ("{screenId}/tabs")
    public Response getAllTabs(@PathParam ("screenId") Long screenId, @QueryParam("projectKey") String projectKey)
    {
        final FieldScreen fieldScreen = getFieldScreen(screenId);

        projectAdminOrAdministratorPermissionCheck(fieldScreen, projectKey);

        List<ScreenableTabBean> tabBeans = new ArrayList<ScreenableTabBean>();
        for (FieldScreenTab tab : fieldScreen.getTabs())
        {
            tabBeans.add(new ScreenableTabBean(tab));
        }
        return Response.ok(tabBeans).cacheControl(never()).build();
    }

    /**
     * Creates tab for given screen
     *
     * @param screenId id of screen
     * @return a response containing newly created tab
     *
     * @response.representation.200.qname
     *      Newly created tab
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *       Newly created tab in JSON.
     *
     * @response.representation.200.example
     *      {@link ScreenableTabBean#DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen does not exist or tab name is invalid
     */
    @POST
    @WebSudoRequired
    @Path ("{screenId}/tabs")
    public Response addTab(@PathParam ("screenId") Long screenId, ScreenableTabBean tab)
    {
        administerPermissionCheck();

        if (StringUtils.isEmpty(tab.name))
        {
            throwWebException("name", i18n.getText("admin.screens.error.tab.empty"));
        }

        if (getTabByName(screenId, tab.name) != null)
        {
            throwWebException("name", i18n.getText("admin.screens.errors.tab.exists", tab.name));
        }

        final FieldScreenTab fieldScreenTab = getFieldScreen(screenId).addTab(tab.name);
        return Response.ok(new ScreenableTabBean(fieldScreenTab)).cacheControl(never()).build();
    }

    /**
     * Renames tab on given screen
     *
     * @param screenId id of screen
     * @return a response containing renamed tab
     *
     * @response.representation.200.qname
     *      Modified tab
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *       Modified tab in JSON.
     *
     * @response.representation.200.example
     *      {@link ScreenableTabBean#DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen does not exist or tab name is invalid
     */
    @PUT
    @WebSudoRequired
    @Path ("{screenId}/tabs/{tabId}")
    public Response renameTab(@PathParam ("screenId") Long screenId, @PathParam ("tabId") Long tabId, ScreenableTabBean tab)
    {
        administerPermissionCheck();

        final FieldScreenTab screenTab = getTabById(getFieldScreen(screenId), tabId);

        if (StringUtils.isEmpty(tab.name))
        {
            throwWebException("name", i18n.getText("admin.screens.error.tab.empty"));
        }
        else if (getTabByName(screenId, tab.name) != null)
        {
            throwWebException("name", i18n.getText("admin.screens.errors.tab.exists", tab.name));
        }
        else
        {
            screenTab.rename(tab.name);
        }
        return Response.ok(tab).cacheControl(never()).build();
    }

    /**
     * Deletes tab to give screen
     *
     * @param screenId id of screen
     * @param tabId id of tab
     * @return A response containing no content
     *
     * @response.representation.201.doc
     *       Successfully deleted tab
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen or tab does not exist
     */
    @DELETE
    @WebSudoRequired
    @Path ("{screenId}/tabs/{tabId}")
    public Response deleteTab(@PathParam ("screenId") Long screenId, @PathParam ("tabId") Long tabId)
    {
        administerPermissionCheck();

        final FieldScreen fieldScreen = getFieldScreen(screenId);
        final FieldScreenTab tabById = getTabById(fieldScreen, tabId);
        fieldScreen.removeTab(tabById.getPosition());
        return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
    }

    /**
     * Moves tab position
     *
     * @param screenId id of screen
     * @param tabId id of tab
     * @param pos position of tab
     * @return A response containing no content
     *
     * @response.representation.201.doc
     *       Successfully moved tab
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen or tab does not exist
     */
    @POST
    @WebSudoRequired
    @Path ("{screenId}/tabs/{tabId}/move/{pos}")
    public Response moveTab(@PathParam ("screenId") Long screenId, @PathParam ("tabId") Long tabId, @PathParam ("pos") Integer pos)
    {
        administerPermissionCheck();

        final FieldScreen fieldScreen = getFieldScreen(screenId);
        final FieldScreenTab tabById = getTabById(getFieldScreen(screenId), tabId);

        final int size = fieldScreen.getTabs().size();
        if (pos < 0 || pos > size) {
            throwWebException(i18n.getText("admin.screens.error.tab.incorrect.pos", "" + (size-1)));
        }

        fieldScreen.moveFieldScreenTabToPosition(tabById.getPosition(), pos);
        fieldScreen.store();

        return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
    }

    /**
     * Gets all fields for a given tab
     *
     * @param screenId id of screen
     * @param tabId id of tab
     * @param projectKey the key of the project; this parameter is optional
     * @return a response containing all fields for given tab
     *
     * @response.representation.200.doc
     *       List of fields for given tab
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen or tab does not exist
     */
    @GET
    @Path ("{screenId}/tabs/{tabId}/fields")
    public Response getAllFields(@PathParam ("screenId") Long screenId, @PathParam ("tabId") Long tabId, @QueryParam("projectKey") String projectKey)
    {
        final FieldScreen fieldScreen = getFieldScreen(screenId);

        projectAdminOrAdministratorPermissionCheck(fieldScreen, projectKey);

        final List<ScreenableFieldBean> fieldBeans = new ArrayList<ScreenableFieldBean>();
        final FieldScreenTab tab = getTabById(fieldScreen, tabId);
        final List<FieldScreenLayoutItem> fieldScreenLayoutItems = tab.getFieldScreenLayoutItems();

        for (FieldScreenLayoutItem fieldScreenLayoutItem : fieldScreenLayoutItems)
        {
            final Field field = fieldManager.getField(fieldScreenLayoutItem.getFieldId());
            if (field != null)
            {
                fieldBeans.add(new ScreenableFieldBean(field.getId(), field.getName()));
            }
        }
        return Response.ok(fieldBeans).cacheControl(never()).build();
    }

    /**
     * Gets available fields for screen. i.e ones that haven't already been added.
     *
     * @param screenId id of screen
     * @return a response containing available fields
     *
     * @response.representation.200.doc
     *       List of available fields for screen
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen does not exist
     */
    @GET
    @Path ("{screenId}/availableFields")
    public Response getFieldsToAdd(@PathParam ("screenId") Long screenId)
    {
        administerPermissionCheck();

        final List<ScreenableFieldBean> fieldBeans = new ArrayList<ScreenableFieldBean>();
        final LinkedList<OrderableField> addableFields = new LinkedList<OrderableField>(fieldManager.getOrderableFields());
        final List<FieldScreenTab> tabs = getFieldScreen(screenId).getTabs();

        // Iterate over the field screen's layout items and remove them from addableFields
        for (FieldScreenTab fieldScreenTab : tabs)
        {
            for (FieldScreenLayoutItem fieldScreenLayoutItem : fieldScreenTab.getFieldScreenLayoutItems())
            {
                addableFields.remove(fieldScreenLayoutItem.getOrderableField());
            }
        }

        // a nasty hack to not allow Unscreenable fields to be placed onto a screen (an example is
        // CommentSystemField)
        for (Iterator iterator = addableFields.iterator(); iterator.hasNext(); )
        {
            OrderableField orderableField = (OrderableField) iterator.next();
            if (fieldManager.isUnscreenableField(orderableField))
            {
                iterator.remove();
            }
        }

        Collections.sort(addableFields);

        for (OrderableField addableField : addableFields)
        {
            fieldBeans.add(new ScreenableFieldBean(addableField.getId(), addableField.getName()));
        }

        return Response.ok(fieldBeans).cacheControl(never()).build();
    }

    /**
     * Removes field from given tab
     *
     * @param screenId id of screen
     * @param tabId id of tab
     * @return no content
     *
     * @response.representation.201.doc
     *       Successfully removed field from tab
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen or tab does not exist
     */
    @DELETE
    @WebSudoRequired
    @Path ("{screenId}/tabs/{tabId}/fields/{id}")
    public Response removeField(@PathParam ("screenId") Long screenId, @PathParam ("tabId") Long tabId, @PathParam ("id") String id)
    {
        administerPermissionCheck();

        final FieldScreenTab tab = getTabById(getFieldScreen(screenId), tabId);
        if (tab.getFieldScreenLayoutItem(id) == null)
        {
            throwWebException(i18n.getText("admin.screens.error.field.doesnt.exist", id));
        }
        tab.getFieldScreen().removeFieldScreenLayoutItem(id);
        return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
    }

    /**
     * Moves field on the given tab
     *
     * @param screenId id of screen
     * @param tabId id of tab
     * @return no content
     *
     * @response.representation.201.doc
     *       Successfully moved tab
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen or tab does not exist. Or move cooridinates invalid.
     */
    @POST
    @WebSudoRequired
    @Path ("{screenId}/tabs/{tabId}/fields/{id}/move")
    public Response moveField(@PathParam ("screenId") Long screenId, @PathParam ("tabId") Long tabId, @PathParam ("id") String id, MoveFieldBean moveField)
    {
        administerPermissionCheck();

        final FieldScreenTab tab = getTabById(getFieldScreen(screenId), tabId);


        final FieldScreenLayoutItem fieldToMove = tab.getFieldScreenLayoutItem(id);

        if (fieldToMove == null) {
            throwWebException(i18n.getText("admin.screens.error.field.doesnt.exist", id));
        }
        else
        {
            if (moveField.after != null)
            {
                final String[] split = moveField.after.getPath().split("/");
                final String afterField = split[split.length - 1];

                if (tab.getFieldScreenLayoutItem(afterField) == null)
                {
                    throwWebException(i18n.getText("admin.screens.error.field.move.invalid", id, afterField));
                }

                final FieldScreenLayoutItem afterFieldItem = tab.getFieldScreenLayoutItem(afterField);
                int target = afterFieldItem.getPosition();


                if (target < fieldToMove.getPosition()) {
                    target = target + 1;
                }
                if (target >= tab.getFieldScreenLayoutItems().size()) {
                    tab.moveFieldScreenLayoutItemLast(fieldToMove.getPosition());
                }
                else if (target < 0)
                {
                    tab.moveFieldScreenLayoutItemFirst(fieldToMove.getPosition());
                }
                else
                {
                    final Map<Integer, FieldScreenLayoutItem> layoutItemMap = new HashMap<Integer, FieldScreenLayoutItem>();
                    layoutItemMap.put(target, fieldToMove);
                    tab.moveFieldScreenLayoutItemToPosition(layoutItemMap);
                }
            }
            else
            {
                if (moveField.position == MoveFieldBean.Position.Last)
                {
                    tab.moveFieldScreenLayoutItemLast(fieldToMove.getPosition());
                }
                else if (moveField.position == MoveFieldBean.Position.First)
                {
                    tab.moveFieldScreenLayoutItemFirst(fieldToMove.getPosition());
                }
            }
        }
        return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
    }

    /**
     * Adds field to the given tab
     *
     * @param screenId id of screen
     * @param tabId id of tab
     * @return no content
     *
     * @response.representation.200.doc
     *      Newly added field as json
     *
     * @response.representation.200.example
     *      {@link ScreenableFieldBean#DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if you do not have permissions
     *
     * @response.representation.400.doc
     *      Returned if screen,tab or field does not exist.
     */
    @POST
    @WebSudoRequired
    @Path ("{screenId}/tabs/{tabId}/fields")
    public Response addField(@PathParam ("screenId") Long screenId, @PathParam ("tabId") Long tabId, AddFieldBean field)
    {
        administerPermissionCheck();

        if (field.getFieldId() == null)
        {
            throwWebException("fieldId", i18n.getText("admin.screens.error.field.add.required"));
        }

        final Field resultField = addFieldToScreen(screenId, tabId, field.getFieldId());

        return Response.ok(new ScreenableFieldBean(resultField.getId(), resultField.getName())).cacheControl(never()).build();
    }

    /**
     * Adds field or custom field to the default tab
     *
     * @param fieldId id of field / custom field
     * @return no content
     *
     * @response.representation.201.cos
     *      Successfully added field to default screen / default tab
     *
     * @response.representation.401.doc
     *      Returned if you do not have administrator permissions
     *
     * @response.representation.400.doc
     *      Returned if screen, tab or field does not exist or field is already present on a selected tab
     */
    @POST
    @WebSudoRequired
    @Path ("addToDefault/{fieldId}")
    public Response addFieldToDefaultScreen(@PathParam ("fieldId") String fieldId)
    {
        administerPermissionCheck();

        final FieldScreen fieldScreen = fieldScreenManager.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);
        final List<FieldScreenTab> sortedTabs = sortFieldScreenTabs(fieldScreen.getTabs());
        //in order to get "default" tab for a screen we need to sort tabs by id and get the first one
        final FieldScreenTab defaultTabOfDefaultScreen = sortedTabs.iterator().next();
        final Field field = fieldManager.getField(fieldId);

        if (field instanceof CustomField)
        {
            addCustomField(fieldId, defaultTabOfDefaultScreen);
        }
        else
        {
            addFieldToScreen(FieldScreen.DEFAULT_SCREEN_ID, defaultTabOfDefaultScreen.getId(), fieldId);
        }

        return Response.ok(Response.Status.CREATED).cacheControl(never()).build();
    }

    private List<FieldScreenTab> sortFieldScreenTabs(final List<FieldScreenTab> tabs)
    {
        final List<FieldScreenTab> sortedTabs = Lists.newArrayList(tabs);
        if (sortedTabs.size() > 1)
        {
            Collections.sort(sortedTabs, new Comparator<FieldScreenTab>()
            {
                @Override
                public int compare(final FieldScreenTab fieldScreenTab1, final FieldScreenTab fieldScreenTab2)
                {
                    return fieldScreenTab1.getId().compareTo(fieldScreenTab2.getId());
                }
            });
        }
        return sortedTabs;
    }

    private void addCustomField(final String fieldId, final FieldScreenTab defaultTabOfDefaultScreen)
    {
        final ServiceOutcome<List<Long>> outcome = customFieldService.addToScreenTabs(jiraAuthenticationContext.getLoggedInUser(),
                CustomFieldUtils.getCustomFieldId(fieldId),
                Lists.newArrayList(defaultTabOfDefaultScreen.getId()));

        if (outcome.getErrorCollection().hasAnyErrors())
        {
            throwWebException(outcome.getErrorCollection());
        }
    }

    private Field addFieldToScreen(final Long screenId, final Long tabId, final String fieldId)
    {
        final FieldScreenTab tab = getTabById(getFieldScreen(screenId), tabId);

        final AddFieldToScreenUtil addFieldToScreenUtil = new AddFieldToScreenUtilImpl(jiraAuthenticationContext, fieldManager, fieldScreenManager);
        addFieldToScreenUtil.setFieldScreenId(screenId);
        addFieldToScreenUtil.setTabPosition(tab.getPosition());
        addFieldToScreenUtil.setFieldId(new String[] { fieldId });
        addFieldToScreenUtil.setFieldPosition("" + (tab.getFieldScreenLayoutItems().size() + 1));

        final com.atlassian.jira.util.ErrorCollection errorCollection = addFieldToScreenUtil.validate();

        if (errorCollection.hasAnyErrors())
        {
            throwWebException(errorCollection);
        }

        addFieldToScreenUtil.execute();
        return fieldManager.getField(fieldId);
    }

    private FieldScreenTab getTabById(FieldScreen screen, Long tabId)
    {
        final List<FieldScreenTab> tabs = screen.getTabs();
        for (FieldScreenTab tab : tabs)
        {
            if (tab.getId().equals(tabId))
            {
                return tab;
            }
        }
        throwWebException(i18n.getText("admin.screens.error.tab.doesnt.exist", tabId));
        return null;
    }

    private FieldScreenTab getTabByName(Long screenId, String name)
    {
        final List<FieldScreenTab> tabs = getFieldScreen(screenId).getTabs();
        for (FieldScreenTab tab : tabs)
        {
            if (tab.getName().equals(name))
            {
                return tab;
            }
        }
        return null;
    }

    private FieldScreen getFieldScreen(Long screenId)
    {
        final FieldScreen fieldScreen = fieldScreenManager.getFieldScreen(screenId);
        if (fieldScreen == null)
        {
            throwWebException(i18n.getText("admin.screens.error.doesnt.exist", "" + screenId));
        }
        return fieldScreen;
    }

    private void throwWebException(final String field, final String message)
    {
        com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError(field, message);
        throwWebException(errorCollection);
    }

    private void throwWebException(final String message)
    {
        com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(message);
        throwWebException(errorCollection);
    }

    private void throwWebException(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new RESTException(ErrorCollection.of(errorCollection));
    }

    private void administerPermissionCheck()
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, jiraAuthenticationContext.getLoggedInUser()))
        {
            throw new NotAuthorisedWebException();
        }
    }

    private void projectAdminOrAdministratorPermissionCheck(final FieldScreen fieldScreen, final String projectKey)
    {
        if (projectKey == null)
        {
            administerPermissionCheck();
        }
        else if (!projectFieldScreenHelper.canUserViewFieldScreenForProject(jiraAuthenticationContext.getUser(), fieldScreen,
                    projectManager.getProjectObjByKey(projectKey)))
        {
            throw new NotAuthorisedWebException();
        }
    }
}
