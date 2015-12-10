package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.atlassian.httpclient.api.HttpStatus;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.testkit.client.restclient.Response;

import com.google.common.collect.Maps;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hamcrest.Matchers;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.REST;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @since v6.2
 */
@WebTest ( { FUNC_TEST, REST })
public class TestWorkflowTransitionProperties extends RestFuncTest
{
    public static final int INITIAL_ACTION = 1;
    public static final int COMMON_ACTION = 5;
    public static final int SIMPLE_ACTION = 301;
    public static final int GLOBAL_ACTION = 711;
    public static final int INVALID_ACTION = 3738;

    public static final String ACTIVE_WORKFLOW_WITH_DRAFT = "ActiveWorkflowWithDraft";
    public static final String ACTIVE_WORKFLOW = "ActiveWorkflow";
    public static final String INACTIVE_WORKFLOW = "InactiveWorkflow";
    public static final String BAD_WORKFLOW = "BadWorkflow";

    public static final String TRANSITION_ID = "transitionId";
    public static final String WORKFLOW_NAME = "workflowName";
    public static final String WORKFLOW_MODE = "workflowMode";
    public static final String KEY = "key";

    public static final String INVALID_MODE = "wAT?";

    public static final String ID_KEY = "ID";
    public static final String INVALID_KEY = "InvalidKey";
    public static final String NON_ADMIN = "fred";
    public static final String VALUE = "value";

    private WorkflowRestApi restApi;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        backdoor.restoreDataFromResource("TestWorkflowTransitionProperties.xml");
        restApi = new WorkflowRestApi(environmentData);
    }

    public void testGetAll()
    {
        //Test active workflow.
        checkGetAll(ACTIVE_WORKFLOW, false);

        //Test active workflow with draft.
        checkGetAll(ACTIVE_WORKFLOW_WITH_DRAFT, false);

        //Test draft workflow.
        checkGetAll(ACTIVE_WORKFLOW_WITH_DRAFT, true);

        //Test inactive workflow.
        checkGetAll(INACTIVE_WORKFLOW, false);

        //Test inactive workflow in websudo. GET is not protected.
        runInWebsudo(new Runnable()
        {
            @Override
            public void run()
            {
                checkGetAll(INACTIVE_WORKFLOW, false);
            }
        });

        //Test workflow that does not exist.
        Response<?> response = restApi.getWorkflow(BAD_WORKFLOW).transition(INITIAL_ACTION).getAllResponse();
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test draft workflow that does not exist.
        response = restApi.getWorkflow(ACTIVE_WORKFLOW).draft().transition(INITIAL_ACTION).getAllResponse();
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test an invalid workflow mode.
        response = restApi.getWorkflow(ACTIVE_WORKFLOW).mode(INVALID_MODE).transition(INITIAL_ACTION).getAllResponse();
        assertBadResponse(response, HttpStatus.BAD_REQUEST, WORKFLOW_MODE);

        //Test as user that has no permission.
        response = restApi.loginAs(NON_ADMIN).getWorkflow(ACTIVE_WORKFLOW).transition(INITIAL_ACTION).getAllResponse();
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));

        //Test anonymous user.
        response = restApi.anonymous().getWorkflow(ACTIVE_WORKFLOW).transition(INITIAL_ACTION).getAllResponse();
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));
    }

    public void testGet()
    {
        //Test active workflow.
        checkGet(ACTIVE_WORKFLOW, false);

        //Test active workflow in websudo. GET is not protected.
        runInWebsudo(new Runnable()
        {
            @Override
            public void run()
            {
                checkGet(ACTIVE_WORKFLOW, false);
            }
        });

        //Test active workflow with draft.
        checkGet(ACTIVE_WORKFLOW_WITH_DRAFT, false);

        //Test draft workflow.
        checkGet(ACTIVE_WORKFLOW_WITH_DRAFT, true);

        //Test inactive workflow.
        checkGet(INACTIVE_WORKFLOW, false);

        //Test workflow that does not exist.
        Response<?> response = restApi.getWorkflow(BAD_WORKFLOW).transition(INITIAL_ACTION).getPropertyResponse(ID_KEY);
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test draft workflow that does not exist.
        response = restApi.getWorkflow(ACTIVE_WORKFLOW).draft().transition(INITIAL_ACTION).getPropertyResponse(ID_KEY);
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test an invalid workflow mode.
        response = restApi.getWorkflow(ACTIVE_WORKFLOW).mode(INVALID_MODE).transition(INITIAL_ACTION).getPropertyResponse(ID_KEY);
        assertBadResponse(response, HttpStatus.BAD_REQUEST, WORKFLOW_MODE);

        //Test as user that has no permission.
        response = restApi.loginAs(NON_ADMIN).getWorkflow(ACTIVE_WORKFLOW).transition(INITIAL_ACTION).getPropertyResponse(ID_KEY);
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));

        //Test anonymous user.
        response = restApi.anonymous().getWorkflow(ACTIVE_WORKFLOW).transition(INITIAL_ACTION).getPropertyResponse(ID_KEY);
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));
    }

    public void testPost()
    {
        //Check on inactive workflow.
        checkPost(INACTIVE_WORKFLOW, false);

        //Check on draft workflow.
        checkPost(ACTIVE_WORKFLOW_WITH_DRAFT, true);

        //Websudo changes will be rejected.
        runInWebsudo(new Runnable()
        {
            @Override
            public void run()
            {
                final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(ACTIVE_WORKFLOW_WITH_DRAFT, true);
                final WorkflowRestApi.Transition transition = restApiWorkflow.transition(INITIAL_ACTION);

                final Response<?> response = transition.addPropertyResponse("websudo", "Add Me");
                assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));
            }
        });

        //Check write to read-only workflow does not work.
        checkPostReadOnly(ACTIVE_WORKFLOW_WITH_DRAFT, false);
        checkPostReadOnly(ACTIVE_WORKFLOW, false);

        //Test workflow that does not exist.
        Response<?> response = restApi.getWorkflow(BAD_WORKFLOW).transition(INITIAL_ACTION)
                .addPropertyResponse("ABC", "DEF");
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test draft workflow that does not exist.
        response = restApi.getWorkflow(INACTIVE_WORKFLOW).draft().transition(INITIAL_ACTION)
                .addPropertyResponse("ABC", "DEF");
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test an invalid workflow mode.
        response = restApi.getWorkflow(INACTIVE_WORKFLOW).mode(INVALID_MODE).transition(INITIAL_ACTION)
                .addPropertyResponse("ABC", "DEF");
        assertBadResponse(response, HttpStatus.BAD_REQUEST, WORKFLOW_MODE);

        //Test as user that has no permission.
        response = restApi.loginAs(NON_ADMIN).getWorkflow(INACTIVE_WORKFLOW).transition(INITIAL_ACTION)
                .addPropertyResponse("ABC", "DEF");
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));

        //Test anonymous user.
        response = restApi.anonymous().getWorkflow(INACTIVE_WORKFLOW).transition(INITIAL_ACTION)
                .addPropertyResponse("ABC", "DEF");
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));
    }

    public void testPut()
    {
        //Check on inactive workflow.
        checkPut(INACTIVE_WORKFLOW, false);

        //Check on draft workflow.
        checkPut(ACTIVE_WORKFLOW_WITH_DRAFT, true);

        //Websudo changes will be rejected.
        runInWebsudo(new Runnable()
        {
            @Override
            public void run()
            {
                final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(ACTIVE_WORKFLOW_WITH_DRAFT, true);
                final WorkflowRestApi.Transition transition = restApiWorkflow.transition(INITIAL_ACTION);

                final Response<?> response = transition.setPropertyResponse("websudo", "Add Me");
                assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));
            }
        });

        //Check write to read-only workflow does not work.
        checkPutReadOnly(ACTIVE_WORKFLOW_WITH_DRAFT, false);
        checkPutReadOnly(ACTIVE_WORKFLOW, false);

        //Test workflow that does not exist.
        Response<?> response = restApi.getWorkflow(BAD_WORKFLOW).transition(INITIAL_ACTION)
                .setPropertyResponse("ABC", "DEF");
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test draft workflow that does not exist.
        response = restApi.getWorkflow(INACTIVE_WORKFLOW).draft().transition(INITIAL_ACTION)
                .setPropertyResponse("ABC", "DEF");
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test an invalid workflow mode.
        response = restApi.getWorkflow(INACTIVE_WORKFLOW).mode(INVALID_MODE).transition(INITIAL_ACTION)
                .setPropertyResponse("ABC", "DEF");
        assertBadResponse(response, HttpStatus.BAD_REQUEST, WORKFLOW_MODE);

        //Test as user that has no permission.
        response = restApi.loginAs(NON_ADMIN).getWorkflow(INACTIVE_WORKFLOW).transition(INITIAL_ACTION)
                .setPropertyResponse("ABC", "DEF");
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));

        //Test anonymous user.
        response = restApi.anonymous().getWorkflow(INACTIVE_WORKFLOW).transition(INITIAL_ACTION)
                .setPropertyResponse("ABC", "DEF");
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));
    }

    public void testDelete()
    {
        //Check delete on mutable workflows.
        checkDelete(INACTIVE_WORKFLOW, false);
        checkDelete(ACTIVE_WORKFLOW_WITH_DRAFT, true);

        //Websudo changes will be rejected.
        runInWebsudo(new Runnable()
        {
            @Override
            public void run()
            {
                final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(ACTIVE_WORKFLOW_WITH_DRAFT, true);
                final WorkflowRestApi.Transition transition = restApiWorkflow.transition(INITIAL_ACTION);

                final Response<?> response = transition.deletePropertyResponse("websudo");
                assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));
            }
        });

        //Check write to read-only workflow does not work.
        checkDeleteReadOnly(ACTIVE_WORKFLOW_WITH_DRAFT, false);
        checkDeleteReadOnly(ACTIVE_WORKFLOW, false);

        //Test workflow that does not exist.
        Response<?> response = restApi.getWorkflow(BAD_WORKFLOW).transition(INITIAL_ACTION)
                .deletePropertyResponse(ID_KEY);
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test draft workflow that does not exist.
        response = restApi.getWorkflow(INACTIVE_WORKFLOW).draft().transition(INITIAL_ACTION)
                .deletePropertyResponse(ID_KEY);
        assertBadResponse(response, HttpStatus.NOT_FOUND, WORKFLOW_NAME);

        //Test an invalid workflow mode.
        response = restApi.getWorkflow(INACTIVE_WORKFLOW).mode(INVALID_MODE).transition(INITIAL_ACTION)
                .deletePropertyResponse(ID_KEY);
        assertBadResponse(response, HttpStatus.BAD_REQUEST, WORKFLOW_MODE);

        //Test as user that has no permission.
        response = restApi.loginAs(NON_ADMIN).getWorkflow(INACTIVE_WORKFLOW).transition(INITIAL_ACTION)
                .deletePropertyResponse(ID_KEY);
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));

        //Test anonymous user.
        response = restApi.anonymous().getWorkflow(INACTIVE_WORKFLOW).transition(INITIAL_ACTION)
                .deletePropertyResponse(ID_KEY);
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.UNAUTHORIZED.code));
    }

    private void checkDeleteReadOnly(final String workflowName, final boolean draft)
    {
        final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(workflowName, draft);
        checkDeleteReadOnly(restApiWorkflow.transition(INITIAL_ACTION));
        checkDeleteReadOnly(restApiWorkflow.transition(GLOBAL_ACTION));
        checkDeleteReadOnly(restApiWorkflow.transition(COMMON_ACTION));
        checkDeleteReadOnly(restApiWorkflow.transition(SIMPLE_ACTION));
    }

    public void checkDeleteReadOnly(WorkflowRestApi.Transition transition)
    {
        //Try an remove stuff to read only workflow. It should fail.
        Response<?> response = transition.deletePropertyResponse(ID_KEY);
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.BAD_REQUEST.code));
    }

    private void checkDelete(final String workflowName, final boolean draft)
    {
        final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(workflowName, draft);
        checkDelete(restApiWorkflow.transition(INITIAL_ACTION));
        checkDelete(restApiWorkflow.transition(GLOBAL_ACTION));
        checkDelete(restApiWorkflow.transition(COMMON_ACTION));
        checkDelete(restApiWorkflow.transition(SIMPLE_ACTION));
    }

    private void checkDelete(WorkflowRestApi.Transition transition)
    {
        Response<?> id = transition.deletePropertyResponse(ID_KEY);
        assertThat(id.statusCode, Matchers.equalTo(HttpStatus.OK.code));

        id = transition.getPropertyResponse(ID_KEY);
        assertThat(id.statusCode, Matchers.equalTo(HttpStatus.NOT_FOUND.code));

        id = transition.deletePropertyResponse(ID_KEY);
        assertThat(id.statusCode, Matchers.equalTo(HttpStatus.NOT_MODIFIED.code));

        id = transition.deletePropertyResponse("   \t\n\r\t  ");
        assertBadResponse(id, HttpStatus.BAD_REQUEST, KEY);
    }

    private void checkPut(final String workflowName, final boolean draft)
    {
        final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(workflowName, draft);

        checkPut(restApiWorkflow, INITIAL_ACTION);
        checkPut(restApiWorkflow, COMMON_ACTION);
        checkPut(restApiWorkflow, GLOBAL_ACTION);
        checkPut(restApiWorkflow, SIMPLE_ACTION);
    }

    private void checkPut(final WorkflowRestApi.Workflow restApiWorkflow, final int transitionId)
    {
        final WorkflowRestApi.Transition transition = restApiWorkflow.transition(transitionId);

        //Add the property.
        transition.setProperty("NEW", "Property");
        assertThat(transition.getProperty("NEW"), Matchers.equalTo("Property"));

        //Try and update.
        transition.setProperty("NEW", "Property2");
        assertThat(transition.getProperty("NEW"), Matchers.equalTo("Property2"));

        //Try and set to an empty value.
        transition.setProperty("NEW", "\t\r\n\t");
        assertThat(transition.getProperty("NEW"), Matchers.equalTo(""));

        //Empty key.
        Response<?> response = transition.setPropertyResponse("   \t\n\r\t  ", "BAD_VALUE");
        assertBadResponse(response, HttpStatus.BAD_REQUEST, KEY);

        //Empty null value.
        response = transition.addPropertyResponse("BAD_VALUE", null);
        assertBadResponse(response, HttpStatus.BAD_REQUEST, VALUE);
    }

    private void checkPutReadOnly(String workflowName, final boolean draft)
    {
        final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(workflowName, draft);

        checkPutReadOnly(restApiWorkflow, INITIAL_ACTION);
        checkPutReadOnly(restApiWorkflow, COMMON_ACTION);
        checkPutReadOnly(restApiWorkflow, GLOBAL_ACTION);
        checkPutReadOnly(restApiWorkflow, SIMPLE_ACTION);
    }

    private void checkPutReadOnly(final WorkflowRestApi.Workflow restApiWorkflow, final int transitionId)
    {
        final WorkflowRestApi.Transition transition = restApiWorkflow.transition(transitionId);

        //Try an add stuff to read only workflow. It should fail.
        Response<?> response = transition.setPropertyResponse("Valid", "ReadOnly");
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.BAD_REQUEST.code));
    }

    private void checkPostReadOnly(String workflowName, final boolean draft)
    {
        final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(workflowName, draft);

        checkPostReadOnly(restApiWorkflow, INITIAL_ACTION);
        checkPostReadOnly(restApiWorkflow, COMMON_ACTION);
        checkPostReadOnly(restApiWorkflow, GLOBAL_ACTION);
        checkPostReadOnly(restApiWorkflow, SIMPLE_ACTION);
    }

    private void checkPostReadOnly(final WorkflowRestApi.Workflow restApiWorkflow, final int transitionId)
    {
        final WorkflowRestApi.Transition transition = restApiWorkflow.transition(transitionId);

        //Try an add stuff to read only workflow. It should fail.
        Response<?> response = transition.addPropertyResponse("Valid", "ReadOnly");
        assertThat(response.statusCode, Matchers.equalTo(HttpStatus.BAD_REQUEST.code));
    }

    private void checkPost(final String workflowName, final boolean draft)
    {
        final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(workflowName, draft);

        checkPost(restApiWorkflow, INITIAL_ACTION);
        checkPost(restApiWorkflow, COMMON_ACTION);
        checkPost(restApiWorkflow, GLOBAL_ACTION);
        checkPost(restApiWorkflow, SIMPLE_ACTION);
    }

    private void checkPost(final WorkflowRestApi.Workflow restApiWorkflow, final int transitionId)
    {
        final WorkflowRestApi.Transition transition = restApiWorkflow.transition(transitionId);

        //Add the property.
        transition.addProperty("NEW", "Property");
        assertThat(transition.getProperty("NEW"), Matchers.equalTo("Property"));

        //Empty property allowed?
        transition.addProperty("EMPTY", "       ");
        assertThat(transition.getProperty("EMPTY"), Matchers.equalTo(""));

        //Try and add a duplicate.
        Response<?> response = transition.addPropertyResponse("NEW", "Dup");
        assertBadResponse(response, HttpStatus.BAD_REQUEST, KEY);

        //Try and add with empty key.
        response = transition
                .addPropertyResponse("   \t\n\r\t  ", "BAD_VALUE");
        assertBadResponse(response, HttpStatus.BAD_REQUEST, KEY);

        //Try and add with null value.
        response = transition.addPropertyResponse("BAD_VALUE", null);
        assertBadResponse(response, HttpStatus.BAD_REQUEST, VALUE);
    }

    private void checkGet(String workflowName, boolean draft)
    {
        final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(workflowName, draft);

        //Initial action.
        assertThat(restApiWorkflow.transition(INITIAL_ACTION).getProperty(ID_KEY),
                Matchers.equalTo(String.valueOf(INITIAL_ACTION)));

        //Common action.
        assertThat(restApiWorkflow.transition(COMMON_ACTION).getProperty(ID_KEY),
                Matchers.equalTo(String.valueOf(COMMON_ACTION)));

        //Step local action.
        assertThat(restApiWorkflow.transition(SIMPLE_ACTION).getProperty(ID_KEY),
                Matchers.equalTo(String.valueOf(SIMPLE_ACTION)));

        //Global action.
        assertThat(restApiWorkflow.transition(GLOBAL_ACTION).getProperty(ID_KEY),
                Matchers.equalTo(String.valueOf(GLOBAL_ACTION)));

        //Check an invalid action
        Response<?> singleResponse = restApiWorkflow.transition(INVALID_ACTION).getPropertyResponse(ID_KEY);
        assertBadResponse(singleResponse, HttpStatus.NOT_FOUND, TRANSITION_ID);

        //Check invalid key
        singleResponse = restApiWorkflow.transition(INVALID_ACTION).getPropertyResponse(INVALID_KEY);
        assertBadResponse(singleResponse, HttpStatus.NOT_FOUND, TRANSITION_ID);
    }

    private WorkflowRestApi.Workflow getWorkflow(final String workflowName, final boolean draft)
    {
        final WorkflowRestApi.Workflow restApiWorkflow;
        if (draft)
        {
            restApiWorkflow = restApi.getWorkflow(workflowName).draft();
        }
        else
        {
            restApiWorkflow = restApi.getWorkflow(workflowName);
        }
        return restApiWorkflow;
    }

    private void checkGetAll(String workflowName, boolean draft)
    {
        final WorkflowRestApi.Workflow restApiWorkflow = getWorkflow(workflowName, draft);

        //Initial action.
        assertThat(restApiWorkflow.transition(INITIAL_ACTION).getAllProperties(),
                Matchers.equalTo(expectedProperties(workflowName, draft, INITIAL_ACTION, false)));

        //Common action.
        assertThat(restApiWorkflow.transition(COMMON_ACTION).getAllProperties(),
                Matchers.equalTo(expectedProperties(workflowName, draft, COMMON_ACTION, false)));

        //Step local action.
        assertThat(restApiWorkflow.transition(SIMPLE_ACTION).getAllProperties(),
                Matchers.equalTo(expectedProperties(workflowName, draft, SIMPLE_ACTION, false)));

        //Global action.
        assertThat(restApiWorkflow.transition(GLOBAL_ACTION).getAllProperties(),
                Matchers.equalTo(expectedProperties(workflowName, draft, GLOBAL_ACTION, true)));

        //Check an invalid action
        Response<?> allResponse = restApiWorkflow.transition(INVALID_ACTION).getAllResponse();
        assertBadResponse(allResponse, HttpStatus.NOT_FOUND, TRANSITION_ID);
    }

    private void assertBadResponse(final Response<?> allResponse, final HttpStatus status, final String errorKey)
    {
        assertThat(allResponse.body, Matchers.nullValue());
        assertThat(allResponse.statusCode, Matchers.equalTo(status.code));
        assertThat(allResponse.entity.errors, Matchers.hasKey(errorKey));
    }

    private Map<String, String> expectedProperties(final String workflowName, final boolean draft, int id, final boolean global)
    {
        Map<String, String> expectedProperties = Maps.newHashMap();
        expectedProperties.put(ID_KEY, String.valueOf(id));
        expectedProperties.put("WF", workflowName);
        if (draft)
        {
            expectedProperties.put("DRAFT", "YES");
        }
        if (global)
        {
            expectedProperties.put("GLOBAL", "YES");
        }
        return expectedProperties;
    }

    public Map<String, String> toMap(Object...args)
    {
        if ((args.length & 0x1) == 0)
        {
            Map<String, String> result = Maps.newHashMap();
            for (int i = 0; i < args.length; )
            {
                result.put(String.valueOf(args[i++]), String.valueOf(args[i++]));
            }
            return result;
        }
        else
        {
            throw new IllegalArgumentException("Odd number of arguments.");
        }
    }

    public static class WorkflowRestApi extends RestApiClient<WorkflowRestApi>
    {
        protected WorkflowRestApi(final JIRAEnvironmentData environmentData)
        {
            super(environmentData);
        }

        @Override
        protected WebResource createResource()
        {
            return super.createResource().path("workflow");
        }

        public ActiveWorkflow getWorkflow(String name)
        {
            return new ActiveWorkflow(name);
        }

        public abstract class Workflow
        {
            private final String name;

            public Workflow(final String name)
            {
                this.name = name;
            }

            public String getName()
            {
                return name;
            }

            WebResource resource()
            {
                return createResource().queryParam("workflowName", name).queryParam("workflowMode", getMode());
            }

            abstract String getMode();

            public Transition transition(int i)
            {
                return new Transition(this, i);
            }
        }

        public class ActiveWorkflow extends Workflow
        {
            private ActiveWorkflow(final String name)
            {
                super(name);
            }

            @Override
            String getMode()
            {
                return "live";
            }

            public DraftWorkflow draft()
            {
                return new DraftWorkflow(getName());
            }

            public ConstantMode mode(String mode)
            {
                return new ConstantMode(getName(), mode);
            }
        }

        public class DraftWorkflow extends Workflow
        {
            private DraftWorkflow(final String name)
            {
                super(name);
            }

            public ActiveWorkflow parent()
            {
                return new ActiveWorkflow(getName());
            }

            @Override
            String getMode()
            {
                return "draft";
            }
        }

        public class ConstantMode extends Workflow
        {
            private final String mode;

            private ConstantMode(final String name, final String mode)
            {
                super(name);
                this.mode = mode;
            }

            @Override
            String getMode()
            {
                return mode;
            }
        }

        public class Transition
        {
            private final Workflow workflow;
            private final long transition;

            private Transition(final Workflow workflow, final long transition)
            {
                this.workflow = workflow;
                this.transition = transition;
            }

            public String getProperty(String key)
            {
                final PropertyBean propertyBean = propertiesResource().queryParam("key", key).get(PropertyBean.class);
                return propertyBean.getValue();
            }

            public Response<String> getPropertyResponse(final String key)
            {
                Response<PropertyBean> response = toResponse(new Method()
                {
                    @Override
                    public ClientResponse call()
                    {
                        return propertiesResource()
                                .queryParam("key", key)
                                .accept(MediaType.APPLICATION_JSON_TYPE)
                                .get(ClientResponse.class);
                    }
                }, PropertyBean.class);

                if (response.body != null)
                {
                    return new Response<String>(response.statusCode, response.entity, response.body.getValue());
                }
                else
                {
                    return new Response<String>(response.statusCode, response.entity, null);
                }
            }

            public Map<String, String> getAllProperties()
            {
                return PropertyBean.toMap(propertiesResource().get(PropertyBean.LIST));
            }

            public Response<Map<String, String>> getAllResponse()
            {
                Response<List<PropertyBean>> response = toResponse(new Method()
                {
                    @Override
                    public ClientResponse call()
                    {
                        return propertiesResource()
                                .accept(MediaType.APPLICATION_JSON_TYPE)
                                .get(ClientResponse.class);
                    }
                }, PropertyBean.LIST);

                if (response.body != null)
                {
                    return new Response<Map<String, String>>(response.statusCode, response.entity, PropertyBean.toMap(response.body));
                }
                else
                {
                    return new Response<Map<String, String>>(response.statusCode, response.entity, null);
                }
            }

            public void addProperty(final String key, final String value)
            {
                propertiesResource()
                        .queryParam("key", key)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(new PropertyBean(key, value));
            }

            public Response<?> addPropertyResponse(final String key, final String value)
            {
                return toResponse(new Method()
                {
                    @Override
                    public ClientResponse call()
                    {
                        return propertiesResource()
                                .queryParam("key", key)
                                .type(MediaType.APPLICATION_JSON_TYPE)
                                .accept(MediaType.APPLICATION_JSON_TYPE)
                                .post(ClientResponse.class, new PropertyBean(key, value));
                    }
                });
            }

            public void setProperty(final String key, final String value)
            {
                propertiesResource()
                        .queryParam("key", key)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .put(new PropertyBean(key, value));
            }

            public Response<?> setPropertyResponse(final String key, final String value)
            {
                return toResponse(new Method()
                {
                    @Override
                    public ClientResponse call()
                    {
                        return propertiesResource()
                                .queryParam("key", key)
                                .type(MediaType.APPLICATION_JSON_TYPE)
                                .accept(MediaType.APPLICATION_JSON_TYPE)
                                .put(ClientResponse.class, new PropertyBean(key, value));
                    }
                });
            }


            public void deleteProperty(final String key)
            {
                propertiesResource()
                    .queryParam("key", key)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .delete();
            }

            public Response<?> deletePropertyResponse(final String key)
            {
                return toResponse(new Method()
                {
                    @Override
                    public ClientResponse call()
                    {
                        return propertiesResource()
                                .queryParam("key", key)
                                .type(MediaType.APPLICATION_JSON_TYPE)
                                .accept(MediaType.APPLICATION_JSON_TYPE)
                                .delete(ClientResponse.class);
                    }
                });
            }


            WebResource propertiesResource()
            {
                return resource().path("properties");
            }

            WebResource resource()
            {
                return workflow.resource().path("transitions").path(String.valueOf(transition));
            }
        }
    }

    private void runInWebsudo(Runnable runnable)
    {
        backdoor.websudo().enable();
        try
        {
            runnable.run();
        }
        finally
        {
            backdoor.websudo().disable();
        }
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class PropertyBean
    {
        public static final GenericType<List<PropertyBean>> LIST = new GenericType<List<PropertyBean>>(){};

        private final String key;
        private final String value;

        @JsonCreator
        public PropertyBean(@JsonProperty ("key") String key, @JsonProperty("value") String value)
        {
            this.key = key;
            this.value = value;
        }

        @JsonProperty
        public String getKey()
        {
            return key;
        }

        @JsonProperty
        public String getValue()
        {
            return value;
        }

        @JsonProperty
        public String getId()
        {
            return getKey();
        }

        public static Map<String, String> toMap(List<PropertyBean> beans)
        {
            Map<String, String> map = Maps.newHashMap();
            for (PropertyBean bean : beans)
            {
                map.put(bean.getKey(), bean.getValue());
            }
            return map;
        }
    }
}
