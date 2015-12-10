package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestProjectCategoryResource extends RestFuncTest
{
    private static final String PROJECT_CATEGORY = "projectCategory";

    private static final String SELF = "self";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    public static final String CATEGORY_NAME = "category_one";
    public static final String CATEGORY_DESCRIPTION = "description of category one";

    public static final String CATEGORY_NAME_2 = "category_two";
    public static final String CATEGORY_DESCRIPTION_2 = "description of category two";

    private ProjectCategoryClient preferencesClient;


    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        preferencesClient = new ProjectCategoryClient(getEnvironmentData());
        backdoor.restoreBlankInstance();
    }

    @Override
    protected void tearDownTest()
    {
        preferencesClient.cleanup();
    }

    public void testHappyPathUseCase()
    {
        //there are no project categories at first
        final ClientResponse responseGetAllCategoriesEmpty = preferencesClient.getAllCategories();
        assertThat(responseGetAllCategoriesEmpty.getStatus(), equalTo(ClientResponse.Status.OK.getStatusCode()));
        assertThat((Collection<Object>)responseGetAllCategoriesEmpty.getEntity(Collection.class), empty());
        preferencesClient.cleanup();

        //retrieving not existing category will fail
        final ClientResponse responseGetNotExistingCategory = preferencesClient.getCategory("0");
        assertThat(responseGetNotExistingCategory.getStatus(), equalTo(ClientResponse.Status.NOT_FOUND.getStatusCode()));
        preferencesClient.cleanup();

        //create new category
        final ClientResponse responseCreateCategory = preferencesClient.createCategory(CATEGORY_NAME, CATEGORY_DESCRIPTION);
        final Map<String, String> responseCreateCategoryEntity = responseCreateCategory.getEntity(Map.class);

        assertThat(responseGetAllCategoriesEmpty.getStatus(), equalTo(ClientResponse.Status.OK.getStatusCode()));
        assertThat(responseCreateCategoryEntity, hasKey(SELF));
        assertThat(responseCreateCategoryEntity, hasKey(ID));
        assertThat(responseCreateCategoryEntity, hasEntry(NAME, CATEGORY_NAME));
        assertThat(responseCreateCategoryEntity, hasEntry(DESCRIPTION, CATEGORY_DESCRIPTION));
        final String categoryId = responseCreateCategoryEntity.get(ID);
        preferencesClient.cleanup();

        //create second new category
        final ClientResponse responseCreateCategory2 = preferencesClient.createCategory(CATEGORY_NAME_2, CATEGORY_DESCRIPTION_2);
        final Map<String, String> responseCreateCategoryEntity2 = responseCreateCategory2.getEntity(Map.class);
        assertThat(responseCreateCategory2.getStatus(), equalTo(ClientResponse.Status.CREATED.getStatusCode()));
        final String categoryId2 = responseCreateCategoryEntity2.get(ID);
        preferencesClient.cleanup();

        //retrieving one of created categories by id
        final ClientResponse responseGetCategory = preferencesClient.getCategory(categoryId);
        final Map<String, String> responseGetCategoryEntity = responseGetCategory.getEntity(Map.class);
        assertThat(responseGetCategory.getStatus(), equalTo(ClientResponse.Status.OK.getStatusCode()));
        assertThat(responseGetCategoryEntity, hasKey(SELF));
        assertThat(responseGetCategoryEntity, hasEntry(ID, categoryId));
        assertThat(responseGetCategoryEntity, hasEntry(NAME, CATEGORY_NAME));
        assertThat(responseGetCategoryEntity, hasEntry(DESCRIPTION, CATEGORY_DESCRIPTION));
        preferencesClient.cleanup();

        //now retrieving all categories will return all of them
        final ClientResponse responseGetAllCategoriesTwo = preferencesClient.getAllCategories();
        assertThat(responseGetAllCategoriesTwo.getStatus(), equalTo(ClientResponse.Status.OK.getStatusCode()));
        final Collection<Object> responseGetAllCategoriesTwoEntity = responseGetAllCategoriesTwo .getEntity(Collection.class);
        assertThat(responseGetAllCategoriesTwoEntity, hasSize(2));
        preferencesClient.cleanup();

        //deleting one category
        final ClientResponse responseDeleteCategory = preferencesClient.deleteCategory(categoryId);
        assertThat(responseDeleteCategory.getStatus(), equalTo(ClientResponse.Status.NO_CONTENT.getStatusCode()));
        preferencesClient.cleanup();

        //now retrieving all categories will return one less
        final ClientResponse responseGetAllCategoriesOne = preferencesClient.getAllCategories();
        assertThat(responseGetAllCategoriesOne.getStatus(), equalTo(ClientResponse.Status.OK.getStatusCode()));
        final Collection<Object> responseGetAllCategoriesOneEntity = responseGetAllCategoriesOne.getEntity(Collection.class);
        assertThat(responseGetAllCategoriesOneEntity, hasSize(1));
        preferencesClient.cleanup();

        //deleting second category
        final ClientResponse responseDeleteCategory2 = preferencesClient.deleteCategory(categoryId2);
        assertThat(responseDeleteCategory2.getStatus(), equalTo(ClientResponse.Status.NO_CONTENT.getStatusCode()));
        preferencesClient.cleanup();

        //deleting second category again should fail
        final ClientResponse responseDeleteCategory3 = preferencesClient.getCategory(categoryId2);
        assertThat(responseDeleteCategory3.getStatus(), equalTo(ClientResponse.Status.NOT_FOUND.getStatusCode()));
        preferencesClient.cleanup();
    }

    private class ProjectCategoryClient extends RestApiClient<ProjectCategoryClient>
    {
        final private Set<ClientResponse> responses = Sets.newHashSet();

        protected ProjectCategoryClient(JIRAEnvironmentData environmentData)
        {
            super(environmentData);
        }

        private ClientResponse getAllCategories()
        {
            final WebResource webResource = createResource().path(PROJECT_CATEGORY);
            final ClientResponse clientResponse = webResource.get(ClientResponse.class);
            responses.add(clientResponse);
            return clientResponse;
        }

        private ClientResponse getCategory(final String id)
        {
            final WebResource webResource = createResource().path(PROJECT_CATEGORY + "/" + id);
            final ClientResponse clientResponse = webResource.get(ClientResponse.class);
            responses.add(clientResponse);
            return clientResponse;
        }

        private ClientResponse createCategory(final String name, final String description)
        {
            final WebResource webResource = createResource().path(PROJECT_CATEGORY);

            final Map<String,String> postBody = Maps.newHashMap();
            postBody.put("name", name);
            postBody.put("description", description);

            final ClientResponse post = webResource.accept("application/json").type("application/json").post(ClientResponse.class, postBody);

            responses.add(post);
            return post;
        }

        private ClientResponse deleteCategory(final String id)
        {
            final WebResource webResource = createResource().path(PROJECT_CATEGORY + "/" + id);
            final ClientResponse clientResponse = webResource.delete(ClientResponse.class);
            responses.add(clientResponse);
            return clientResponse;
        }

        private void cleanup()
        {
            for (ClientResponse response : responses)
            {
                response.close();
            }
        }
    }
}
