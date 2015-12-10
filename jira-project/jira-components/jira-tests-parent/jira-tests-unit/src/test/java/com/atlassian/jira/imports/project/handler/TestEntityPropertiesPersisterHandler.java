package com.atlassian.jira.imports.project.handler;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.Executor;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyImpl;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.matchers.ReflectionEqualTo;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.atlassian.jira.imports.project.parser.EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME;

/**
 * @since v6.2
 */
public class TestEntityPropertiesPersisterHandler
{
    private final Executor executor = new ExecutorForTests();
    @Rule
    public TestRule initMock = new InitMockitoMocks(this);
    @Mock
    private ProjectImportPersister projectImportPersister;
    @Mock
    private ProjectImportResults projectImportResults;
    @Mock
    private I18nHelper i18nHelper;
    @Mock
    private SimpleProjectImportIdMapper projectImportIdMapper;
    @Mock
    private SimpleProjectImportIdMapper issueImportIdMapper;
    @Mock
    private EntityPropertiesPersisterHandler propertiesPersisterHandler;

    @Before
    public void setUp()
    {
        propertiesPersisterHandler = new EntityPropertiesPersisterHandler(executor, projectImportResults, projectImportPersister, EntityPropertyType.ISSUE_PROPERTY, issueImportIdMapper);

    }

    @Test
    public void shouldTransformIssuePropertyWithNewIssueId() throws ParseException, AbortImportException
    {
        //having
        final Map<String, String> attributes = createNewEntityProperty(1l, EntityPropertyType.ISSUE_PROPERTY.getDbEntityName());
        final String newIssueId = "123";
        Mockito.when(issueImportIdMapper.getMappedId(attributes.get(EntityProperty.ENTITY_ID))).
                thenReturn(String.valueOf(newIssueId));
        //when
        propertiesPersisterHandler.handleEntity(ENTITY_PROPERTY_ENTITY_NAME, attributes);
        //then
        final Map<String, String> expectedAttributes = Maps.newHashMap(attributes);
        expectedAttributes.put(EntityProperty.ENTITY_ID, newIssueId);

        Mockito.verify(projectImportPersister).createEntity(Mockito.argThat(entityRepresentationMatcher(expectedAttributes)));
    }

    @Test
    public void shouldTransformProjectPropertyWithNewProjectId() throws ParseException, AbortImportException
    {
        //having
        propertiesPersisterHandler = new EntityPropertiesPersisterHandler(executor, projectImportResults, projectImportPersister, EntityPropertyType.PROJECT_PROPERTY, projectImportIdMapper);
        final Map<String, String> attributes = createNewEntityProperty(1l, EntityPropertyType.PROJECT_PROPERTY.getDbEntityName());
        final String newProjectId = "123";
        Mockito.when(projectImportIdMapper.getMappedId(attributes.get(EntityProperty.ENTITY_ID))).thenReturn(newProjectId);
        //when
        propertiesPersisterHandler.handleEntity(ENTITY_PROPERTY_ENTITY_NAME, attributes);

        //then
        final Map<String, String> expectedAttributes = Maps.newHashMap(attributes);
        expectedAttributes.put(EntityProperty.ENTITY_ID, newProjectId);

        Mockito.verify(projectImportPersister).createEntity(Mockito.argThat(entityRepresentationMatcher(expectedAttributes)));
    }

    @Test
    public void shouldNotTransformOtherTransformProjectAndIssueProperties() throws ParseException, AbortImportException
    {
        //having
        final Map<String, String> attributes = createNewEntityProperty(1l, EntityPropertyType.REMOTE_VERSION_LINK.getDbEntityName());

        //when
        propertiesPersisterHandler.handleEntity(ENTITY_PROPERTY_ENTITY_NAME, attributes);
        //then

        Mockito.verify(projectImportPersister, Mockito.never()).createEntity(Mockito.any(EntityRepresentation.class));

    }

    @Test
    public void shouldNotTouchOtherEntitiesThatEntityProperties() throws ParseException, AbortImportException
    {
        //having
        final Map<String, String> attributes = createNewEntityProperty(1l, EntityPropertyType.ISSUE_PROPERTY.getDbEntityName());

        //when
        propertiesPersisterHandler.handleEntity(ENTITY_PROPERTY_ENTITY_NAME + "wrong", attributes);
        //then

        Mockito.verify(projectImportPersister, Mockito.never()).createEntity(Mockito.any(EntityRepresentation.class));
    }

    @Test
    public void shouldHandleNotCreatedIssuePropertyWithReportingError() throws ParseException, AbortImportException
    {
        //having
        final Map<String, String> attributes = createNewEntityProperty(1l, EntityPropertyType.ISSUE_PROPERTY.getDbEntityName());
        final String newIssueId = "12";
        Mockito.when(issueImportIdMapper.getMappedId(attributes.get(EntityProperty.ENTITY_ID))).
                thenReturn(newIssueId);
        Mockito.when(projectImportPersister.createEntity(Mockito.any(EntityRepresentation.class))).thenReturn(null);
        Mockito.when(projectImportResults.getI18n()).thenReturn(i18nHelper);
        final String fancyErrorMessage = "fancyErrorMessage";
        Mockito.when(i18nHelper.getText("admin.errors.project.import.entity.property.error",
                attributes.get(EntityProperty.ID),
                attributes.get(EntityProperty.ENTITY_NAME),
                attributes.get(EntityProperty.ENTITY_ID))).thenReturn(fancyErrorMessage);

        //when
        propertiesPersisterHandler.handleEntity(ENTITY_PROPERTY_ENTITY_NAME, attributes);
        //then
        Mockito.verify(projectImportResults).addError(fancyErrorMessage);
    }

    @Test
    public void shouldNotMapPropertiesThatBelongToNotImportedProject() throws ParseException, AbortImportException
    {
        //having
        final Map<String, String> attributes = createNewEntityProperty(1l, EntityPropertyType.ISSUE_PROPERTY.getDbEntityName());

        //when
        propertiesPersisterHandler.handleEntity(ENTITY_PROPERTY_ENTITY_NAME, attributes);
        //then

        Mockito.verify(projectImportPersister, Mockito.never()).createEntity(Mockito.any(EntityRepresentation.class));

    }

    @Test
    public void shouldThrowParseExceptionWhenFieldIsMissing() throws ParseException, AbortImportException
    {
        final Map<String, String> attributes = createNewEntityProperty(1l, EntityPropertyType.ISSUE_PROPERTY.getDbEntityName());

        //when
        for (final String keyToRemove : attributes.keySet())
        {
            final Map<String, String> brokenAttributes = Maps.newHashMap(attributes);
            brokenAttributes.remove(keyToRemove);
            try
            {
                propertiesPersisterHandler.handleEntity(ENTITY_PROPERTY_ENTITY_NAME, brokenAttributes);
                Assert.fail("Expected ParseException for missing field " + keyToRemove);
            }
            catch (final ParseException exception)
            {
                Assert.assertThat(exception.getMessage(), CoreMatchers.is(CoreMatchers.containsString(keyToRemove)));
            }
        }
        //then
        Mockito.verify(projectImportPersister, Mockito.never()).createEntity(Mockito.any(EntityRepresentation.class));
    }

    private Map<String, String> createNewEntityProperty(Long id, String entityName)
    {
        final Map<String, Object> fieldsMap = Entity.ENTITY_PROPERTY.fieldMapFrom(EntityPropertyImpl.existing(
                id, entityName, 1000l + id, "key" + id, "value" + id, new Timestamp(2000l + id), new Timestamp(3000l + id)
        ));
        return valuesToString(fieldsMap);
    }

    private Map<String, String> valuesToString(final Map<String, Object> arguments)
    {
        return Maps.transformValues(arguments, new Function<Object, String>()
        {
            @Override
            public String apply(final Object input)
            {
                return input.toString();
            }
        });
    }

    private Matcher<EntityRepresentation> entityRepresentationMatcher(final Map<String, String> entityValues)
    {
        return new ReflectionEqualTo<EntityRepresentation>(new EntityRepresentationImpl(ENTITY_PROPERTY_ENTITY_NAME, entityValues));
    }
}
