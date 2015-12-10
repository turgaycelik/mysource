package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.testkit.client.WorkflowSchemesControl;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.UserBean;
import com.atlassian.jira.webtests.util.RunOnce;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.sun.jersey.api.client.ClientResponse.Status.BAD_REQUEST;
import static com.sun.jersey.api.client.ClientResponse.Status.CREATED;
import static com.sun.jersey.api.client.ClientResponse.Status.FORBIDDEN;
import static com.sun.jersey.api.client.ClientResponse.Status.NOT_FOUND;
import static com.sun.jersey.api.client.ClientResponse.Status.NO_CONTENT;
import static com.sun.jersey.api.client.ClientResponse.Status.OK;
import static com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED;

/**
 * @since 5.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestWorkflowSchemeResource extends RestFuncTest
{
    private static final RunOnce RESTORE = new RunOnce();

    private static final String WF_ONE = "One";
    private static final String WF_TWO = "Two";
    private static final String WF_THREE = "Three";
    private static final String WF_FOUR = "Four";
    private static final String WF_FIVE = "Five";
    private static final String WF_BAD = "This Workflow Does not Exist";

    private static final List<String> WORKFLOWS = ImmutableList.of(WF_ONE, WF_TWO, WF_THREE, WF_FOUR, WF_FIVE);

    private WorkflowSchemeClient client;

    public void testGet()
    {
        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testGet").setDescription("testGet");
        scheme.setDefaultWorkflow(WF_ONE).setMapping(IssueType.BUG.name, WF_TWO);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());
        assertEquals(expectedScheme, asSimpleScheme(client.getWorkflowScheme(scheme.getId(), false)));

        //Create a draft.
        WorkflowSchemeData draft = schemesControl.createDraft(scheme);

        draft.setDefaultWorkflow(null).setMapping(IssueType.IMPROVMENT.id, WF_FIVE)
                .setMapping(IssueType.NEW_FEATURE.id, WF_ONE);
        schemesControl.updateDraftScheme(scheme.getId(), draft);

        //Should return the orig scheme unless we pass true.
        assertEquals(expectedScheme, asSimpleScheme(client.getWorkflowScheme(scheme.getId(), false)));

        //The draft should now be visible.
        WorkflowSchemeClient.WorkflowScheme actualDraft = client.getWorkflowScheme(scheme.getId(), true);
        assertEquals(ADMIN_USERNAME, actualDraft.getLastModifiedUser().name);
        assertNotNull(actualDraft.getLastModified());

        SimpleWorkflowScheme expectedDraft = asSimpleScheme(scheme.getId());
        assertEquals(expectedDraft, asSimpleScheme(actualDraft));

        //Draft is now gone again.
        schemesControl.discardDraftScheme(scheme.getId());

        assertEquals(expectedScheme, asSimpleScheme(client.getWorkflowScheme(scheme.getId(), false)));
        assertEquals(expectedScheme, asSimpleScheme(client.getWorkflowScheme(scheme.getId(), true)));

        //Test not found
        Response<?> response = client.getWorkflowSchemeResponse(2);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);
    }

    public void testGetDraft()
    {
        client = client.asDraft();

        //Test get draft even when you don't have a parent.
        Response<WorkflowSchemeClient.WorkflowScheme> response = client.getWorkflowSchemeResponse(2);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testGetDraft");
        scheme.setDefaultWorkflow(WF_ONE).setMapping(IssueType.BUG.name, WF_TWO);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TGD";
        backdoor.project().addProject("TGD", pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Test when the parent does not have a draft.
        response = client.getWorkflowSchemeResponse(scheme.getId());
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        schemesControl.createDraft(scheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId(), true);
        assertEquals(expectedScheme, asSimpleScheme(client.getWorkflowScheme(scheme.getId())));

        //Draft is now gone again.
        schemesControl.discardDraftScheme(scheme.getId());

        response = client.getWorkflowSchemeResponse(scheme.getId());
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        response = client.anonymous().getWorkflowSchemeResponse(scheme.getId());
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        response = client.loginAs("fred").getWorkflowSchemeResponse(scheme.getId());
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testGetNoPermission()
    {
        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testGetNoPermission").setDescription("testGetNoPermission");
        scheme.setDefaultWorkflow(WF_ONE).setMapping(IssueType.BUG.name, WF_TWO);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        Response<WorkflowSchemeClient.WorkflowScheme> response = client.anonymous().getWorkflowSchemeResponse(scheme.getId());
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        response = client.loginAs("fred").getWorkflowSchemeResponse(scheme.getId());
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testCreateSchemeIssueTypes()
    {
       assertCreateScheme("IssueType", new Function<SimpleWorkflowScheme, WorkflowSchemeClient.WorkflowScheme>()
       {
           @Override
           public WorkflowSchemeClient.WorkflowScheme apply(SimpleWorkflowScheme input)
           {
               return input.asRestBean();
           }
       });
    }

    private void assertCreateScheme(String prefix, Function<SimpleWorkflowScheme, WorkflowSchemeClient.WorkflowScheme> toRest)
    {
        SimpleWorkflowScheme scheme = new SimpleWorkflowScheme();
        scheme.setName(prefix + "testCreateScheme");
        scheme.setDescription("description");
        scheme.setDefaultWorkflow(WF_ONE);
        scheme.setMapping(IssueType.BUG, WF_TWO);
        scheme.setMapping(IssueType.IMPROVMENT, WF_TWO);
        scheme.setMapping(IssueType.NEW_FEATURE, WF_THREE);
        scheme.setMapping(IssueType.TASK, WF_FIVE);

        Response<WorkflowSchemeClient.WorkflowScheme> actualSchemeResponse = client.createSchemeResponse(toRest.apply(scheme));
        assertEquals(CREATED.getStatusCode(), actualSchemeResponse.statusCode);

        final WorkflowSchemeClient.WorkflowScheme actualScheme = actualSchemeResponse.body;
        assertNotNull(actualScheme.getId());
        scheme.setId(actualScheme.getId());
        scheme.setSelf(selfUri(actualScheme.getId(), false));

        assertEquals(scheme, asSimpleScheme(actualScheme));

        //Lets try a duplicate name.
        Response<WorkflowSchemeClient.WorkflowScheme> response = client.createSchemeResponse(toRest.apply(scheme));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try noname.
        response = client.createSchemeResponse(toRest.apply(scheme.copy().setName(null)));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try badDefault.
        scheme.setName(prefix + "testCreateScheme2");
        response = client.createSchemeResponse(toRest.apply(scheme.copy().setDefaultWorkflow("badWorkflow")));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try bad workflowMapping
        response = client.createSchemeResponse(toRest.apply(scheme.copy().setMapping(IssueType.IMPROVMENT, "random")));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try a bad issue type.
        response = client.createSchemeResponse(toRest.apply(scheme.copy().setMapping(IssueType.BAD, WF_FOUR)));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try as anonymous
        response = client.anonymous().createSchemeResponse(toRest.apply(scheme));
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Try an something without permission.
        response = client.loginAs("fred").createSchemeResponse(toRest.apply(scheme));
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testDeleteScheme()
    {
        SimpleWorkflowScheme scheme = new SimpleWorkflowScheme();
        scheme.setName("testDeleteScheme");
        scheme.setDescription("description");
        scheme.setDefaultWorkflow(WF_ONE);

        final WorkflowSchemesControl control = backdoor.workflowSchemes();
        Long id = control.createScheme(scheme.asBackdoorBean()).getId();
        Response<Void> response = client.deleteSchemeResponse(id);
        assertEquals(NO_CONTENT.getStatusCode(), response.statusCode);
        assertSchemeGone(id);

        response = client.deleteSchemeResponse(id);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TDS";
        backdoor.project().addProject("testDeleteScheme", pkey, "admin");
        id = control.createScheme(scheme.asBackdoorBean()).getId();
        backdoor.project().setWorkflowScheme(pkey, id);

        response = client.deleteSchemeResponse(id);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Remove the project. The scheme should not be deleteable.
        backdoor.project().deleteProject(pkey);

        //Try as anonymous
        response = client.anonymous().deleteSchemeResponse(id);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Try on someone without permission.
        response = client.loginAs("fred").deleteSchemeResponse(id);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);

        //The admin should be able to delete the scheme
        response = client.loginAs(ADMIN_USERNAME).deleteSchemeResponse(id);
        assertEquals(NO_CONTENT.getStatusCode(), response.statusCode);
        assertSchemeGone(id);
    }

    public void testDeleteDraftScheme()
    {
        client = client.asDraft();

        SimpleWorkflowScheme scheme = new SimpleWorkflowScheme();
        scheme.setName("testDeleteDrafScheme");
        scheme.setDescription("testDeleteDrafScheme");
        scheme.setDefaultWorkflow(WF_ONE);

        final WorkflowSchemesControl control = backdoor.workflowSchemes();
        WorkflowSchemeData parentData = control.createScheme(scheme.asBackdoorBean());
        long parentId = parentData.getId();

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TDDS";
        backdoor.project().addProject("testDeleteDrafScheme", pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, parentId);

        control.createDraft(parentData);

        Response<Void> response = client.deleteSchemeResponse(parentId);
        assertEquals(NO_CONTENT.getStatusCode(), response.statusCode);
        assertDraftDoesNotExist(parentId);

        response = client.deleteSchemeResponse(parentId);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        control.createDraft(parentData);

        //Try as anonymous
        response = client.anonymous().deleteSchemeResponse(parentId);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Try on someone without permission.
        response = client.loginAs("fred").deleteSchemeResponse(parentId);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);

        //The admin should be able to delete the scheme
        response = client.loginAs(ADMIN_USERNAME).deleteSchemeResponse(parentId);
        assertEquals(NO_CONTENT.getStatusCode(), response.statusCode);
        assertDraftDoesNotExist(parentId);
    }

    public void testCreateDraft()
    {
        SimpleWorkflowScheme scheme = new SimpleWorkflowScheme();
        scheme.setName("testCreateDraft");
        scheme.setDescription("testCreateDraft");
        scheme.setDefaultWorkflow(WF_ONE);

        final WorkflowSchemesControl control = backdoor.workflowSchemes();
        WorkflowSchemeData parentData = control.createScheme(scheme.asBackdoorBean());
        long parentId = parentData.getId();

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TCD";
        backdoor.project().addProject("testCreateDraft", pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, parentId);

        WorkflowSchemeClient.WorkflowScheme draft = client.createDraft(parentData.getId());

        scheme.setWorkflowMappings(Maps.<IssueType, String>newHashMap());
        SimpleWorkflowScheme expectedDraft = updateDraftInfo(scheme.copyAsDraft(), draft, parentId);
        assertEquals(expectedDraft, asSimpleScheme(draft));

        //Can't create a second draft.
        Response<WorkflowSchemeClient.WorkflowScheme> response = client.createDraftResponse(parentData.getId());
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        control.discardDraftScheme(parentId);

        //Try as anonymous
        response = client.anonymous().createDraftResponse(parentId);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Try on someone without permission.
        response = client.loginAs("fred").createDraftResponse(parentId);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testUpdateIssueType()
    {
        assertUpdate("IssTp", new Function<SimpleWorkflowScheme, WorkflowSchemeClient.WorkflowScheme>()
        {
            @Override
            public WorkflowSchemeClient.WorkflowScheme apply(SimpleWorkflowScheme input)
            {
                return input.asRestBean();
            }
        });
    }

    public void testUpdateNoPermission()
    {
        SimpleWorkflowScheme scheme = new SimpleWorkflowScheme();
        scheme.setName("testUpdateNoPermission");
        scheme.setDescription("testUpdateNoPermission");
        scheme.setDefaultWorkflow(WF_ONE);

        final WorkflowSchemesControl control = backdoor.workflowSchemes();
        Long id = control.createScheme(scheme.asBackdoorBean()).getId();
        scheme.setId(id);

        SimpleWorkflowScheme updateScheme = new SimpleWorkflowScheme(scheme);
        updateScheme.setName("NewName");

        Response<WorkflowSchemeClient.WorkflowScheme> response = client.anonymous().updateWorkflowSchemeResponse(updateScheme.asRestBean());
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        response = client.loginAs("fred").updateWorkflowSchemeResponse(updateScheme.asRestBean());
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testUpdateDraft()
    {
        client = client.asDraft();

        SimpleWorkflowScheme scheme = new SimpleWorkflowScheme();
        scheme.setName("testUpdateDraft");
        scheme.setDescription("testUpdateDraft");
        scheme.setDefaultWorkflow(WF_ONE).setMapping(IssueType.IMPROVMENT, WF_FIVE);

        final WorkflowSchemesControl control = backdoor.workflowSchemes();
        Long id = control.createScheme(scheme.asBackdoorBean()).getId();
        scheme.setId(id);

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TUD";
        backdoor.project().addProject("TUD", pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, id);

        SimpleWorkflowScheme draftScheme = new SimpleWorkflowScheme(scheme);

        //Lets update the name to something invalid and make sure the scheme is not updated.
        draftScheme.setMapping(IssueType.NEW_FEATURE, WF_ONE).setDefaultWorkflow(WF_FIVE);
        draftScheme.setName(StringUtils.repeat("&&", 255));

        Response<WorkflowSchemeClient.WorkflowScheme> response = client.updateWorkflowSchemeResponse(draftScheme.asRestBean());
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertDraftDoesNotExist(id);

        draftScheme.setName("testUpdateDraft2");

        //This should create a draft.
        response = client.updateWorkflowSchemeResponse(draftScheme.asRestBean());

        SimpleWorkflowScheme expectedScheme = updateDraftInfo(scheme.copyAsDraft(), response.body, id);
        expectedScheme.setName("testUpdateDraft2");
        expectedScheme.setMapping(IssueType.NEW_FEATURE, WF_ONE).setDefaultWorkflow(WF_FIVE);

        assertEquals(OK.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme, asSimpleScheme(response.body));

        draftScheme.setName("testUpdateDraft4").setDefaultWorkflow(null).clearMappings().setMapping(IssueType.TASK, WF_TWO);

        //This should update the draft.
        response = client.updateWorkflowSchemeResponse(draftScheme.asRestBean());
        expectedScheme.setName(draftScheme.getName()).clearMappings().setMapping(IssueType.TASK, WF_TWO);
        assertEquals(OK.getStatusCode(), response.statusCode);
        updateDraftInfo(scheme, response.body, id);
        assertEquals(expectedScheme, asSimpleScheme(response.body));

        //Tying to update scheme that does not exist should return a 404.
        draftScheme.setId(Long.MAX_VALUE);
        response = client.updateWorkflowSchemeResponse(draftScheme.asRestBean());
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        SimpleWorkflowScheme badScheme = new SimpleWorkflowScheme().setName("GoodName").setId(id);
        final WorkflowSchemeData duplicate = control.createScheme(new WorkflowSchemeData().setName("testUpdateDraftDuplicate"));

        //Try a duplicate name.
        response = client.updateWorkflowSchemeResponse(badScheme.copy().setName(duplicate.getName()).asRestBean());
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try badDefault.
        response = client.updateWorkflowSchemeResponse(badScheme.copy().setDefaultWorkflow(WF_BAD).asRestBean());
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try bad workflowMapping
        response = client.updateWorkflowSchemeResponse(badScheme.copy().setMapping(IssueType.IMPROVMENT, WF_BAD).asRestBean());
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try a bad issue type.
        response = client.updateWorkflowSchemeResponse(badScheme.copy().setMapping(IssueType.BAD, WF_FOUR).asRestBean());
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Anonymous should not be able to hit the resource.
        response = client.anonymous().updateWorkflowSchemeResponse(expectedScheme.asRestBean());
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Non system admin should not have permission.
        response = client.loginAs("fred").updateWorkflowSchemeResponse(expectedScheme.asRestBean());
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    private void assertUpdate(String prefix, Function<SimpleWorkflowScheme, WorkflowSchemeClient.WorkflowScheme> toRest)
    {
        SimpleWorkflowScheme scheme = new SimpleWorkflowScheme();
        scheme.setName(prefix+ "testUpdate");
        scheme.setDescription("testUpdate");
        scheme.setDefaultWorkflow(WF_ONE);

        final WorkflowSchemesControl control = backdoor.workflowSchemes();
        Long id = control.createScheme(scheme.asBackdoorBean()).getId();

        SimpleWorkflowScheme expectedScheme = new SimpleWorkflowScheme(scheme);

        //Update the name and description.
        scheme = new SimpleWorkflowScheme().setId(id);
        scheme.setName(prefix + "testUpdate2").setDescription("testUpdate2");

        expectedScheme.setId(id);
        expectedScheme.setName(scheme.getName()).setDescription(scheme.getDescription());
        expectedScheme.setSelf(selfUri(id, false));
        expectedScheme.setWorkflowMappings(Maps.<IssueType, String>newHashMap());

        Response<WorkflowSchemeClient.WorkflowScheme> response = client.updateWorkflowSchemeResponse(toRest.apply(scheme));
        assertEquals(OK.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme, asSimpleScheme(response.body));

        //Update a few mappings.
        scheme = new SimpleWorkflowScheme().setId(id);
        scheme.setMapping(IssueType.BUG, WF_ONE).setMapping(IssueType.TASK, WF_TWO);

        expectedScheme.setMapping(IssueType.BUG, WF_ONE)
                .setMapping(IssueType.TASK, WF_TWO);

        response = client.updateWorkflowSchemeResponse(toRest.apply(scheme));
        assertEquals(OK.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(response.body));

        //Try to change the name of an active workflow. Should be fine.
        final String pkey = "TUS" + prefix.toUpperCase();
        backdoor.project().addProject(prefix + "testUpdate", pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        scheme = new SimpleWorkflowScheme().setId(id);
        scheme.setName(prefix + "testUpdate3").setDescription("testUpdate3");

        expectedScheme.setName(scheme.getName()).setDescription(scheme.getDescription());

        response = client.updateWorkflowSchemeResponse(toRest.apply(scheme));
        assertEquals(OK.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(response.body));

        //Should not be able to change active scheme.
        scheme = new SimpleWorkflowScheme().setId(id);
        scheme.setDefaultWorkflow(WF_FOUR).setMapping(IssueType.IMPROVMENT, WF_TWO);

        response = client.updateWorkflowSchemeResponse(toRest.apply(scheme));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(id));

        //Lets check the call that creates the draft if we pass the magic flag.
        WorkflowSchemeClient.WorkflowScheme restBean = toRest.apply(scheme);
        restBean.setUpdateDraftIfNeeded(true);

        expectedScheme.setDraft(true);
        expectedScheme.setSelf(selfUri(id, true));
        expectedScheme.clearMappings().setDefaultWorkflow(WF_FOUR).setMapping(IssueType.IMPROVMENT, WF_TWO);

        expectedScheme.setOriginalDefaultWorkflow(WF_ONE);
        expectedScheme.setOriginalMapping(IssueType.BUG, WF_ONE);
        expectedScheme.setOriginalMapping(IssueType.TASK, WF_TWO);

        response = client.updateWorkflowSchemeResponse(restBean);
        assertEquals(OK.getStatusCode(), response.statusCode);

        SimpleWorkflowScheme actualScheme = asSimpleScheme(response.body);
        assertNotNull(actualScheme.getLastModifiedUser());
        assertNotNull(actualScheme.getLastModified());
        expectedScheme.setLastModified(actualScheme.getLastModified());
        expectedScheme.setLastModifiedUser(actualScheme.getLastModifiedUser());
        expectedScheme.setId(actualScheme.getId());

        assertEquals(expectedScheme.copyWithDefault(), actualScheme);

        //Lets update the name to something invalid and make sure the scheme is not updated.
        scheme.setMapping(IssueType.NEW_FEATURE, WF_FIVE).setDefaultWorkflow(WF_FIVE);
        scheme.setName(StringUtils.repeat("&&", 255));
        restBean =  toRest.apply(scheme);
        restBean.setUpdateDraftIfNeeded(true);

        response = client.updateWorkflowSchemeResponse(restBean);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Lets update the name to something valid and make sure the scheme is updated.
        scheme.setName(prefix + "testUpdate4");
        restBean =  toRest.apply(scheme);
        restBean.setUpdateDraftIfNeeded(true);

        expectedScheme.setName(scheme.getName())
                .setDefaultWorkflow(WF_FIVE)
                .setMapping(IssueType.NEW_FEATURE, WF_FIVE);

        response = client.updateWorkflowSchemeResponse(restBean);
        assertEquals(OK.getStatusCode(), response.statusCode);

        actualScheme = asSimpleScheme(response.body);
        assertNotNull(actualScheme.getLastModifiedUser());
        assertNotNull(actualScheme.getLastModified());
        expectedScheme.setLastModified(actualScheme.getLastModified());
        expectedScheme.setLastModifiedUser(actualScheme.getLastModifiedUser());
        assertEquals(expectedScheme.copyWithDefault(), actualScheme);

        //Make sure that not passing the magic flag still updates active, in this case fails
        //for this valid update.
        scheme = new SimpleWorkflowScheme().setId(id);
        scheme.setDefaultWorkflow(WF_THREE);

        response = client.updateWorkflowSchemeResponse(toRest.apply(scheme));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(id));

        control.discardDraftScheme(id);

        final WorkflowSchemeData duplicate = control.createScheme(new WorkflowSchemeData().setName(prefix + "Duplicate"));

        //Try noname.
        response = client.updateWorkflowSchemeResponse(toRest.apply(scheme.copy().setName(null)));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try a duplicate name.
        response = client.updateWorkflowSchemeResponse(toRest.apply(scheme.copy().setName(duplicate.getName())));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try badDefault.
        response = client.updateWorkflowSchemeResponse(toRest.apply(scheme.copy().setDefaultWorkflow(WF_BAD)));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try bad workflowMapping
        response = client.updateWorkflowSchemeResponse(toRest.apply(scheme.copy().setMapping(IssueType.IMPROVMENT, WF_BAD)));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Try a bad issue type.
        response = client.updateWorkflowSchemeResponse(toRest.apply(scheme.copy().setMapping(IssueType.BAD, WF_FOUR)));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Anonymous should not be able to hit the resource.
        response = client.anonymous().updateWorkflowSchemeResponse(toRest.apply(scheme));
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Non system admin should not have permission.
        response = client.loginAs("fred").updateWorkflowSchemeResponse(toRest.apply(scheme));
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testGetWorkflowMappings()
    {
        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testGetWorkflowMappings").setDescription("test Get Description");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());
        assertEquals(expectedScheme.getWorkflowMappings(), asWorkflowMap(client.getWorkflowMappings(scheme.getId(), false)));
        assertGetWorkflowMapping(expectedScheme, scheme.getId(), false);
        assertGetWorkflowMapping(expectedScheme, scheme.getId(), true);

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TGWM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Create a draft.
        WorkflowSchemeData draft = schemesControl.createDraft(scheme);

        draft.setDefaultWorkflow(null).setMapping(IssueType.IMPROVMENT.id, WF_FIVE)
                .setMapping(IssueType.NEW_FEATURE.id, WF_ONE)
                .setMapping(IssueType.BUG.id, WF_ONE);
        schemesControl.updateDraftScheme(scheme.getId(), draft);

        //Should return the orig scheme unless we pass true.
        assertEquals(expectedScheme.getWorkflowMappings(), asWorkflowMap(client.getWorkflowMappings(scheme.getId(), false)));
        assertGetWorkflowMapping(expectedScheme, scheme.getId(), false);

        //The draft should now be visible.
        WorkflowSchemeClient.WorkflowScheme actualDraft = client.getWorkflowScheme(scheme.getId(), true);
        assertEquals(ADMIN_USERNAME, actualDraft.getLastModifiedUser().name);
        assertNotNull(actualDraft.getLastModified());

        SimpleWorkflowScheme expectedDraft = asSimpleScheme(scheme.getId());
        assertEquals(expectedDraft.getWorkflowMappings(), asWorkflowMap(client.getWorkflowMappings(scheme.getId(), true)));
        assertGetWorkflowMapping(expectedDraft, scheme.getId(), true);

        //Draft is now gone again.
        schemesControl.discardDraftScheme(scheme.getId());

        assertEquals(expectedScheme.getWorkflowMappings(), asWorkflowMap(client.getWorkflowMappings(scheme.getId(), false)));
        assertEquals(expectedScheme.getWorkflowMappings(), asWorkflowMap(client.getWorkflowMappings(scheme.getId(), true)));
        assertGetWorkflowMapping(expectedScheme, scheme.getId(), false);
        assertGetWorkflowMapping(expectedScheme, scheme.getId(), true);

        //Test not found
        Response<?> response = client.getWorkflowMappingsResponse(2, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Test bad workflow.
        response = client.getWorkflowMappingResponse(scheme.getId(), WF_BAD, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().getWorkflowMappingsResponse(scheme.getId(), false);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").getWorkflowMappingsResponse(scheme.getId(), false);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testGetDraftWorkflowMappings()
    {
        client = client.asDraft();

        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testGetDraftWorkflowMappings").setDescription("testGetDraftWorkflowMappings");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        final String pkey = "TGDWM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        schemesControl.createDraft(scheme);

        WorkflowSchemeData draftScheme = new WorkflowSchemeData()
                .setDefaultWorkflow(WF_FIVE)
                .setMapping(IssueType.BUG.name, WF_THREE)
                .setMapping(IssueType.TASK.name, WF_FOUR)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        schemesControl.updateDraftScheme(scheme.getId(), draftScheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());
        assertEquals(expectedScheme.getWorkflowMappings(), asWorkflowMap(client.getWorkflowMappings(scheme.getId(), false)));
        assertGetWorkflowMapping(expectedScheme, scheme.getId(), false);

        //Draft is now gone again.
        schemesControl.discardDraftScheme(scheme.getId());

        Response<?> response = client.getWorkflowMappingsResponse(scheme.getId(), false);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Test not found
        response = client.getWorkflowMappingsResponse(2, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Test bad workflow.
        response = client.getWorkflowMappingResponse(scheme.getId(), WF_BAD, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().getWorkflowMappingsResponse(scheme.getId(), false);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").getWorkflowMappingsResponse(scheme.getId(), false);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testDeleteWorkflowMapping()
    {
        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testDeleteWorkflowMapping").setDescription("testDeleteWorkflowMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId()).removeWorkflow(WF_TWO);
        assertEquals(expectedScheme, asSimpleScheme(client.deleteWorkflowMapping(scheme.getId(), WF_TWO, false)));

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TDWM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Should not be able to update an active scheme.
        Response<WorkflowSchemeClient.WorkflowScheme> response = client.deleteWorkflowMappingResponse(scheme.getId(), WF_THREE, false);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Should be able to update scheme if we ask smartly.
        SimpleWorkflowScheme draftScheme = expectedScheme.copyAsDraft();
        response = client.deleteWorkflowMappingResponse(scheme.getId(), WF_THREE, true);
        assertEquals(OK.getStatusCode(), response.statusCode);
        updateDraftInfo(draftScheme, response.body, scheme.getId());
        assertEquals(draftScheme.removeWorkflow(WF_THREE), asSimpleScheme(response.body));

        //Delete the default.
        response = client.deleteWorkflowMappingResponse(scheme.getId(), WF_ONE, true);
        assertEquals(OK.getStatusCode(), response.statusCode);
        updateDraftInfo(draftScheme.removeWorkflow(WF_ONE), response.body, scheme.getId());
        assertEquals(draftScheme.copyWithDefault(), asSimpleScheme(response.body));

        //A no-op on the active scheme should work.
        response = client.deleteWorkflowMappingResponse(scheme.getId(), WF_FIVE, false);
        assertEquals(OK.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(response.body));

        //Test not found
        response = client.deleteWorkflowMappingResponse(2, WF_THREE, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Test bad workflow.
        response = client.deleteWorkflowMappingResponse(scheme.getId(), WF_BAD, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().deleteWorkflowMappingResponse(scheme.getId(), WF_TWO, false);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").deleteWorkflowMappingResponse(scheme.getId(), WF_TWO, false);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testDeleteDraftWorkflowMapping()
    {
        client = client.asDraft();

        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testDeleteDraftWorkflowMapping").setDescription("testDeleteDraftWorkflowMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        final String pkey = "TDDWM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        schemesControl.createDraft(scheme);

        WorkflowSchemeData draftScheme = new WorkflowSchemeData()
                .setDefaultWorkflow(WF_FIVE)
                .setMapping(IssueType.BUG.name, WF_THREE)
                .setMapping(IssueType.TASK.name, WF_FOUR)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        schemesControl.updateDraftScheme(scheme.getId(), draftScheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId()).removeWorkflow(WF_THREE);
        WorkflowSchemeClient.WorkflowScheme data = client.deleteWorkflowMapping(scheme.getId(), WF_THREE, false);
        updateDraftInfo(expectedScheme, data, scheme.getId());
        assertEquals(expectedScheme, asSimpleScheme(data));

        //Delete the default.
        Response<WorkflowSchemeClient.WorkflowScheme> response = client.deleteWorkflowMappingResponse(scheme.getId(), WF_FIVE, false);
        assertEquals(OK.getStatusCode(), response.statusCode);
        updateDraftInfo(expectedScheme.removeWorkflow(WF_FIVE), response.body, scheme.getId());
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(response.body));

        //A no-op is fine.
        response = client.deleteWorkflowMappingResponse(scheme.getId(), WF_FIVE, false);
        assertEquals(OK.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(response.body));

        //Test not found
        response = client.deleteWorkflowMappingResponse(2, WF_THREE, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Test bad workflow.
        response = client.deleteWorkflowMappingResponse(scheme.getId(), WF_BAD, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().deleteWorkflowMappingResponse(scheme.getId(), WF_TWO, false);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").deleteWorkflowMappingResponse(scheme.getId(), WF_TWO, false);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testUpdateWorkflowMapping()
    {
        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testUpdateWorkflowMapping").setDescription("testUpdateWorkflowMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());

        //Update an exiting WF.
        WorkflowSchemeClient.WorkflowMapping mapping = new WorkflowSchemeClient.WorkflowMapping(WF_TWO);
        mapping.setIssueTypes(Sets.newHashSet(IssueType.TASK.id));
        expectedScheme.removeMapping(IssueType.BUG);
        assertEquals(expectedScheme, asSimpleScheme(client.updateWorkflowMapping(scheme.getId(), mapping)));

        //Change the default.
        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_BAD);
        mapping.setDefaultMapping(true);
        expectedScheme.setDefaultWorkflow(WF_TWO);
        assertEquals(expectedScheme, asSimpleScheme(client.updateWorkflowMapping(scheme.getId(), WF_TWO, mapping)));

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TUWM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Should be possible to do a no-op on an active scheme.
        mapping.setWorkflow(WF_TWO);
        assertEquals(expectedScheme, asSimpleScheme(client.updateWorkflowMapping(scheme.getId(), mapping)));

        //Should not be possible to mutate an active scheme.
        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_FIVE);
        mapping.setDefaultMapping(false);
        mapping.setIssueTypes(Sets.newHashSet(IssueType.BUG.id, IssueType.IMPROVMENT.id));

        Response<WorkflowSchemeClient.WorkflowScheme> response = client.updateWorkflowMappingResponse(scheme.getId(), mapping);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Should create the draft when this flag is set.
        mapping.setUpdateDraftIfNeeded(true);
        SimpleWorkflowScheme expectedDraft = expectedScheme.copyAsDraft();
        expectedDraft.setMapping(IssueType.BUG, WF_FIVE);
        expectedDraft.setMapping(IssueType.IMPROVMENT, WF_FIVE);
        WorkflowSchemeClient.WorkflowScheme actualDraft = client.updateWorkflowMapping(scheme.getId(), mapping);
        updateDraftInfo(expectedDraft, actualDraft, scheme.getId());
        assertEquals(expectedDraft, asSimpleScheme(actualDraft));

        //Should update the draft. In this case we are removing WF_FIVE
        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_BAD);
        mapping.setUpdateDraftIfNeeded(true);
        mapping.setIssueTypes(Sets.<String>newHashSet());
        expectedDraft.removeWorkflow(WF_FIVE);
        actualDraft = client.updateWorkflowMapping(scheme.getId(), WF_FIVE, mapping);
        updateDraftInfo(expectedDraft, actualDraft, scheme.getId());
        assertEquals(expectedDraft, asSimpleScheme(actualDraft));

        //Can we change the default without changing the issue types?
        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_FOUR);
        mapping.setDefaultMapping(true);
        mapping.setUpdateDraftIfNeeded(true);
        expectedDraft.setDefaultWorkflow(WF_FOUR);
        actualDraft = client.updateWorkflowMapping(scheme.getId(), mapping);
        updateDraftInfo(expectedDraft, actualDraft, scheme.getId());
        assertEquals(expectedDraft, asSimpleScheme(actualDraft));

        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_ONE);
        mapping.setDefaultMapping(true);

        //JRADEV-15521
        //Can we change the issue types of the default without removing the default.
        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_FOUR);
        mapping.setIssueTypes(Sets.newHashSet(IssueType.BUG.id));
        mapping.setUpdateDraftIfNeeded(true);
        expectedDraft.setMapping(IssueType.BUG, WF_FOUR);
        actualDraft = client.updateWorkflowMapping(scheme.getId(), mapping);
        updateDraftInfo(expectedDraft, actualDraft, scheme.getId());
        assertEquals(expectedDraft, asSimpleScheme(actualDraft));

        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_ONE);
        mapping.setDefaultMapping(true);

        //Test not found
        response = client.updateWorkflowMappingResponse(2, mapping);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_BAD);
        mapping.setDefaultMapping(true);

        //Test bad workflow.
        response = client.updateWorkflowMappingResponse(scheme.getId(), mapping);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_FOUR);
        mapping.setIssueTypes(Sets.newHashSet(IssueType.BAD.id));
        mapping.setDefaultMapping(true);

        //Try a bad issue type.
        response = client.updateWorkflowMappingResponse(scheme.getId(), mapping);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().updateWorkflowMappingResponse(scheme.getId(), mapping);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").updateWorkflowMappingResponse(scheme.getId(), mapping);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testUpdateDraftWorkflowMapping()
    {
        client = client.asDraft();

        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testUpdateDraftWorkflowMapping").setDescription("testUpdateDraftWorkflowMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        final String pkey = "TUDWM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        schemesControl.createDraft(scheme);

        WorkflowSchemeData draftScheme = new WorkflowSchemeData()
                .setDefaultWorkflow(WF_FIVE)
                .setMapping(IssueType.BUG.name, WF_THREE)
                .setMapping(IssueType.TASK.name, WF_FOUR)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        schemesControl.updateDraftScheme(scheme.getId(), draftScheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());

        //Update an exiting WF.
        WorkflowSchemeClient.WorkflowMapping mapping = new WorkflowSchemeClient.WorkflowMapping(WF_THREE);
        mapping.setIssueTypes(Sets.newHashSet(IssueType.TASK.id));
        expectedScheme.removeMapping(IssueType.BUG).removeMapping(IssueType.NEW_FEATURE).setMapping(IssueType.TASK, WF_THREE);
        WorkflowSchemeClient.WorkflowScheme actualData = client.updateWorkflowMapping(scheme.getId(), mapping);
        updateDraftInfo(expectedScheme, actualData, scheme.getId());
        assertEquals(expectedScheme, asSimpleScheme(actualData));

        //Change the default.
        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_TWO);
        mapping.setDefaultMapping(true);
        expectedScheme.setDefaultWorkflow(WF_TWO);
        actualData = client.updateWorkflowMapping(scheme.getId(), mapping);
        updateDraftInfo(expectedScheme, actualData, scheme.getId());
        assertEquals(expectedScheme, asSimpleScheme(actualData));

        //Remove the default and also add some issue types.
        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_BAD);
        mapping.setDefaultMapping(false);
        mapping.setIssueTypes(Sets.newHashSet(IssueType.IMPROVMENT.id, IssueType.NEW_FEATURE.id));
        expectedScheme.setDefaultWorkflow(null)
                .setMapping(IssueType.IMPROVMENT, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE, WF_TWO);

        actualData = client.updateWorkflowMapping(scheme.getId(), WF_TWO, mapping);
        updateDraftInfo(expectedScheme, actualData, scheme.getId());
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(actualData));

        //Set the default and also add some issue types.
        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_THREE);
        mapping.setDefaultMapping(true);
        mapping.setIssueTypes(Sets.newHashSet(IssueType.IMPROVMENT.id));
        expectedScheme.setDefaultWorkflow(WF_THREE)
                .removeMapping(IssueType.TASK)
                .setMapping(IssueType.IMPROVMENT, WF_THREE);

        actualData = client.updateWorkflowMapping(scheme.getId(), mapping);
        updateDraftInfo(expectedScheme, actualData, scheme.getId());
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(actualData));

        //JRADEV-15515
        //Can we change the issue types of the default without removing the default.
        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_FOUR);
        mapping.setIssueTypes(Sets.newHashSet(IssueType.BUG.id));
        mapping.setUpdateDraftIfNeeded(true);
        expectedScheme.setMapping(IssueType.BUG, WF_FOUR);
        actualData = client.updateWorkflowMapping(scheme.getId(), mapping);
        updateDraftInfo(expectedScheme, actualData, scheme.getId());
        assertEquals(expectedScheme, asSimpleScheme(actualData));

        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_ONE);
        mapping.setDefaultMapping(true);

        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_ONE);
        mapping.setDefaultMapping(true);

        //Test not found
        Response<?> response = client.updateWorkflowMappingResponse(2, mapping);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_BAD);
        mapping.setDefaultMapping(true);

        //Test bad workflow.
        response = client.updateWorkflowMappingResponse(scheme.getId(), mapping);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        mapping = new WorkflowSchemeClient.WorkflowMapping(WF_FOUR);
        mapping.setIssueTypes(Sets.newHashSet(IssueType.BAD.id));
        mapping.setDefaultMapping(true);

        //Try a bad issue type.
        response = client.updateWorkflowMappingResponse(scheme.getId(), mapping);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().updateWorkflowMappingResponse(scheme.getId(), mapping);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").updateWorkflowMappingResponse(scheme.getId(), mapping);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testGetIssueTypeMapping()
    {
        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testGetIssueTypeMapping").setDescription("testGetIssueTypeMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());
        assertIssueTypeMapping(expectedScheme, scheme.getId(), false);
        assertIssueTypeMapping(expectedScheme, scheme.getId(), true);

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TGITM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Create a draft.
        WorkflowSchemeData draft = schemesControl.createDraft(scheme);

        draft.setDefaultWorkflow(null).setMapping(IssueType.IMPROVMENT.id, WF_FIVE)
                .setMapping(IssueType.NEW_FEATURE.id, WF_ONE)
                .setMapping(IssueType.BUG.id, WF_ONE);
        schemesControl.updateDraftScheme(scheme.getId(), draft);

        //Should return the orig scheme unless we pass true.
        assertIssueTypeMapping(expectedScheme, scheme.getId(), false);

        //The draft should now be visible.
        WorkflowSchemeClient.WorkflowScheme actualDraft = client.getWorkflowScheme(scheme.getId(), true);
        assertEquals(ADMIN_USERNAME, actualDraft.getLastModifiedUser().name);
        assertNotNull(actualDraft.getLastModified());

        SimpleWorkflowScheme expectedDraft = asSimpleScheme(scheme.getId());
        assertIssueTypeMapping(expectedDraft, scheme.getId(), true);

        //Draft is now gone again.
        schemesControl.discardDraftScheme(scheme.getId());

        assertIssueTypeMapping(expectedScheme, scheme.getId(), false);
        assertIssueTypeMapping(expectedScheme, scheme.getId(), true);

        //Test not found
        Response<?> response = client.getIssueTypeMappingResponse(2, IssueType.BAD.getId(), true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Test bad issuetype.
        response = client.getIssueTypeMappingResponse(scheme.getId(), IssueType.BAD.getId(), true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().getIssueTypeMappingResponse(scheme.getId(), IssueType.BUG.getId(), false);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").getIssueTypeMappingResponse(scheme.getId(), IssueType.BUG.getId(), false);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testGetDraftIssueTypeMapping()
    {
        client = client.asDraft();

        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testGetDraftIssueTypeMapping").setDescription("testGetDraftIssueTypeMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TGDITM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Create a draft.
        WorkflowSchemeData draft = schemesControl.createDraft(scheme);

        draft.setDefaultWorkflow(null).setMapping(IssueType.IMPROVMENT.id, WF_FIVE)
                .setMapping(IssueType.NEW_FEATURE.id, WF_ONE)
                .setMapping(IssueType.BUG.id, WF_ONE);
        schemesControl.updateDraftScheme(scheme.getId(), draft);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());
        assertIssueTypeMapping(expectedScheme, scheme.getId(), false);

        //Test not found
        Response<?> response = client.getIssueTypeMappingResponse(2, IssueType.BAD.getId(), true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Test bad issuetype.
        response = client.getIssueTypeMappingResponse(scheme.getId(), IssueType.BAD.getId(), true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().getIssueTypeMappingResponse(scheme.getId(), IssueType.BUG.getId(), false);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").getIssueTypeMappingResponse(scheme.getId(), IssueType.BUG.getId(), false);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testDeleteIssueTypeMapping()
    {
        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testDeleteIssueTypeMapping").setDescription("testDeleteIssueTypeMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId()).removeMapping(IssueType.BUG);
        assertEquals(expectedScheme, asSimpleScheme(client.deleteIssueMapping(scheme.getId(), IssueType.BUG.id, false)));
        assertEquals(expectedScheme, asSimpleScheme(client.deleteIssueMapping(scheme.getId(), IssueType.BUG.id, false)));

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TDITM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Should not be able to update an active scheme.
        Response<WorkflowSchemeClient.WorkflowScheme> response = client.deleteIssueMappingResponse(scheme.getId(), IssueType.TASK.id, false);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Should be able to update scheme if we ask smartly.
        SimpleWorkflowScheme draftScheme = expectedScheme.copyAsDraft();
        response = client.deleteIssueMappingResponse(scheme.getId(), IssueType.TASK.id, true);
        assertEquals(OK.getStatusCode(), response.statusCode);
        updateDraftInfo(draftScheme, response.body, scheme.getId());
        assertEquals(draftScheme.removeMapping(IssueType.TASK), asSimpleScheme(response.body));

        //A no-op on the active scheme should work.
        response = client.deleteIssueMappingResponse(scheme.getId(), IssueType.IMPROVMENT.id, false);
        assertEquals(OK.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme, asSimpleScheme(response.body));

        //Test not found
        response = client.deleteIssueMappingResponse(2, IssueType.NEW_FEATURE.id, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Test bad issue type.
        response = client.deleteIssueMappingResponse(scheme.getId(), IssueType.BAD.id, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().deleteIssueMappingResponse(scheme.getId(), IssueType.BUG.id, false);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").deleteIssueMappingResponse(scheme.getId(), IssueType.BUG.id, false);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testDeleteDraftIssueTypeMapping()
    {
        client = client.asDraft();

        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testDeleteDraftIssueTypeMapping").setDescription("testDeleteDraftIssueTypeMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        Response<WorkflowSchemeClient.WorkflowScheme> response = client.deleteIssueMappingResponse(scheme.getId(), IssueType.NEW_FEATURE.id, false);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TDDITM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Create a draft.
        WorkflowSchemeData draft = schemesControl.createDraft(scheme);

        draft.setDefaultWorkflow(null).setMapping(IssueType.IMPROVMENT.id, WF_FIVE)
                .setMapping(IssueType.NEW_FEATURE.id, WF_ONE)
                .setMapping(IssueType.BUG.id, WF_ONE);
        schemesControl.updateDraftScheme(scheme.getId(), draft);

        final SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());

        //Simple remove should work.
        response = client.deleteIssueMappingResponse(scheme.getId(), IssueType.IMPROVMENT.id, false);
        expectedScheme.removeMapping(IssueType.IMPROVMENT);
        assertEquals(OK.getStatusCode(), response.statusCode);
        updateDraftInfo(expectedScheme, response.body, scheme.getId());
        assertEquals(expectedScheme, asSimpleScheme(response.body));

        //Removing a no-op should work.
        response = client.deleteIssueMappingResponse(scheme.getId(), IssueType.IMPROVMENT.id, false);
        assertEquals(OK.getStatusCode(), response.statusCode);
        assertEquals(expectedScheme, asSimpleScheme(response.body));

        //Test not found
        response = client.deleteIssueMappingResponse(2, IssueType.NEW_FEATURE.id, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Test bad issue type.
        response = client.deleteIssueMappingResponse(scheme.getId(), IssueType.BAD.id, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().deleteIssueMappingResponse(scheme.getId(), IssueType.BUG.id, false);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").deleteIssueMappingResponse(scheme.getId(), IssueType.BUG.id, false);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testUpdateIssueTypeMapping()
    {
        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testUpdateIssueTypeMapping").setDescription("testUpdateIssueTypeMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());

        //Update an exiting WF.
        WorkflowSchemeClient.IssueTypeMappingBean mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.BUG.id, WF_ONE);
        expectedScheme.setMapping(IssueType.BUG, WF_ONE);
        assertEquals(expectedScheme, asSimpleScheme(client.updateIssueTypeMapping(scheme.getId(), mapping)));

        //Remove add a new mapping.
        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.BAD.id, WF_THREE);
        expectedScheme.setMapping(IssueType.IMPROVMENT, WF_THREE);
        assertEquals(expectedScheme, asSimpleScheme(client.updateIssueTypeMapping(scheme.getId(), IssueType.IMPROVMENT.id, mapping)));

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TUITM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Should be possible to do a no-op on an active scheme.
        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.IMPROVMENT.id, WF_THREE);
        assertEquals(expectedScheme, asSimpleScheme(client.updateIssueTypeMapping(scheme.getId(), mapping)));

        //Should not be possible to mutate an active scheme.
        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.IMPROVMENT.id, WF_FOUR);
        Response<WorkflowSchemeClient.WorkflowScheme> response = client.updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Should create the draft when this flag is set.
        mapping.setUpdateDraftIfNeeded(true);
        SimpleWorkflowScheme expectedDraft = expectedScheme.copyAsDraft();
        expectedDraft.setMapping(IssueType.IMPROVMENT, WF_FOUR);
        WorkflowSchemeClient.WorkflowScheme actualDraft = client.updateIssueTypeMapping(scheme.getId(), mapping);
        updateDraftInfo(expectedDraft, actualDraft, scheme.getId());
        assertEquals(expectedDraft, asSimpleScheme(actualDraft));

        //Test not found
        response = client.updateIssueTypeMappingResponse(2, mapping);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.BAD.id, WF_FOUR);
        mapping.setUpdateDraftIfNeeded(true);

        //Test bad issue type.
        response = client.updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.BUG.id, WF_BAD);
        mapping.setUpdateDraftIfNeeded(true);

        //Test bad workflow.
        response = client.updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.IMPROVMENT.id, WF_THREE);
        mapping.setUpdateDraftIfNeeded(true);

        //Test anonymous
        response = client.anonymous().updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testUpdateDraftIssueTypeMapping()
    {
        client = client.asDraft();

        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testUpdateDraftIssueTypeMapping").setDescription("testUpdateDraftIssueTypeMapping");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        WorkflowSchemesControl schemesControl = backdoor.workflowSchemes();
        scheme = schemesControl.createScheme(scheme);

        WorkflowSchemeClient.IssueTypeMappingBean mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.BUG.id, WF_THREE);
        Response<WorkflowSchemeClient.WorkflowScheme> response = client.updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TUDITM";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //Create a draft.
        WorkflowSchemeData draft = schemesControl.createDraft(scheme);

        draft.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);
        schemesControl.updateDraftScheme(scheme.getId(), draft);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());

        //Update an exiting WF.
        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.BUG.id, WF_ONE);
        expectedScheme.setMapping(IssueType.BUG, WF_ONE);
        WorkflowSchemeClient.WorkflowScheme actualScheme = client.updateIssueTypeMapping(scheme.getId(), mapping);
        updateDraftInfo(expectedScheme, actualScheme, scheme.getId());
        assertEquals(expectedScheme, asSimpleScheme(actualScheme));

        //Add a new mapping.
        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.BAD.id, WF_THREE);
        expectedScheme.setMapping(IssueType.IMPROVMENT, WF_THREE);
        actualScheme = client.updateIssueTypeMapping(scheme.getId(), IssueType.IMPROVMENT.id, mapping);
        updateDraftInfo(expectedScheme, actualScheme, scheme.getId());
        assertEquals(expectedScheme, asSimpleScheme(actualScheme));

        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.IMPROVMENT.id, WF_THREE);
        //Should be possible to do a no-op.
        assertEquals(expectedScheme, asSimpleScheme(client.updateIssueTypeMapping(scheme.getId(), mapping)));

        //Test not found
        response = client.updateIssueTypeMappingResponse(2, mapping);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        assertNull(response.body);

        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.BAD.id, WF_FOUR);
        mapping.setUpdateDraftIfNeeded(true);

        //Test bad issue type.
        response = client.updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.BUG.id, WF_BAD);
        mapping.setUpdateDraftIfNeeded(true);

        //Test bad workflow.
        response = client.updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        mapping = new WorkflowSchemeClient.IssueTypeMappingBean(IssueType.IMPROVMENT.id, WF_THREE);
        mapping.setUpdateDraftIfNeeded(true);

        //Test anonymous
        response = client.anonymous().updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").updateIssueTypeMappingResponse(scheme.getId(), mapping);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testDefaultResource()
    {
        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testDefaultResource").setDescription("testDefaultResource");
        scheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        scheme = backdoor.workflowSchemes().createScheme(scheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), true));

        //Set the default to null.
        expectedScheme.removeDefault();
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(client.deleteDefault(scheme.getId(), false)));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), true));

        //Set the default to something again.
        expectedScheme.setDefaultWorkflow(WF_FIVE);
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(client.setDefault(scheme.getId(), WF_FIVE, false)));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), true));

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TDR";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        //This is a noop and as such should be okay.
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(client.setDefault(scheme.getId(), WF_FIVE, false)));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), true));

        //Should not be able to change an active scheme.
        Response<?> response = client.setDefaultResponse(scheme.getId(), WF_FOUR, false);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), true));

        //Should be able to change the scheme if we specify correctly.
        WorkflowSchemeClient.WorkflowScheme actualScheme = client.setDefault(scheme.getId(), WF_FOUR, true);

        SimpleWorkflowScheme expectedDraft = expectedScheme.copyAsDraft().setDefaultWorkflow(WF_FOUR);
        updateDraftInfo(expectedDraft, actualScheme, scheme.getId());

        assertEquals(expectedDraft.copyWithDefault(), asSimpleScheme(actualScheme));
        expectedDraft.assertDefault(client.getDefault(scheme.getId(), true));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));

        backdoor.workflowSchemes().discardDraftScheme(scheme.getId());

        //Should not be able to change an active scheme.
        response = client.deleteDefaultResponse(scheme.getId(), false);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), true));

        //Should be able to change the scheme if we specify correctly.
        actualScheme = client.deleteDefault(scheme.getId(), true);
        updateDraftInfo(expectedDraft.removeDefault(), actualScheme, scheme.getId());

        assertEquals(expectedDraft.copyWithDefault(), asSimpleScheme(actualScheme));
        expectedDraft.assertDefault(client.getDefault(scheme.getId(), true));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));

        backdoor.workflowSchemes().discardDraftScheme(scheme.getId());

        //The draft should be been deleted.
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), true));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));

        //Test bad scheme
        response = client.deleteDefaultResponse(-1, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        response = client.getDefaultResponse(-1, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        response = client.setDefaultResponse(-1, WF_FIVE, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test bad workflow.
        response = client.setDefaultResponse(scheme.getId(), WF_BAD, true);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().deleteDefaultResponse(scheme.getId(), true);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        response = client.getDefaultResponse(scheme.getId(), true);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        response = client.setDefaultResponse(scheme.getId(), WF_FIVE, true);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").getDefaultResponse(scheme.getId(), true);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);

        response = client.getDefaultResponse(scheme.getId(), true);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);

        response = client.setDefaultResponse(scheme.getId(), WF_FIVE, true);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testDefaultDraftResource()
    {
        client = client.asDraft();

        WorkflowSchemeData scheme = new WorkflowSchemeData();
        scheme.setName("testDefaulDrafttResource").setDescription("testDefaulDrafttResource");

        scheme = backdoor.workflowSchemes().createScheme(scheme);

        //Make sure we can't delete the scheme of an active project.
        final String pkey = "TDDR";
        backdoor.project().addProject(pkey, pkey, "admin");
        backdoor.project().setWorkflowScheme(pkey, scheme.getId());

        WorkflowSchemeData draftScheme = new WorkflowSchemeData();
        draftScheme.setDefaultWorkflow(WF_ONE)
                .setMapping(IssueType.BUG.name, WF_TWO)
                .setMapping(IssueType.TASK.name, WF_TWO)
                .setMapping(IssueType.NEW_FEATURE.name, WF_THREE);

        backdoor.workflowSchemes().createDraft(scheme);
        backdoor.workflowSchemes().updateDraftScheme(scheme.getId(), draftScheme);

        SimpleWorkflowScheme expectedScheme = asSimpleScheme(scheme.getId());
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));

        //Set the default to null.
        expectedScheme.removeDefault();
        WorkflowSchemeClient.WorkflowScheme actualScheme = client.deleteDefault(scheme.getId(), false);
        updateDraftInfo(expectedScheme, actualScheme, scheme.getId());
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(actualScheme));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));

        //Set the default to something again.
        expectedScheme.setDefaultWorkflow(WF_FIVE);
        actualScheme = client.setDefault(scheme.getId(), WF_FIVE, false);
        updateDraftInfo(expectedScheme, actualScheme, scheme.getId());
        assertEquals(expectedScheme.copyWithDefault(), asSimpleScheme(actualScheme));
        expectedScheme.assertDefault(client.getDefault(scheme.getId(), false));

        //Test bad scheme
        Response<?> response = client.deleteDefaultResponse(-1, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        response = client.getDefaultResponse(-1, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        response = client.setDefaultResponse(-1, WF_FIVE, true);
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);

        //Test bad workflow.
        response = client.setDefaultResponse(scheme.getId(), WF_BAD, true);
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);

        //Test anonymous
        response = client.anonymous().deleteDefaultResponse(scheme.getId(), true);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        response = client.getDefaultResponse(scheme.getId(), true);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        response = client.setDefaultResponse(scheme.getId(), WF_FIVE, true);
        assertEquals(UNAUTHORIZED.getStatusCode(), response.statusCode);

        //Test no permission.
        response = client.loginAs("fred").getDefaultResponse(scheme.getId(), true);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);

        response = client.getDefaultResponse(scheme.getId(), true);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);

        response = client.setDefaultResponse(scheme.getId(), WF_FIVE, true);
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    private void assertGetWorkflowMapping(SimpleWorkflowScheme expectedScheme, Long schemeId, boolean draft)
    {
        for (String wf : WORKFLOWS)
        {
            assertEquals(expectedScheme.getMapping(wf), asWorkflowMap(client.getWorkflowMapping(schemeId, wf, draft)));
        }
    }

    private void assertIssueTypeMapping(SimpleWorkflowScheme expectedScheme, Long schemeId, boolean draft)
    {
        for (IssueType type : IssueType.ALL)
        {
            expectedScheme.assertIssueTypeMapping(client.getIssueTypeMapping(schemeId, type.id, draft));
        }
    }

    private void assertSchemeGone(Long id)
    {
        try
        {
            backdoor.workflowSchemes().getWorkflowScheme(id);
            fail("The scheme should have been deleted.");
        }
        catch (UniformInterfaceException e)
        {
            assertEquals(NOT_FOUND.getStatusCode(), e.getResponse().getStatus());
        }
    }

    private void assertDraftDoesNotExist(Long id)
    {
        assertNull("The draft scheme should not be there.",
                backdoor.workflowSchemes().getWorkflowSchemeForParentNullIfNotFound(id));

        try
        {
            backdoor.workflowSchemes().getWorkflowScheme(id);
        }
        catch (UniformInterfaceException e)
        {
            fail("Deleting the draft should not have deleted the parent.");
        }
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        RESTORE.run(new Runnable()
        {
            @Override
            public void run()
            {
                backdoor.restoreDataFromResource("TestWorkflowSchemeResource.xml");
            }
        });
        client = new WorkflowSchemeClient(getEnvironmentData());
    }

    private Map<IssueType, String> asWorkflowMap(Iterable<WorkflowSchemeClient.WorkflowMapping> mappings)
    {
        Map<IssueType, String> map = Maps.newHashMap();
        for (WorkflowSchemeClient.WorkflowMapping mapping : mappings)
        {
            for (String issueType : mapping.getIssueTypes())
            {
                if (map.put(IssueType.findById(issueType), mapping.getWorkflow()) != null)
                {
                    throw new RuntimeException("Issue Type '" + issueType + "' mapped twice.");
                }
            }
        }
        return map;
    }

    private Map<IssueType, String> asWorkflowMap(WorkflowSchemeClient.WorkflowMapping mapping)
    {
        Map<IssueType, String> map = Maps.newHashMap();
        for (String issueType : mapping.getIssueTypes())
        {
            if (map.put(IssueType.findById(issueType), mapping.getWorkflow()) != null)
            {
                throw new RuntimeException("Issue Type '" + issueType + "' mapped twice.");
            }
        }
        return map;
    }

    private SimpleWorkflowScheme asSimpleScheme(long id)
    {
        return asSimpleScheme(id, true);
    }

    private SimpleWorkflowScheme updateDraftInfo(SimpleWorkflowScheme scheme, WorkflowSchemeClient.WorkflowScheme data, long parentId)
    {
        assertNotNull(data.getLastModified());
        assertNotNull(data.getLastModifiedUser());

        scheme.setId(data.getId());
        scheme.setLastModified(data.getLastModified());
        scheme.setLastModifiedUser(data.getLastModifiedUser().name);
        scheme.setSelf(selfUri(parentId, true));

        return scheme;
    }

    private SimpleWorkflowScheme asSimpleScheme(long id, boolean lookForDraft)
    {
        final WorkflowSchemeData scheme = backdoor.workflowSchemes().getWorkflowScheme(id);
        final WorkflowSchemeData draftScheme = lookForDraft ? backdoor.workflowSchemes().getWorkflowSchemeForParentNullIfNotFound(id) : null;

        final WorkflowSchemeData data = draftScheme == null ? scheme : draftScheme;

        SimpleWorkflowScheme bean = new SimpleWorkflowScheme();
        bean.setId(data.getId());
        bean.setName(data.getName());
        bean.setDescription(data.getDescription());
        bean.setDraft(data.isDraft());
        bean.setSelf(selfUri(scheme.getId(), data.isDraft()));
        bean.setDefaultWorkflow(data.getDefaultWorkflow() == null ? "jira" : data.getDefaultWorkflow());
        bean.setWorkflowMappings(convertMappingsByName(data.getMappings()));

        if (draftScheme != null)
        {
            bean.setLastModified(draftScheme.getLastModified());
            bean.setLastModifiedUser(draftScheme.getLastModifiedUser());
            bean.setOriginalWorkflowMappings(convertMappingsByName(scheme.getMappings()));
            bean.setOriginalDefaultWorkflow(scheme.getDefaultWorkflow());
        }

        return bean;
    }

    private SimpleWorkflowScheme asSimpleScheme(WorkflowSchemeClient.WorkflowScheme data)
    {
        SimpleWorkflowScheme bean = new SimpleWorkflowScheme();
        bean.setId(data.getId());
        bean.setName(data.getName());
        bean.setDescription(data.getDescription());
        bean.setDraft(data.isDraft());
        bean.setSelf(data.getSelf());
        bean.setDefaultWorkflow(data.getDefaultWorkflow());
        bean.setOriginalDefaultWorkflow(data.getOriginalDefaultWorkflow());
        bean.setLastModified(data.getLastModified());

        UserBean lastModifiedUser = data.getLastModifiedUser();
        if (lastModifiedUser != null)
        {
            bean.setLastModifiedUser(lastModifiedUser.name);
        }

        bean.setWorkflowMappings(convertMappingsById(data.getIssueTypeMappings()));
        bean.setOriginalWorkflowMappings(convertMappingsById(data.getOriginalIssueTypeMappings()));
        return bean;
    }

    private static Map<IssueType, String> convertMappingsByName(Map<String, String> dataMappings)
    {
        if (dataMappings == null)
        {
            return null;
        }

        Map<IssueType, String> mappings = Maps.newHashMap();
        for (Map.Entry<String, String> entry : dataMappings.entrySet())
        {
            String issueType = entry.getKey();
            String workflow = entry.getValue();
            mappings.put(IssueType.findByName(issueType), workflow);
        }
        return mappings;
    }

    private static Map<IssueType, String> convertMappingsById(Map<String, String> dataMappings)
    {
        if (dataMappings == null)
        {
            return null;
        }

        Map<IssueType, String> mappings = Maps.newHashMap();
        for (Map.Entry<String, String> entry : dataMappings.entrySet())
        {
            String issueType = entry.getKey();
            String workflow = entry.getValue();
            mappings.put(IssueType.findById(issueType), workflow);
        }
        return mappings;
    }

    private URI selfUri(Long id, boolean draft)
    {
        ArrayList<String> paths = Lists.newArrayList("workflowscheme", id.toString());
        if (draft)
        {
            paths.add("draft");
        }
        return getRestApiUri(paths);
    }

    private static Map<String, WorkflowSchemeClient.WorkflowMapping> workflowCollectionToMap(Collection<WorkflowSchemeClient.WorkflowMapping> workflowMapping)
    {
        return Maps.uniqueIndex(workflowMapping, new Function<WorkflowSchemeClient.WorkflowMapping, String>() {
            @Override
            public String apply(WorkflowSchemeClient.WorkflowMapping input)
            {
                return input.getWorkflow();
            }
        });
    }

    private static class SimpleWorkflowScheme
    {
        private Long id;
        private String name;
        private String description;
        private String defaultWorkflow;
        private Map<IssueType, String> workflowMappings;
        private String originalDefaultWorkflow;
        private Map<IssueType, String> originalWorkflowMappings;
        private boolean draft;
        private String lastModifiedUser;
        private String lastModified;
        private URI self;

        public SimpleWorkflowScheme()
        {
        }

        public SimpleWorkflowScheme(SimpleWorkflowScheme other)
        {
            this.id = other.id;
            this.name = other.name;
            this.description = other.description;
            this.workflowMappings = copyMap(other.workflowMappings);
            this.originalWorkflowMappings = copyMap(other.originalWorkflowMappings);
            this.draft = other.draft;
            this.lastModified = other.lastModified;
            this.lastModifiedUser = other.lastModifiedUser;
            this.self = other.self;
            this.defaultWorkflow = other.defaultWorkflow;
            this.originalDefaultWorkflow = other.originalDefaultWorkflow;
        }

        private static <K, V> HashMap<K, V> copyMap(Map<K, V> other)
        {
            return other != null ? Maps.newHashMap(other) : null;
        }

        public SimpleWorkflowScheme copy()
        {
            return new SimpleWorkflowScheme(this);
        }

        public Long getId()
        {
            return id;
        }

        public SimpleWorkflowScheme setId(Long id)
        {
            this.id = id;
            return this;
        }

        public String getName()
        {
            return name;
        }

        public SimpleWorkflowScheme setName(String name)
        {
            this.name = name;
            return this;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getDefaultWorkflow()
        {
            return defaultWorkflow;
        }

        public SimpleWorkflowScheme setDefaultWorkflow(String defaultWorkflow)
        {
            this.defaultWorkflow = defaultWorkflow;
            return this;
        }

        public Map<IssueType, String> getWorkflowMappings()
        {
            return workflowMappings;
        }

        public SimpleWorkflowScheme setWorkflowMappings(Map<IssueType, String> workflowMappings)
        {
            this.workflowMappings = workflowMappings;
            return this;
        }

        public Map<IssueType, String> getOriginalWorkflowMappings()
        {
            return originalWorkflowMappings;
        }

        public void setOriginalWorkflowMappings(Map<IssueType, String> originalWorkflowMappings)
        {
            this.originalWorkflowMappings = originalWorkflowMappings;
        }

        public boolean isDraft()
        {
            return draft;
        }

        public void setDraft(boolean draft)
        {
            this.draft = draft;
        }

        public String getLastModifiedUser()
        {
            return lastModifiedUser;
        }

        public void setLastModifiedUser(String lastModifiedUser)
        {
            this.lastModifiedUser = lastModifiedUser;
        }

        public String getLastModified()
        {
            return lastModified;
        }

        public void setLastModified(String lastModified)
        {
            this.lastModified = lastModified;
        }

        public String getOriginalDefaultWorkflow()
        {
            return originalDefaultWorkflow;
        }

        public void setOriginalDefaultWorkflow(String originalDefaultWorkflow)
        {
            this.originalDefaultWorkflow = originalDefaultWorkflow;
        }

        public URI getSelf()
        {
            return self;
        }

        public SimpleWorkflowScheme setSelf(URI self)
        {
            this.self = self;
            return this;
        }

        public SimpleWorkflowScheme clearMappings()
        {
            this.workflowMappings = null;
            return this;
        }

        public SimpleWorkflowScheme setMapping(IssueType issueType, String workflow)
        {
            if (workflowMappings == null)
            {
                workflowMappings = Maps.newHashMap();
            }
            workflowMappings.put(issueType, workflow);
            return this;
        }

        public SimpleWorkflowScheme removeMapping(IssueType issueType)
        {
            if (workflowMappings != null)
            {
                workflowMappings.remove(issueType);
            }
            return this;
        }

        public SimpleWorkflowScheme setOriginalMapping(IssueType issueType, String workflow)
        {
            if (originalWorkflowMappings == null)
            {
                originalWorkflowMappings = Maps.newHashMap();
            }
            originalWorkflowMappings.put(issueType, workflow);
            return this;
        }

        public SimpleWorkflowScheme removeDefault()
        {
            this.defaultWorkflow = null;
            return this;
        }

        public SimpleWorkflowScheme copyAsDraft()
        {
            SimpleWorkflowScheme scheme = copy();
            scheme.setDraft(true);
            scheme.setOriginalWorkflowMappings(getWorkflowMappings() == null ? null : Maps.newHashMap(getWorkflowMappings()));
            scheme.setOriginalDefaultWorkflow(getDefaultWorkflow());

            return scheme;
        }

        public WorkflowSchemeClient.WorkflowScheme asRestBean()
        {
            WorkflowSchemeClient.WorkflowScheme scheme = new WorkflowSchemeClient.WorkflowScheme();
            scheme.setName(getName());
            scheme.setDescription(getDescription());
            scheme.setDefaultWorkflow(getDefaultWorkflow());
            scheme.setId(getId());

            if (workflowMappings != null)
            {
                Map<String, String> issueTypes = Maps.newHashMap();
                for (Map.Entry<IssueType, String> entry : workflowMappings.entrySet())
                {
                    IssueType type = entry.getKey();
                    String workflow = entry.getValue();
                    issueTypes.put(type.getId(), workflow);
                }
                scheme.setIssueTypeMappings(issueTypes);
            }
            return scheme;
        }

        public WorkflowSchemeData asBackdoorBean()
        {
            WorkflowSchemeData scheme = new WorkflowSchemeData();
            scheme.setName(getName());
            scheme.setDescription(getDescription());
            scheme.setDefaultWorkflow(getDefaultWorkflow());
            scheme.setId(getId());

            if (workflowMappings != null)
            {
                Map<String, String> mappings = Maps.newHashMap();
                for (Map.Entry<IssueType, String> entry : workflowMappings.entrySet())
                {
                    IssueType type = entry.getKey();
                    String workflow = entry.getValue();
                    mappings.put(type.getId(), workflow);
                }
                scheme.setMappings(mappings);
            }
            return scheme;
        }

        public Map<IssueType, String> getMapping(final String workflowName)
        {
            return Maps.filterValues(workflowMappings, new Predicate<String>()
            {
                @Override
                public boolean apply(@Nullable String input)
                {
                    return workflowName.equals(input);
                }
            });
        }

        public SimpleWorkflowScheme removeWorkflow(String workflowName)
        {
            for (Iterator<String> iterator = workflowMappings.values().iterator(); iterator.hasNext(); )
            {
                String actualName = iterator.next();
                if (workflowName.equals(actualName))
                {
                    iterator.remove();
                }
            }

            if (workflowName.equals(defaultWorkflow))
            {
                defaultWorkflow = null;
            }
            return this;
        }

        public SimpleWorkflowScheme assertIssueTypeMapping(WorkflowSchemeClient.IssueTypeMappingBean bean)
        {
            final IssueType type = IssueType.findById(bean.getIssueType());
            assertEquals(workflowMappings.get(type), bean.getWorkflow());
            return this;
        }

        public SimpleWorkflowScheme assertDefault(WorkflowSchemeClient.DefaultBean bean)
        {
            assertEquals(defaultWorkflow == null ? "jira" : defaultWorkflow, bean.getWorkflow());

            return this;
        }

        public SimpleWorkflowScheme copyWithDefault()
        {
            final SimpleWorkflowScheme copy = copy();
            if (copy.getDefaultWorkflow() == null)
            {
                copy.setDefaultWorkflow("jira");
            }

            if (draft && copy.getOriginalDefaultWorkflow() == null)
            {
                copy.setOriginalDefaultWorkflow("jira");
            }
            return copy;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleWorkflowScheme that = (SimpleWorkflowScheme) o;

            if (draft != that.draft) { return false; }
            if (defaultWorkflow != null ? !defaultWorkflow.equals(that.defaultWorkflow) : that.defaultWorkflow != null)
            { return false; }
            if (description != null ? !description.equals(that.description) : that.description != null)
            {
                return false;
            }
            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null)
            {
                return false;
            }
            if (lastModifiedUser != null ? !lastModifiedUser.equals(that.lastModifiedUser) : that.lastModifiedUser != null)
            { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
            if (originalDefaultWorkflow != null ? !originalDefaultWorkflow.equals(that.originalDefaultWorkflow) : that.originalDefaultWorkflow != null)
            { return false; }
            if (originalWorkflowMappings != null ? !originalWorkflowMappings.equals(that.originalWorkflowMappings) : that.originalWorkflowMappings != null)
            { return false; }
            if (self != null ? !self.equals(that.self) : that.self != null) { return false; }
            if (workflowMappings != null ? !workflowMappings.equals(that.workflowMappings) : that.workflowMappings != null)
            { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (defaultWorkflow != null ? defaultWorkflow.hashCode() : 0);
            result = 31 * result + (workflowMappings != null ? workflowMappings.hashCode() : 0);
            result = 31 * result + (originalDefaultWorkflow != null ? originalDefaultWorkflow.hashCode() : 0);
            result = 31 * result + (originalWorkflowMappings != null ? originalWorkflowMappings.hashCode() : 0);
            result = 31 * result + (draft ? 1 : 0);
            result = 31 * result + (lastModifiedUser != null ? lastModifiedUser.hashCode() : 0);
            result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
            result = 31 * result + (self != null ? self.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    private static class IssueType
    {
        private static final IssueType BUG = new IssueType("1", "Bug");
        private static final IssueType IMPROVMENT = new IssueType("4", "Improvement");
        private static final IssueType NEW_FEATURE = new IssueType("2", "New Feature");
        private static final IssueType TASK = new IssueType("3", "Task");

        private static final List<IssueType> ALL = ImmutableList.of(BUG, IMPROVMENT, NEW_FEATURE, TASK);

        private static final IssueType BAD = new IssueType("abc", "I Don't Actually Exist");

        private static IssueType findByName(final String name)
        {
            return Iterables.find(ALL, new Predicate<IssueType>()
            {
                @Override
                public boolean apply(IssueType input)
                {
                    return input.name.equals(name);
                }
            });
        }

        private static IssueType findById(final String id)
        {
            return Iterables.find(ALL, new Predicate<IssueType>()
            {
                @Override
                public boolean apply(IssueType input)
                {
                    return input.id.equals(id);
                }
            });
        }

        private final String id;
        private final String name;

        private IssueType(String id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            IssueType issueType = (IssueType) o;

            if (!id.equals(issueType.id)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            return id.hashCode();
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
