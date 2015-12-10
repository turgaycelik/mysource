package com.atlassian.jira.rest.v2.entity.property;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.issue.fields.rest.json.beans.*;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.atlassian.jira.entity.property.EntityPropertyService.DeletePropertyValidationResult;
import static com.atlassian.jira.entity.property.EntityPropertyService.EntityPropertyInput;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyKeys;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import static com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import static com.atlassian.jira.rest.v2.entity.property.EntityPropertiesKeysBean.EntityPropertyKeyBean;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
public class BasePropertyResourceTest
{
    private static final Long ENTITY_ID = 1l;
    private static final String PROPERTY_KEY = "property.key";
    private static final String ERROR = "error.msg";

    @Mock public EntityPropertyService<ArtificialEntity> entityPropertyService;
    @Mock public JiraAuthenticationContext authenticationContext;
    @Mock public ApplicationUser user;
    @Mock public JiraBaseUrls jiraBaseUrls;
    @Mock public I18nHelper i18n;
    @Mock public Function2<Long, String, String> entityIdAndPropertyKeyToSelfFunction;
    @Mock @AvailableInContainer public ApplicationProperties applicationProperties;

    @Rule public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @Rule public final ExpectedException exception = ExpectedException.none();

    private BasePropertyResource<ArtificialEntity> basePropertyResource;

    @Before
    public void setup()
    {
        this.basePropertyResource = new BasePropertyResource<ArtificialEntity>(entityPropertyService, authenticationContext, jiraBaseUrls, i18n, entityIdAndPropertyKeyToSelfFunction, mock(EntityPropertyType.class));
        when(applicationProperties.getEncoding()).thenReturn(Charsets.UTF_8.toString());
        when(authenticationContext.getUser()).thenReturn(user);
    }

    @Test
    public void setNewProperty()
    {
        PropertyResult propertyResult = new PropertyResult(new SimpleErrorCollection(), Option.none(EntityProperty.class));
        SetPropertyValidationResult setPropertyValidationResult =
                new SetPropertyValidationResult(new SimpleErrorCollection(), Option.some(mock(EntityPropertyInput.class)));

        when(entityPropertyService.getProperty(user, ENTITY_ID, PROPERTY_KEY)).thenReturn(propertyResult);
        when(entityPropertyService.validateSetProperty(eq(user), any(Long.class), any(PropertyInput.class))).thenReturn(setPropertyValidationResult);
        when(entityPropertyService.setProperty(eq(user), eq(setPropertyValidationResult))).thenReturn(
                new PropertyResult(new SimpleErrorCollection(), Option.some(mock(EntityProperty.class))));

        Response response =  basePropertyResource.setProperty(ENTITY_ID.toString(), PROPERTY_KEY, getJsonObject());

        assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
    }

    @Test
    public void replaceExistingProperty()
    {
        PropertyResult propertyResult = new PropertyResult(new SimpleErrorCollection(), Option.some(mock(EntityProperty.class)));
        SetPropertyValidationResult setPropertyValidationResult = new SetPropertyValidationResult(new SimpleErrorCollection(), Option.some(mock(EntityPropertyInput.class)));

        when(entityPropertyService.getProperty(eq(user), eq(ENTITY_ID), eq(PROPERTY_KEY))).thenReturn(propertyResult);
        when(entityPropertyService.validateSetProperty(eq(user), eq(ENTITY_ID), any(PropertyInput.class)))
                .thenReturn(setPropertyValidationResult);
        when(entityPropertyService.setProperty(eq(user), eq(setPropertyValidationResult))).thenReturn(
                new PropertyResult(new SimpleErrorCollection(), Option.some(mock(EntityProperty.class))));

        Response response =  basePropertyResource.setProperty(ENTITY_ID.toString(), PROPERTY_KEY, getJsonObject());

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void inputStreamTooLong() throws IOException
    {
        InputStream inputStream = mock(InputStream.class);
        when(inputStream.read()).thenReturn(1);

        exception.expect(BadRequestWebException.class);
        basePropertyResource.setProperty(ENTITY_ID.toString(), PROPERTY_KEY, mockHttpServletRequest(inputStream));
    }

    @Test
    public void userNotLoggedInWhenSettingProperty()
    {
        when(authenticationContext.getUser()).thenReturn(null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.NOT_LOGGED_IN);

        when(entityPropertyService.validateSetProperty(any(ApplicationUser.class), eq(ENTITY_ID), any(PropertyInput.class)))
                .thenReturn(new SetPropertyValidationResult(errorCollection, Option.<EntityPropertyInput>none()));

        Response response = basePropertyResource.setProperty(ENTITY_ID.toString(), PROPERTY_KEY, getJsonObject());
        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void userNotAllowedToEditEntityWhenSettingProperty()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.FORBIDDEN);

        when(entityPropertyService.validateSetProperty(any(ApplicationUser.class), eq(ENTITY_ID), any(PropertyInput.class)))
                .thenReturn(new SetPropertyValidationResult(errorCollection, Option.<EntityPropertyInput>none()));

        Response response = basePropertyResource.setProperty(ENTITY_ID.toString(), PROPERTY_KEY, getJsonObject());
        assertThat(response.getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void entityOnWhichPropertyIsSetDoesNotExist()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("jira.properties.service.entity.does.not.exist", ErrorCollection.Reason.NOT_FOUND);

        when(entityPropertyService.validateSetProperty(any(ApplicationUser.class), eq(ENTITY_ID), any(PropertyInput.class)))
                .thenReturn(new SetPropertyValidationResult(errorCollection, Option.<EntityPropertyInput>none()));

        Response response = basePropertyResource.setProperty(ENTITY_ID.toString(), PROPERTY_KEY, getJsonObject());
        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void gettingPropertyWithExistingKey()
    {
        EntityProperty entityProperty = mock(EntityProperty.class);
        when(entityProperty.getValue()).thenReturn("value");
        when(entityProperty.getKey()).thenReturn(PROPERTY_KEY);
        when(entityProperty.getEntityId()).thenReturn(ENTITY_ID);
        when(jiraBaseUrls.restApi2BaseUrl()).thenReturn("http://www.example.com/jira/rest/api/2/");
        when(entityIdAndPropertyKeyToSelfFunction.apply(eq(ENTITY_ID), eq(PROPERTY_KEY)))
                .thenReturn(String.format("artentity/%d/properties/%s", ENTITY_ID, PROPERTY_KEY));
        when(entityPropertyService.getProperty(eq(user), eq(ENTITY_ID), eq(PROPERTY_KEY)))
                .thenReturn(new PropertyResult(new SimpleErrorCollection(), Option.option(entityProperty)));

        Response response = basePropertyResource.getProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), notNullValue());

        com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean entity = (com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean) response.getEntity();
        assertThat(entity.getSelf(), is(String.format("http://www.example.com/jira/rest/api/2/artentity/%d/properties/%s", ENTITY_ID, PROPERTY_KEY)));
        assertThat(entity.getKey(), is(PROPERTY_KEY));
        assertThat(entity.getValue(), is("value"));
    }

    @Test
    public void gettingNotExistingProperty()
    {
        when(entityPropertyService.getProperty(eq(user), any(Long.class), any(String.class)))
                .thenReturn(new PropertyResult(new SimpleErrorCollection(), Option.<EntityProperty>none()));

        Response response = basePropertyResource.getProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void gettingPropertyForNotExistingEntity()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.NOT_FOUND);
        when(entityPropertyService.getProperty(eq(user), any(Long.class), any(String.class)))
                .thenReturn(new PropertyResult(errorCollection, Option.<EntityProperty>none()));

        Response response = basePropertyResource.getProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void gettingPropertyWithoutPermissions()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.FORBIDDEN);
        when(entityPropertyService.getProperty(eq(user), any(Long.class), any(String.class)))
                .thenReturn(new PropertyResult(errorCollection, Option.<EntityProperty>none()));

        Response response = basePropertyResource.getProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void gettingPropertyWithoutUserLoggedIn()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.NOT_LOGGED_IN);
        when(entityPropertyService.getProperty(eq(user), any(Long.class), any(String.class)))
                .thenReturn(new PropertyResult(errorCollection, Option.<EntityProperty>none()));

        Response response = basePropertyResource.getProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void deletingProperty()
    {
        EntityProperty entityProperty = mock(EntityProperty.class);
        when(entityProperty.getValue()).thenReturn("value");
        when(entityProperty.getKey()).thenReturn(PROPERTY_KEY);
        when(entityPropertyService.validateDeleteProperty(eq(user), eq(ENTITY_ID), eq(PROPERTY_KEY)))
                .thenReturn(new DeletePropertyValidationResult(new SimpleErrorCollection(), Option.option(entityProperty)));

        Response response = basePropertyResource.deleteProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
    }

    @Test
    public void deletingNotExistingProperty()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.NOT_FOUND);

        when(entityPropertyService.validateDeleteProperty(eq(user), eq(ENTITY_ID), eq(PROPERTY_KEY)))
                .thenReturn(new DeletePropertyValidationResult(errorCollection, Option.<EntityProperty>none()));

        Response response = basePropertyResource.deleteProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void deletingPropertyWithoutPermissionToEntity()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.FORBIDDEN);
        when(entityPropertyService.validateDeleteProperty(eq(user), eq(ENTITY_ID), eq(PROPERTY_KEY)))
                .thenReturn(new DeletePropertyValidationResult(errorCollection, Option.<EntityProperty>none()));

        Response response = basePropertyResource.deleteProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void deletingPropertyWithoutUserLoggedIn()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.NOT_LOGGED_IN);
        when(entityPropertyService.validateDeleteProperty(eq(user), eq(ENTITY_ID), eq(PROPERTY_KEY)))
                .thenReturn(new DeletePropertyValidationResult(errorCollection, Option.<EntityProperty>none()));

        Response response = basePropertyResource.deleteProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void deletingPropertyFromNotExistingEntity()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.NOT_FOUND);
        when(entityPropertyService.validateDeleteProperty(eq(user), eq(ENTITY_ID), eq(PROPERTY_KEY)))
                .thenReturn(new DeletePropertyValidationResult(errorCollection, Option.<EntityProperty>none()));

        Response response = basePropertyResource.deleteProperty(ENTITY_ID.toString(), PROPERTY_KEY);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testGettingPropertiesKeys()
    {
        ArtificialEntity entity = mock(ArtificialEntity.class);
        when(entity.getId()).thenReturn(ENTITY_ID);
        when(jiraBaseUrls.restApi2BaseUrl()).thenReturn("http://www.example.com/jira/rest/api/2/");
        doAnswer(new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                return invocation.getArguments()[1];
            }
        }).when(entityIdAndPropertyKeyToSelfFunction).apply(eq(ENTITY_ID), any(String.class));
        when(entityPropertyService.getPropertiesKeys(eq(user), eq(ENTITY_ID)))
                .thenReturn(new PropertyKeys<ArtificialEntity>(new SimpleErrorCollection(), Lists.newArrayList("property1", "property2", "property3"), entity));

        Response response = basePropertyResource.getPropertiesKeys(ENTITY_ID.toString());

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        Iterable<EntityPropertyKeyBean> propertyKeyBeans =
                ((EntityPropertiesKeysBean) response.getEntity()).getEntityPropertyKeyBeans();

        assertThat(propertyKeyBeans, Matchers.<EntityPropertyKeyBean>hasItem(propertyBean("property1")));
        assertThat(propertyKeyBeans, Matchers.<EntityPropertyKeyBean>hasItem(propertyBean("property2")));
        assertThat(propertyKeyBeans, Matchers.<EntityPropertyKeyBean>hasItem(propertyBean("property3")));
    }

    @Test
    public void gettingPropertiesKeysForNonExistingEntity()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.NOT_FOUND);
        when(entityPropertyService.getPropertiesKeys(eq(user), eq(ENTITY_ID)))
                .thenReturn(new PropertyKeys<ArtificialEntity>(errorCollection, Collections.<String>emptyList(), null));

        Response response = basePropertyResource.getPropertiesKeys(ENTITY_ID.toString());

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void gettingEntityPropertiesKeysWithoutUserLoggedIn()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.NOT_LOGGED_IN);
        when(entityPropertyService.getPropertiesKeys(eq(user), eq(ENTITY_ID)))
                .thenReturn(new PropertyKeys<ArtificialEntity>(errorCollection, Collections.<String>emptyList(), null));

        Response response = basePropertyResource.getPropertiesKeys(ENTITY_ID.toString());

        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }

    @Test
    public void gettingEntityPropertiesKeysWithoutPermissions()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(ERROR, ErrorCollection.Reason.FORBIDDEN);
        when(entityPropertyService.getPropertiesKeys(eq(user), eq(ENTITY_ID)))
                .thenReturn(new PropertyKeys<ArtificialEntity>(errorCollection, Collections.<String>emptyList(), null));

        Response response = basePropertyResource.getPropertiesKeys(ENTITY_ID.toString());

        assertThat(response.getStatus(), is(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void badRequestWhenInvalidId()
    {
        exception.expect(BadRequestWebException.class);
        basePropertyResource.getPropertiesKeys("123g");
    }

    private HttpServletRequest getJsonObject()
    {
        final ByteArrayInputStream bis = new ByteArrayInputStream(new JSONObject(ImmutableMap.<String, String>of("x", "1")).toString().getBytes());
        return mockHttpServletRequest(bis);
    }

    private HttpServletRequest mockHttpServletRequest(final InputStream bis)
    {
        ServletInputStream servletInputStream = new ServletInputStream()
        {
            public int read() throws IOException
            {
                return bis.read();
            }
        };
        final HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        try
        {
            when(servletRequest.getInputStream()).thenReturn(servletInputStream);
            return servletRequest;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Matcher<EntityPropertyKeyBean> propertyBean(final String propertyKey)
    {
        return new TypeSafeMatcher<EntityPropertyKeyBean>()
        {
            @Override
            public boolean matchesSafely(final EntityPropertyKeyBean entityPropertyKeyBean)
            {
                return entityPropertyKeyBean.getKey().equals(propertyKey) && entityPropertyKeyBean.getSelf().endsWith(propertyKey);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("No item with key " + propertyKey);
            }
        };
    }

    private static interface ArtificialEntity extends WithId { }
}
