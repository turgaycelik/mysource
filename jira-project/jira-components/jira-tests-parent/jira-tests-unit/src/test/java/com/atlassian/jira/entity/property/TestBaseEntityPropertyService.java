package com.atlassian.jira.entity.property;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.event.entity.AbstractPropertyEvent;
import com.atlassian.jira.event.entity.EntityPropertyDeletedEvent;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.entity.property.EntityPropertyHelper.CheckPermissionFunction;
import static com.atlassian.jira.entity.property.EntityPropertyService.DeletePropertyValidationResult;
import static com.atlassian.jira.entity.property.EntityPropertyService.EntityPropertyInput;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyKeys;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import static com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import static com.atlassian.jira.util.ErrorCollection.Reason;
import static com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN;
import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_FOUND;
import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_LOGGED_IN;
import static com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
@RunWith (MockitoJUnitRunner.class)
public class TestBaseEntityPropertyService
{
    private static final String PROPERTY_KEY = "property.key";
    private static final String ARTIFICIAL_ENTITY = "ArtificialEntity";
    private static final long ENTITY_ID = 1l;

    @Mock
    public JsonEntityPropertyManager propertyManager;
    @Mock
    public PermissionManager permissionManager;
    @Mock
    public ApplicationUser user;
    @Mock
    public EventPublisher eventPublisher;
    @Mock
    private EntityPropertyHelper<ArtificialEntity> entityPropertyHelper;
    private final I18nHelper i18n = new MockI18nHelper();

    @Rule
    public ExpectedException exception;

    @Test
    public void validationOfCorrectIssuePropertyInput()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasEditPermissionFunction(user, artificialEntity, true)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .build();
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        final SetPropertyValidationResult validationResult =
                entityPropertyService.validateSetProperty(user, ENTITY_ID, new PropertyInput(getJson(), PROPERTY_KEY));

        assertThat(validationResult.isValid(), is(true));
    }

    @Test
    public void validationOfPropertyWithUserWithoutPermissions()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasEditPermissionFunction(user, artificialEntity, false, FORBIDDEN)
                .hasReadPermissionFunction(user, artificialEntity, false, FORBIDDEN)
                .build();
        doNothing().when(propertyManager).putDryRun(any(String.class), any(String.class), any(String.class));
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        SetPropertyValidationResult validationResult =
                entityPropertyService.validateSetProperty(user, ENTITY_ID, new PropertyInput(getJson(), PROPERTY_KEY));

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getErrorCollection().getReasons(), contains(FORBIDDEN));
    }

    @Test
    public void validationOfPropertyWithoutUserLoggedIn()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasEditPermissionFunction(user, artificialEntity, false, NOT_LOGGED_IN)
                .hasReadPermissionFunction(user, artificialEntity, false, NOT_LOGGED_IN)
                .build();
        doNothing().when(propertyManager).putDryRun(any(String.class), any(String.class), any(String.class));
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        SetPropertyValidationResult validationResult =
                entityPropertyService.validateSetProperty(user, ENTITY_ID, new PropertyInput(getJson(), PROPERTY_KEY));

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getErrorCollection().getReasons(), contains(Reason.NOT_LOGGED_IN));
    }

    @Test
    public void validateSetPropertyWithoutPermissionChecks()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasEditPermissionFunction(user, artificialEntity, false, NOT_LOGGED_IN)
                .hasReadPermissionFunction(user, artificialEntity, false, NOT_LOGGED_IN)
                .build();
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);
        EntityPropertyOptions options = new EntityPropertyOptions.Builder().skipPermissionChecks().build();

        SetPropertyValidationResult validationResult =
                entityPropertyService.validateSetProperty(user, ENTITY_ID, new PropertyInput(getJson(), PROPERTY_KEY), options);

        assertThat(validationResult.isValid(), is(true));
    }

    @Test
    public void validationOfPropertyWithInvalidJson()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasEditPermissionFunction(user, artificialEntity, true)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .build();
        doThrow(InvalidJsonPropertyException.class).when(propertyManager).putDryRun(any(String.class), any(String.class), any(String.class));
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        SetPropertyValidationResult validationResult =
                entityPropertyService.validateSetProperty(user, ENTITY_ID, new PropertyInput(getJson(), PROPERTY_KEY));

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getErrorCollection().getErrorMessages(), contains(startsWith("jira.properties.service.invalid.json")));
    }

    @Test
    public void validationOfPropertyWithTooLongJson()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasEditPermissionFunction(user, artificialEntity, true)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .build();
        doThrow(new FieldTooLongJsonPropertyException("x", 10, 5)).when(propertyManager).putDryRun(any(String.class), any(String.class), any(String.class));
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        SetPropertyValidationResult validationResult =
                entityPropertyService.validateSetProperty(user, ENTITY_ID, new PropertyInput(getJson(), PROPERTY_KEY));

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getErrorCollection().getErrorMessages(), contains(startsWith("jira.properties.service.too.long.value")));
    }

    @Test
    public void validationOfPropertyWithoutExistingEntity()
    {
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, null)
                .build();
        doNothing().when(propertyManager).putDryRun(any(String.class), any(String.class), any(String.class));
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        SetPropertyValidationResult validationResult =
                entityPropertyService.validateSetProperty(user, ENTITY_ID, new PropertyInput(getJson(), PROPERTY_KEY));

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getErrorCollection().getErrorMessages(), contains(Matchers.startsWith("jira.properties.service.entity.does.not.exist")));
    }

    @Test
    public void validationOfPropertyWithEmptyValue()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasEditPermissionFunction(user, artificialEntity, true)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .build();
        doNothing().when(propertyManager).putDryRun(any(String.class), any(String.class), any(String.class));
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        final SetPropertyValidationResult validationResult =
                entityPropertyService.validateSetProperty(user, ENTITY_ID, new PropertyInput(StringUtils.EMPTY, PROPERTY_KEY));

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getErrorCollection().getErrorMessages(), contains("jira.properties.service.empty.value"));
        assertThat(validationResult.getErrorCollection().getReasons(), contains(VALIDATION_FAILED));
    }

    @Test
    public void settingIssueProperty()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasEditPermissionFunction(user, artificialEntity, true)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .build();
        EntityProperty entityProperty = EntityPropertyImpl.forCreate(ARTIFICIAL_ENTITY, ENTITY_ID, PROPERTY_KEY, getJson());
        when(propertyManager.get(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID), eq(PROPERTY_KEY))).thenReturn(entityProperty);
        EntityPropertyInput entityPropertyInput = new EntityPropertyInput(getJson(), PROPERTY_KEY, ENTITY_ID, ARTIFICIAL_ENTITY);
        SetPropertyValidationResult validationResult = new SetPropertyValidationResult(new SimpleErrorCollection(), Option.some(entityPropertyInput));

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        PropertyResult propertyResult = entityPropertyService.setProperty(user, validationResult);

        assertThat(propertyResult.isValid(), is(true));
        assertThat(propertyResult.getEntityProperty().isDefined(), is(true));

        assertThat(propertyResult.getEntityProperty().get().getEntityName(), is(ARTIFICIAL_ENTITY));
        assertThat(propertyResult.getEntityProperty().get().getEntityId(), is(ENTITY_ID));
        assertThat(propertyResult.getEntityProperty().get().getKey(), is(PROPERTY_KEY));
        assertThat(propertyResult.getEntityProperty().get().getValue(), is(getJson()));
    }

    @Test
    public void gettingProperty()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .build();
        EntityProperty entityProperty = EntityPropertyImpl.forCreate(ARTIFICIAL_ENTITY, ENTITY_ID, PROPERTY_KEY, getJson());
        when(propertyManager.get(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID), eq(PROPERTY_KEY))).thenReturn(entityProperty);

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        PropertyResult property = entityPropertyService.getProperty(user, ENTITY_ID, PROPERTY_KEY);

        assertThat(property.getEntityProperty().isDefined(), is(true));
        assertThat(property.getEntityProperty().get().getEntityName(), is(ARTIFICIAL_ENTITY));
        assertThat(property.getEntityProperty().get().getEntityId(), is(ENTITY_ID));
        assertThat(property.getEntityProperty().get().getKey(), is(PROPERTY_KEY));
        assertThat(property.getEntityProperty().get().getValue(), is(getJson()));
    }

    @Test
    public void gettingPropertyWithoutPermissions()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, false, FORBIDDEN)
                .build();

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        PropertyResult property = entityPropertyService.getProperty(user, ENTITY_ID, PROPERTY_KEY);
        assertThat(property.isValid(), is(false));
        assertThat(property.getErrorCollection().getReasons(), contains(FORBIDDEN));
    }

    @Test
    public void gettingPropertyWithoutExistingEntity()
    {
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, null)
                .build();

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        PropertyResult property = entityPropertyService.getProperty(user, ENTITY_ID, PROPERTY_KEY);
        assertThat(property.isValid(), is(false));
        assertThat(property.getEntityProperty().isEmpty(), is(true));
        assertThat(property.getErrorCollection().getReasons(), contains(NOT_FOUND));
    }

    @Test
    public void gettingNotExistingProperty()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .build();
        when(propertyManager.get(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID), eq(PROPERTY_KEY))).thenReturn(null);

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        PropertyResult property = entityPropertyService.getProperty(user, ENTITY_ID, PROPERTY_KEY);
        assertThat(property.isValid(), is(true));
        assertThat(property.getEntityProperty().isEmpty(), is(true));
    }

    @Test
    public void gettingPropertyWithoutPermissionChecks()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, false)
                .build();
        EntityProperty entityProperty = EntityPropertyImpl.forCreate(ARTIFICIAL_ENTITY, ENTITY_ID, PROPERTY_KEY, getJson());
        when(propertyManager.get(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID), eq(PROPERTY_KEY))).thenReturn(entityProperty);

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);
        EntityPropertyOptions options = new EntityPropertyOptions.Builder().skipPermissionChecks().build();

        PropertyResult property = entityPropertyService.getProperty(user, ENTITY_ID, PROPERTY_KEY, options);

        assertThat(property.getEntityProperty().isDefined(), is(true));
        assertThat(property.getEntityProperty().get().getEntityName(), is(ARTIFICIAL_ENTITY));
        assertThat(property.getEntityProperty().get().getEntityId(), is(ENTITY_ID));
        assertThat(property.getEntityProperty().get().getKey(), is(PROPERTY_KEY));
        assertThat(property.getEntityProperty().get().getValue(), is(getJson()));
    }

    @Test
    public void validationOfExistingPropertyDelete()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .hasEditPermissionFunction(user, artificialEntity, true)
                .build();
        EntityProperty entityProperty = mock(EntityProperty.class);
        when(entityProperty.getEntityId()).thenReturn(ENTITY_ID);
        when(entityProperty.getKey()).thenReturn(PROPERTY_KEY);
        when(propertyManager.get(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID), eq(PROPERTY_KEY))).thenReturn(entityProperty);

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        DeletePropertyValidationResult property = entityPropertyService.validateDeleteProperty(user, ENTITY_ID, PROPERTY_KEY);

        assertThat(property.isValid(), is(true));
        assertThat(property.getEntityProperty().isDefined(), is(true));
        assertThat(property.getEntityProperty().get().getEntityId(), is(ENTITY_ID));
        assertThat(property.getEntityProperty().get().getKey(), is(PROPERTY_KEY));
    }

    @Test
    public void validationOfNotExistingPropertyDelete()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .hasEditPermissionFunction(user, artificialEntity, true)
                .build();
        when(propertyManager.get(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID), eq(PROPERTY_KEY))).thenReturn(null);

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        DeletePropertyValidationResult validationResult = entityPropertyService.validateDeleteProperty(user, ENTITY_ID, PROPERTY_KEY);

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getErrorCollection().getReasons(), contains(Reason.NOT_FOUND));
    }

    @Test
    public void validationOfPropertyDeleteWithoutEditPermissions()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .hasEditPermissionFunction(user, artificialEntity, false, FORBIDDEN)
                .build();

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        DeletePropertyValidationResult validationResult = entityPropertyService.validateDeleteProperty(user, ENTITY_ID, PROPERTY_KEY);

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getErrorCollection().getReasons(), contains(Reason.FORBIDDEN));
    }

    @Test
    public void validationOfPropertyDeleteWithoutExistingEntity()
    {
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, null)
                .build();

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        DeletePropertyValidationResult validationResult = entityPropertyService.validateDeleteProperty(user, ENTITY_ID, PROPERTY_KEY);

        assertThat(validationResult.isValid(), is(false));
        assertThat(validationResult.getErrorCollection().getReasons(), contains(Reason.NOT_FOUND));
    }

    @Test
    public void validationOfExistingPropertyDeleteWithoutPermissionChecks()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, false)
                .hasEditPermissionFunction(user, artificialEntity, false)
                .build();
        EntityProperty entityProperty = mock(EntityProperty.class);
        when(entityProperty.getEntityId()).thenReturn(ENTITY_ID);
        when(entityProperty.getKey()).thenReturn(PROPERTY_KEY);
        when(propertyManager.get(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID), eq(PROPERTY_KEY))).thenReturn(entityProperty);

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);
        EntityPropertyOptions options = new EntityPropertyOptions.Builder().skipPermissionChecks().build();

        DeletePropertyValidationResult property = entityPropertyService.validateDeleteProperty(user, ENTITY_ID, PROPERTY_KEY, options);

        assertThat(property.isValid(), is(true));
        assertThat(property.getEntityProperty().isDefined(), is(true));
        assertThat(property.getEntityProperty().get().getEntityId(), is(ENTITY_ID));
        assertThat(property.getEntityProperty().get().getKey(), is(PROPERTY_KEY));
    }

    @Test
    public void deletionOfProperty()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .hasEditPermissionFunction(user, artificialEntity, true)
                .build();

        EntityProperty entityProperty = EntityPropertyImpl.forCreate(ARTIFICIAL_ENTITY, ENTITY_ID, PROPERTY_KEY, getJson());
        when(propertyManager.get(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID), eq(PROPERTY_KEY))).thenReturn(entityProperty);

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService =
                getService(entityPropertyHelper);

        DeletePropertyValidationResult validationResult = entityPropertyService.validateDeleteProperty(user, ENTITY_ID, PROPERTY_KEY);

        entityPropertyService.deleteProperty(user, validationResult);
        verify(propertyManager, times(1)).delete(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID), eq(PROPERTY_KEY));

        ArgumentCaptor<ArtificationEntityPropertyDeletedEvent> argumentCaptor = ArgumentCaptor.forClass(ArtificationEntityPropertyDeletedEvent.class);
        verify(eventPublisher, times(1)).publish(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getEntityProperty().getEntityId(), is(ENTITY_ID));
        assertThat(argumentCaptor.getValue().getEntityProperty().getKey(), is(PROPERTY_KEY));
    }

    @Test
    public void gettingAllKeysOfEntityProperties()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, true)
                .build();
        when(propertyManager.findKeys(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID))).thenReturn(Lists.newArrayList("property.key.1", "property.key.2", "property.key.3"));
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService = getService(entityPropertyHelper);

        PropertyKeys<ArtificialEntity> propertyKeys = entityPropertyService.getPropertiesKeys(user, ENTITY_ID);

        assertThat(propertyKeys.isValid(), is(true));
        assertThat(propertyKeys.getKeys(), Matchers.<String>hasItem("property.key.1"));
        assertThat(propertyKeys.getKeys(), Matchers.<String>hasItem("property.key.2"));
        assertThat(propertyKeys.getKeys(), Matchers.<String>hasItem("property.key.3"));
    }

    @Test
    public void gettingAllKeysForNotExistingEntity()
    {
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, null)
                .build();

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService = getService(entityPropertyHelper);

        PropertyKeys<ArtificialEntity> propertyKeys = entityPropertyService.getPropertiesKeys(user, ENTITY_ID);

        assertThat(propertyKeys.isValid(), is(false));
        assertThat(propertyKeys.getErrorCollection().getReasons(), contains(Reason.NOT_FOUND));
    }

    @Test
    public void gettingAllKeysWithoutPermissions()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, false, FORBIDDEN)
                .build();

        BaseEntityPropertyService<ArtificialEntity> entityPropertyService = getService(entityPropertyHelper);

        PropertyKeys<ArtificialEntity> propertyKeys = entityPropertyService.getPropertiesKeys(user, ENTITY_ID);

        assertThat(propertyKeys.isValid(), is(false));
        assertThat(propertyKeys.getErrorCollection().getReasons(), contains(Reason.FORBIDDEN));
    }

    @Test
    public void gettingAllKeysOfEntityPropertiesWithoutPermissionChecks()
    {
        ArtificialEntity artificialEntity = mock(ArtificialEntity.class);
        EntityPropertyHelper<ArtificialEntity> entityPropertyHelper = new ArtificialEntityPropertyHelperBuilder()
                .entityById(ENTITY_ID, artificialEntity)
                .hasReadPermissionFunction(user, artificialEntity, false)
                .build();
        when(propertyManager.findKeys(eq(ARTIFICIAL_ENTITY), eq(ENTITY_ID))).thenReturn(Lists.newArrayList("property.key.1", "property.key.2", "property.key.3"));
        BaseEntityPropertyService<ArtificialEntity> entityPropertyService = getService(entityPropertyHelper);
        EntityPropertyOptions options = new EntityPropertyOptions.Builder().skipPermissionChecks().build();

        PropertyKeys<ArtificialEntity> propertyKeys = entityPropertyService.getPropertiesKeys(user, ENTITY_ID, options);

        assertThat(propertyKeys.isValid(), is(true));
        assertThat(propertyKeys.getKeys(), Matchers.<String>hasItem("property.key.1"));
        assertThat(propertyKeys.getKeys(), Matchers.<String>hasItem("property.key.2"));
        assertThat(propertyKeys.getKeys(), Matchers.<String>hasItem("property.key.3"));
    }

    private BaseEntityPropertyService<ArtificialEntity> getService(final EntityPropertyHelper<ArtificialEntity> entityPropertyHelper)
    {
        EntityPropertyType entityPropertyType = mock(EntityPropertyType.class);
        when(entityPropertyType.getI18nKeyForEntityName()).thenReturn(ARTIFICIAL_ENTITY);
        when(entityPropertyType.getDbEntityName()).thenReturn(ARTIFICIAL_ENTITY);
        when(entityPropertyHelper.getEntityPropertyType()).thenReturn(entityPropertyType);
        return new BaseEntityPropertyService<ArtificialEntity>(propertyManager, i18n, eventPublisher, entityPropertyHelper);
    }

    private String getJson()
    {
        return new JSONObject(ImmutableMap.of("x", "y")).toString();
    }

    static interface ArtificialEntity extends WithId
    {
    }

    @SuppressWarnings ("unchecked")
    private static class ArtificialEntityPropertyHelperBuilder
    {
        private Function<Long, Option<ArtificialEntity>> entityByIdFunc;
        private CheckPermissionFunction editPermissionFunction;
        private CheckPermissionFunction readPermissionFunction;

        public ArtificialEntityPropertyHelperBuilder entityById(final long entityId, final ArtificialEntity artificialEntity)
        {
            this.entityByIdFunc = mock(Function.class);
            when(entityByIdFunc.apply(eq(entityId))).thenReturn(Option.option(artificialEntity));
            if (artificialEntity != null)
            {
                when(artificialEntity.getId()).thenReturn(entityId);
            }
            return this;
        }

        public ArtificialEntityPropertyHelperBuilder hasEditPermissionFunction(final ApplicationUser user, final ArtificialEntity artificialEntity,
                boolean permissionCheckResult, Reason... reasons)
        {
            this.editPermissionFunction = buildPermissionFunction(user, artificialEntity, permissionCheckResult, reasons);
            return this;
        }

        public ArtificialEntityPropertyHelperBuilder hasReadPermissionFunction(final ApplicationUser user, final ArtificialEntity artificialEntity,
                boolean permissionCheckResult, Reason... reasons)
        {
            this.readPermissionFunction = buildPermissionFunction(user, artificialEntity, permissionCheckResult, reasons);
            return this;
        }

        private CheckPermissionFunction buildPermissionFunction(final ApplicationUser user, final ArtificialEntity artificialEntity,
                boolean permissionCheckResult, Reason... reasons)
        {
            CheckPermissionFunction permissionFunction = mock(CheckPermissionFunction.class);
            if (!permissionCheckResult)
            {
                final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
                errorCollection.addErrorMessage("error.message");
                errorCollection.addReasons(Sets.newHashSet(reasons));
                when(permissionFunction.apply(eq(user), eq(artificialEntity))).thenReturn(errorCollection);
            }
            else
            {
                when(permissionFunction.apply(eq(user), eq(artificialEntity))).thenReturn(new SimpleErrorCollection());
            }
            return permissionFunction;
        }

        public EntityPropertyHelper<ArtificialEntity> build()
        {
            EntityPropertyHelper<ArtificialEntity> entityEntityPropertyHelper = mock(EntityPropertyHelper.class);
            if (entityByIdFunc != null)
            {
                when(entityEntityPropertyHelper.getEntityByIdFunction()).thenReturn(entityByIdFunc);
            }
            if (editPermissionFunction != null)
            {
                when(entityEntityPropertyHelper.hasEditPermissionFunction()).thenReturn(editPermissionFunction);
            }
            if (readPermissionFunction != null)
            {
                when(entityEntityPropertyHelper.hasReadPermissionFunction()).thenReturn(readPermissionFunction);
            }
            Function2<ApplicationUser, EntityProperty, ? extends EntityPropertySetEvent> setEventFunction = new Function2<ApplicationUser, EntityProperty, ArtificationEntityPropertySetEvent>()
            {
                @Override
                public ArtificationEntityPropertySetEvent apply(final ApplicationUser arg1, final EntityProperty arg2)
                {
                    return new ArtificationEntityPropertySetEvent(arg2, arg1);
                }
            };
            doReturn(setEventFunction).when(entityEntityPropertyHelper).createSetPropertyEventFunction();

            Function2<ApplicationUser, EntityProperty, ? extends EntityPropertyDeletedEvent> deleteEventFunction = new Function2<ApplicationUser, EntityProperty, EntityPropertyDeletedEvent>()
            {
                @Override
                public EntityPropertyDeletedEvent apply(final ApplicationUser arg1, final EntityProperty arg2)
                {
                    return new ArtificationEntityPropertyDeletedEvent(arg2, arg1);
                }
            };
            doReturn(deleteEventFunction).when(entityEntityPropertyHelper).createDeletePropertyEventFunction();
            return entityEntityPropertyHelper;
        }
    }

    private static class ArtificationEntityPropertySetEvent extends AbstractPropertyEvent
            implements EntityPropertySetEvent
    {
        public ArtificationEntityPropertySetEvent(final EntityProperty entityProperty, final ApplicationUser user)
        {
            super(entityProperty, user);
        }
    }

    private static class ArtificationEntityPropertyDeletedEvent extends AbstractPropertyEvent
            implements EntityPropertyDeletedEvent
    {
        public ArtificationEntityPropertyDeletedEvent(final EntityProperty entityProperty, final ApplicationUser user)
        {
            super(entityProperty, user);
        }
    }
}
