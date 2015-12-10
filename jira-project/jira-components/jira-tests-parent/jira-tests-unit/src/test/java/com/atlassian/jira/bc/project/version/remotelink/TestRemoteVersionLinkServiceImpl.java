package com.atlassian.jira.bc.project.version.remotelink;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.DeleteValidationResult;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.PutValidationResult;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.RemoteVersionLinkListResult;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.RemoteVersionLinkResult;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityEngineImpl;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyImpl;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.entity.property.JsonEntityPropertyManagerImpl;
import com.atlassian.jira.event.project.RemoteVersionLinkDeleteEvent;
import com.atlassian.jira.event.project.RemoteVersionLinkPutEvent;
import com.atlassian.jira.event.project.VersionDeleteEvent;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.base.Function;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkServiceImpl.REMOTE_VERSION_LINK;
import static com.atlassian.jira.entity.Entity.Name.ENTITY_PROPERTY;
import static com.atlassian.jira.entity.property.EntityProperty.ENTITY_ID;
import static com.atlassian.jira.entity.property.EntityProperty.ENTITY_NAME;
import static com.atlassian.jira.entity.property.EntityProperty.KEY;
import static com.atlassian.jira.mock.Strict.strict;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v6.1.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestRemoteVersionLinkServiceImpl
{
    private static final Pattern REGEX_UUID = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    @Mock PermissionManager permissionManager;
    @Mock VersionManager versionManager;
    EventPublisher eventPublisher = mock(EventPublisher.class, strict());
    ApplicationUser fred = new MockApplicationUser("Fred");

    // Conditionally mocked depending on the needed test setup
    EntityEngine entityEngine;
    OfBizDelegator ofBizDelegator;
    JsonEntityPropertyManager jsonEntityPropertyManager;

    // Under test
    RemoteVersionLinkServiceImpl remoteVersionLinkService;

    @After
    public void tearDown()
    {
        fred = null;
        ofBizDelegator = null;
        entityEngine = null;
        jsonEntityPropertyManager = null;
        permissionManager = null;
        versionManager = null;
        eventPublisher = null;
        remoteVersionLinkService = null;
    }

    private void mockOfBizDelegator()
    {
        ofBizDelegator = mock(OfBizDelegator.class);
        entityEngine = new EntityEngineImpl(ofBizDelegator);
        jsonEntityPropertyManager = new JsonEntityPropertyManagerImpl(entityEngine, eventPublisher);
        remoteVersionLinkService = new RemoteVersionLinkServiceImpl(
                new JsonEntityPropertyManagerImpl(entityEngine, eventPublisher),
                permissionManager, versionManager, eventPublisher,
                new MockI18nBean.MockI18nBeanFactory());
    }

    private void mockEntityEngine()
    {
        fred = new MockApplicationUser("Fred");
        ofBizDelegator = null;
        entityEngine = mock(EntityEngine.class, strict());
        jsonEntityPropertyManager = new JsonEntityPropertyManagerImpl(entityEngine, eventPublisher);
        remoteVersionLinkService = new RemoteVersionLinkServiceImpl(
                new JsonEntityPropertyManagerImpl(entityEngine, eventPublisher),
                permissionManager, versionManager, eventPublisher,
                new MockI18nBean.MockI18nBeanFactory());
    }

    private void mockJsonEntityPropertyManager()
    {
        fred = new MockApplicationUser("Fred");
        ofBizDelegator = null;
        entityEngine = null;
        jsonEntityPropertyManager = mock(JsonEntityPropertyManager.class, strict());
        remoteVersionLinkService = new RemoteVersionLinkServiceImpl(
                jsonEntityPropertyManager,
                permissionManager, versionManager, eventPublisher,
                new MockI18nBean.MockI18nBeanFactory());
    }



    @Test
    public void testGetRemoteVersionLinksByVersionIdBadRequest()
    {
        mockJsonEntityPropertyManager();

        final RemoteVersionLinkListResult result = remoteVersionLinkService.getRemoteVersionLinksByVersionId(fred, null);

        assertError(result, "versionId", "'versionId' is required.");
    }

    @Test
    public void testGetRemoteVersionLinksByVersionIdNoSuchVersion()
    {
        mockJsonEntityPropertyManager();

        final RemoteVersionLinkListResult result = remoteVersionLinkService.getRemoteVersionLinksByVersionId(fred, 42L);

        assertError(result, "versionId", "Version with id '42' does not exist.");
    }

    @Test
    public void testGetRemoteVersionLinksByVersionIdNoAccess()
    {
        mockJsonEntityPropertyManager();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);

        final RemoteVersionLinkListResult result = remoteVersionLinkService.getRemoteVersionLinksByVersionId(
                fred, version.getId());

        assertError(result, "versionId", "Version with id '2' does not exist.");
    }

    @Test
    public void testGetRemoteVersionLinksByVersionIdWithNoResults()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, fred)).thenReturn(true);
        mockEntityEngineSelect(selectQuery(EntityProperty.class,
                "entityName=EntityProperty,entityBuilder=EntityPropertyFactory",
                "entityId=2",
                "entityName=RemoteVersionLink"),
                Collections.<EntityProperty>emptyList());

        final RemoteVersionLinkListResult result = remoteVersionLinkService.getRemoteVersionLinksByVersionId(
                fred, version.getId());

        assertValid(result);
        assertThat(result.getRemoteVersionLinks(), Matchers.<RemoteVersionLink>empty());
    }

    @Test
    public void testGetRemoteVersionLinksByVersionIdWithResults()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, fred)).thenReturn(true);
        final Timestamp now = new Timestamp(System.currentTimeMillis());
        mockEntityEngineSelect(selectQuery(EntityProperty.class,
                "entityName=EntityProperty,entityBuilder=EntityPropertyFactory",
                "entityId=2",
                "entityName=RemoteVersionLink"),
                Arrays.<EntityProperty>asList(
                        EntityPropertyImpl.existing(3L, REMOTE_VERSION_LINK, version.getId(), "key1", "\"value1\"", now,
                                now),
                        EntityPropertyImpl.existing(4L, REMOTE_VERSION_LINK, version.getId(), "key2", "\"value2\"", now,
                                now)
                ));

        final RemoteVersionLinkListResult result = remoteVersionLinkService.getRemoteVersionLinksByVersionId(
                fred, version.getId());

        assertValid(result);
        assertEquals(Arrays.<RemoteVersionLink>asList(
                new RemoteVersionLinkImpl(version, "key1", "\"value1\""),
                new RemoteVersionLinkImpl(version, "key2", "\"value2\"")),
                result.getRemoteVersionLinks());
    }

    @Test
    public void testGetRemoteVersionLinkByVersionIdAndGlobalIdBadRequest()
    {
        mockJsonEntityPropertyManager();
        assertError(
                remoteVersionLinkService.getRemoteVersionLinkByVersionIdAndGlobalId(fred, null, "MyGlobalId"),
                "versionId",
                "'versionId' is required.");
        assertError(
                remoteVersionLinkService.getRemoteVersionLinkByVersionIdAndGlobalId(fred, 42L, null),
                "globalId",
                "'globalId' is required.");
        assertError(
                remoteVersionLinkService.getRemoteVersionLinkByVersionIdAndGlobalId(fred, 42L, "  "),
                "globalId",
                "'globalId' is required.");
    }

    @Test
    public void testGetRemoteVersionLinkByVersionIdAndGlobalIdNoSuchVersion()
    {
        mockJsonEntityPropertyManager();
        final RemoteVersionLinkResult result = remoteVersionLinkService.getRemoteVersionLinkByVersionIdAndGlobalId(
                fred, 42L, "MyGlobalId");

        assertError(result, "versionId", "Version with id '42' does not exist.");
    }

    @Test
    public void testGetRemoteVersionLinkByVersionIdAndGlobalIdNoAccess()
    {
        mockJsonEntityPropertyManager();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);

        final RemoteVersionLinkResult result = remoteVersionLinkService.getRemoteVersionLinkByVersionIdAndGlobalId(
                fred, 2L, "MyGlobalId");

        assertError(result, "versionId", "Version with id '2' does not exist.");
    }

    @Test
    public void testGetRemoteVersionLinksByGlobalIdWithNoResults()
    {
        mockEntityEngine();
        final Project project1 = mockProject(1L);
        final Project project2 = mockProject(2L);
        final Version version1 = mockVersion(3L, project1);
        final Version version2 = mockVersion(4L, project2);
        final Version version3 = mockVersion(5L, project2);
        when(versionManager.getVersion(version1.getId())).thenReturn(version1);
        when(versionManager.getVersion(version2.getId())).thenReturn(version2);
        when(versionManager.getVersion(version3.getId())).thenReturn(version3);
        when(permissionManager.hasPermission(Permissions.BROWSE, project2, fred)).thenReturn(true);
        mockEntityEngineSelect(selectQuery(EntityProperty.class,
                "entityName=EntityProperty,entityBuilder=EntityPropertyFactory",
                "propertyKey=MyGlobalId",
                "entityName=RemoteVersionLink"),
                Collections.<EntityProperty>emptyList());

        final RemoteVersionLinkListResult result = remoteVersionLinkService.getRemoteVersionLinksByGlobalId(fred,
                "MyGlobalId");

        assertValid(result);
        assertThat(result.getRemoteVersionLinks(), Matchers.<RemoteVersionLink>empty());
    }

    @Test
    public void testGetRemoteVersionLinksByGlobalIdWithSomeResultsNotVisible()
    {
        mockEntityEngine();
        final Project project1 = mockProject(1L);
        final Project project2 = mockProject(2L);
        final Version version1 = mockVersion(3L, project1);
        final Version version2 = mockVersion(4L, project2);
        final Version version3 = mockVersion(5L, project2);
        final Version version4 = mockVersion(6L, project1);
        when(versionManager.getVersion(version1.getId())).thenReturn(version1);
        when(versionManager.getVersion(version2.getId())).thenReturn(version2);
        when(versionManager.getVersion(version3.getId())).thenReturn(version3);
        when(versionManager.getVersion(version4.getId())).thenReturn(version4);
        when(permissionManager.hasPermission(Permissions.BROWSE, project2, fred)).thenReturn(true);
        final Timestamp now = new Timestamp(System.currentTimeMillis());

        Matcher<SelectQuery<EntityProperty>> query = selectQuery(EntityProperty.class,
                "entityName=EntityProperty,entityBuilder=EntityPropertyFactory",
                "propertyKey=MyGlobalId",
                "entityName=RemoteVersionLink");
        mockEntityEngineCount(query, 4L);

        final long totalCount = remoteVersionLinkService.getRemoteVersionLinkCountByGlobalId("MyGlobalId");

        mockEntityEngineSelect(query, Arrays.<EntityProperty>asList(
                EntityPropertyImpl.existing(91L, REMOTE_VERSION_LINK, version1.getId(), "MyGlobalId",
                        "\"value1\"", now, now),
                EntityPropertyImpl.existing(92L, REMOTE_VERSION_LINK, version2.getId(), "MyGlobalId",
                        "\"value2\"", now, now),
                EntityPropertyImpl.existing(93L, REMOTE_VERSION_LINK, version3.getId(), "MyGlobalId",
                        "\"value3\"", now, now),
                EntityPropertyImpl.existing(94L, REMOTE_VERSION_LINK, version4.getId(), "MyGlobalId",
                        "\"value4\"", now, now)
        ));
        final RemoteVersionLinkListResult result = remoteVersionLinkService.getRemoteVersionLinksByGlobalId(fred, "MyGlobalId");

        // version1 and version4 should be filtered out, because they belong to project1, which Fred can't browse.
        assertEquals("The count does not perform a permission check", totalCount, 4L);
        assertValid(result);
        assertEquals(Arrays.<RemoteVersionLink>asList(
                new RemoteVersionLinkImpl(version2, "MyGlobalId", "\"value2\""),
                new RemoteVersionLinkImpl(version3, "MyGlobalId", "\"value3\"")),
                result.getRemoteVersionLinks());
    }

    @Test
    public void testValidatePutBadRequest()
    {
        mockJsonEntityPropertyManager();

        PutValidationResult result = remoteVersionLinkService.validatePut(fred, null, "MyGlobalId", "\"value\"");
        assertError(result, "versionId", "'versionId' is required.");

        result = remoteVersionLinkService.validatePut(fred, 42L, "MyGlobalId", null);
        assertError(result, "json", "'json' is required.");

        result = remoteVersionLinkService.validatePut(fred, 42L, "MyGlobalId", "  ");
        assertError(result, "json", "'json' is required.");
    }

    @Test
    public void testValidatePutNoSuchVersion()
    {
        mockJsonEntityPropertyManager();

        final PutValidationResult result = remoteVersionLinkService.validatePut(fred, 42L, "MyGlobalId", "\"value\"");

        assertError(result, "versionId", "Version with id '42' does not exist.");
    }

    @Test
    public void testValidatePutNoAccessToVersion()
    {
        mockJsonEntityPropertyManager();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);

        final PutValidationResult result = remoteVersionLinkService.validatePut(
                fred, version.getId(), "MyGlobalId", "\"value\"");

        assertError(result, "versionId", "Version with id '2' does not exist.");
    }

    @Test
    public void testValidatePutNotProjectAdmin()
    {
        mockJsonEntityPropertyManager();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, fred)).thenReturn(true);

        final PutValidationResult result = remoteVersionLinkService.validatePut(
                fred, version.getId(), "MyGlobalId", "\"value\"");

        assertErrorMessage(result, "You do not have permission to manage the remote version links for project 'KEY1'.");
    }

    @Test
    public void testValidatePutInvalidJson()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);

        final PutValidationResult result = remoteVersionLinkService.validatePut(
                fred, version.getId(), "MyGlobalId", "{{}}");

        assertError(result, "json", allOf(
                containsString("InvalidJsonPropertyException"),
                containsString("Unexpected character ('{'"),
                containsString("was expecting double-quote")));
    }

    @Test
    public void testValidatePutWithGlobalIdSupplied()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);

        final PutValidationResult result = remoteVersionLinkService.validatePut(fred, version.getId(), "MyGlobalId", "\"value\"");

        assertValid(result);
        assertEquals(version.getId(), result.version.getId());
        assertEquals("MyGlobalId", result.globalId);
        assertEquals("\"value\"", result.json);
    }

    @Test
    public void testValidatePutWithGlobalIdSuppliedIgnoringJson()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);
        final String json = "{\"globalId\" : \"this should get ignored\"}";

        final PutValidationResult result = remoteVersionLinkService.validatePut(fred, version.getId(), "MyGlobalId", json);

        assertValid(result);
        assertEquals(version.getId(), result.version.getId());
        assertEquals("MyGlobalId", result.globalId);
        assertEquals(json, result.json);
    }

    @Test
    public void testValidatePutWithGlobalIdExtractedFromJson()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);
        final String json = "{\"globalId\" : \"MyGlobalId\"}";

        final PutValidationResult result = remoteVersionLinkService.validatePut(fred, version.getId(), "  ", json);

        assertValid(result);
        assertEquals(version.getId(), result.version.getId());
        assertEquals("MyGlobalId", result.globalId);
        assertEquals(json, result.json);
    }

    @Test
    public void testValidatePutWithGlobalIdGenerated()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);

        final PutValidationResult result = remoteVersionLinkService.validatePut(fred, version.getId(), "  ", "{}");

        assertValid(result);
        assertEquals(version.getId(), result.version.getId());
        assertUuid(result.globalId);
        assertEquals("{}", result.json);
    }

    @Test
    public void testValidatePutWithGlobalIdGeneratedBecauseJsonHadNonTextForIt()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);
        final String json = "{\"globalId\" : 42}";

        final PutValidationResult result = remoteVersionLinkService.validatePut(fred, version.getId(), "  ", json);

        assertValid(result);
        assertEquals(version.getId(), result.version.getId());
        assertUuid(result.globalId);
        assertEquals(json, result.json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullValidationResult()
    {
        mockJsonEntityPropertyManager();
        remoteVersionLinkService.put(fred, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithInvalidValidationResult()
    {
        mockJsonEntityPropertyManager();
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Lies!");
        remoteVersionLinkService.put(fred, new PutValidationResult(errors));
    }

    @Test
    public void testPut()
    {
        mockJsonEntityPropertyManager();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        doNothing().when(jsonEntityPropertyManager).put(REMOTE_VERSION_LINK, version.getId(), "MyGlobalId", "\"value\"");
        doNothing().when(eventPublisher).publish(new RemoteVersionLinkPutEvent(version, "MyGlobalId"));

        final RemoteVersionLinkResult result = remoteVersionLinkService.put(
                fred, new PutValidationResult(version, "MyGlobalId", "\"value\""));

        verify(jsonEntityPropertyManager).put(REMOTE_VERSION_LINK, version.getId(), "MyGlobalId", "\"value\"");
        assertValid(result);
        final RemoteVersionLink link = result.getRemoteVersionLink();
        assertEquals(version.getId(), link.getEntityId());
        assertEquals("MyGlobalId", link.getGlobalId());
        assertEquals("\"value\"", link.getJsonString());
    }

    @Test
    public void testValidateDeleteBadRequest()
    {
        mockJsonEntityPropertyManager();

        DeleteValidationResult result = remoteVersionLinkService.validateDeleteByVersionId(fred, null);
        assertError(result, "versionId", "'versionId' is required.");

        result = remoteVersionLinkService.validateDelete(fred, null, "MyGlobalId");
        assertError(result, "versionId", "'versionId' is required.");

        result = remoteVersionLinkService.validateDelete(fred, 42L, null);
        assertError(result, "globalId", "'globalId' is required.");

        result = remoteVersionLinkService.validateDelete(fred, 42L, "  ");
        assertError(result, "globalId", "'globalId' is required.");
    }


    @Test
    public void testValidateDeleteNoAccessToVersion()
    {
        mockJsonEntityPropertyManager();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);

        DeleteValidationResult result = remoteVersionLinkService.validateDeleteByVersionId(fred, version.getId());

        assertError(result, "versionId", "Version with id '2' does not exist.");

        // Specifying the globalId shouldn't change anything
        result = remoteVersionLinkService.validateDelete(fred, version.getId(), "MyGlobalId");

        assertError(result, "versionId", "Version with id '2' does not exist.");
    }

    @Test
    public void testValidateDeleteNotProjectAdmin()
    {
        mockJsonEntityPropertyManager();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, fred)).thenReturn(true);

        DeleteValidationResult result = remoteVersionLinkService.validateDeleteByVersionId(fred, version.getId());

        assertErrorMessage(result, "You do not have permission to manage the remote version links for project 'KEY1'.");

        // Specifying the globalId shouldn't change anything
        result = remoteVersionLinkService.validateDelete(fred, version.getId(), "MyGlobalId");

        assertErrorMessage(result, "You do not have permission to manage the remote version links for project 'KEY1'.");
    }

    @Test
    public void testValidateDeleteByVersionIdThatHasNoLinks()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);
        mockEntityEngineCount(selectQuery(EntityProperty.class,
                "entityName=EntityProperty,entityBuilder=EntityPropertyFactory",
                "entityId=2",
                "entityName=RemoteVersionLink"),
                0);

        DeleteValidationResult result = remoteVersionLinkService.validateDeleteByVersionId(fred, version.getId());

        assertErrorMessage(result, "The remote link does not exist.");
    }

    @Test
    public void testValidateDeleteLinkThatDoesNotExist()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);
        mockEntityEngineCount(selectQuery(EntityProperty.class,
                "entityName=EntityProperty,entityBuilder=EntityPropertyFactory",
                "entityId=2",
                "entityName=RemoteVersionLink",
                "propertyKey=MyGlobalId"),
                0);

        DeleteValidationResult result = remoteVersionLinkService.validateDelete(fred, version.getId(), "MyGlobalId");

        assertErrorMessage(result, "The remote link does not exist.");
    }

    @Test
    public void testValidateDeleteByVersionId()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);
        mockEntityEngineCount(selectQuery(EntityProperty.class,
                "entityName=EntityProperty,entityBuilder=EntityPropertyFactory",
                "entityId=2",
                "entityName=RemoteVersionLink"),
                1);

        DeleteValidationResult result = remoteVersionLinkService.validateDeleteByVersionId(fred, version.getId());

        assertValid(result);
        assertEquals(version.getId(), result.versionId);
        assertEquals(null, result.globalId);
    }

    @Test
    public void testValidateDelete()
    {
        mockEntityEngine();
        final Project project = mockProject(1L);
        final Version version = mockVersion(2L, project);
        when(versionManager.getVersion(version.getId())).thenReturn(version);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, fred)).thenReturn(true);
        mockEntityEngineCount(selectQuery(EntityProperty.class,
                "entityName=EntityProperty,entityBuilder=EntityPropertyFactory",
                "entityId=2",
                "entityName=RemoteVersionLink",
                "propertyKey=MyGlobalId"),
                1);

        DeleteValidationResult result = remoteVersionLinkService.validateDelete(fred, version.getId(), "MyGlobalId");

        assertValid(result);
        assertEquals(version.getId(), result.versionId);
        assertEquals("MyGlobalId", result.globalId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteWithNullValidationResult()
    {
        mockJsonEntityPropertyManager();

        remoteVersionLinkService.delete(fred, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteWithInvalidValidationResult()
    {
        mockJsonEntityPropertyManager();
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Lies!");

        remoteVersionLinkService.delete(fred, new DeleteValidationResult(errors));
    }

    @Test
    public void testDeleteByVersionId()
    {
        mockOfBizDelegator();
        final Project project = mockProject(1L);
        final Version version = mockVersion(42L, project);

        doNothing().when(eventPublisher).publish(new RemoteVersionLinkDeleteEvent(version, null));

        remoteVersionLinkService.delete(fred, new DeleteValidationResult(version, null));

        verify(ofBizDelegator).removeByAnd(ENTITY_PROPERTY, new FieldMap()
                .add(ENTITY_NAME, REMOTE_VERSION_LINK)
                .add(ENTITY_ID, 42L));
        verifyNoMoreInteractions(ofBizDelegator);
        verify(eventPublisher).publish(new RemoteVersionLinkDeleteEvent(version, null));
    }

    @Test
    public void testDelete()
    {
        mockOfBizDelegator();
        final Project project = mockProject(1L);
        final Version version = mockVersion(42L, project);

        doNothing().when(eventPublisher).publish(new RemoteVersionLinkDeleteEvent(version, "MyGlobalId"));

        remoteVersionLinkService.delete(fred, new DeleteValidationResult(version, "MyGlobalId"));

        verify(ofBizDelegator).removeByAnd(ENTITY_PROPERTY, new FieldMap()
                .add(ENTITY_NAME, REMOTE_VERSION_LINK)
                .add(ENTITY_ID, 42L)
                .add(KEY, "MyGlobalId"));
        verifyNoMoreInteractions(ofBizDelegator);
        verify(eventPublisher).publish(new RemoteVersionLinkDeleteEvent(version, "MyGlobalId"));
    }

    @Test
    public void testOnVersionDeleted()
    {
        mockOfBizDelegator();
        final Project project = mockProject(1L);
        final Version version = mockVersion(42L, project);

        remoteVersionLinkService.onVersionDeleted(new VersionDeleteEvent(version));

        verify(ofBizDelegator).removeByAnd(ENTITY_PROPERTY, new FieldMap()
                .add(ENTITY_NAME, RemoteVersionLinkServiceImpl.REMOTE_VERSION_LINK)
                .add(ENTITY_ID, 42L));
        verifyNoMoreInteractions(ofBizDelegator);
    }




    private static void assertUuid(final String globalId)
    {
        if (!REGEX_UUID.matcher(globalId).matches())
        {
            fail("Expected a generated UUID, but got: " + globalId);
        }
    }

    private static void assertValid(ServiceResult result)
    {
        if (!result.isValid())
        {
            fail(result.getErrorCollection().toString());
        }
    }

    private static void assertError(ServiceResult result, String field, String expectedError)
    {
        if (result.isValid())
        {
            fail("Got a valid service result when expecting error: " + expectedError);
        }
        final Map<String,String> errors = result.getErrorCollection().getErrors();
        if (!errors.containsKey(field))
        {
            fail("Expected field '" + field + "' to have error '" + expectedError + "', but got: " + result.getErrorCollection());
        }
        assertEquals(expectedError, errors.get(field));
    }

    private static void assertError(ServiceResult result, String field, Matcher<String> expectedError)
    {
        if (result.isValid())
        {
            fail("Got a valid service result when expecting error: " + expectedError);
        }
        final Map<String,String> errors = result.getErrorCollection().getErrors();
        if (!errors.containsKey(field))
        {
            fail("Expected field '" + field + "' to have error '" + expectedError + "', but got: " + result.getErrorCollection());
        }
        assertThat(errors.get(field), expectedError);
    }

    private static void assertErrorMessage(ServiceResult result, String expectedError)
    {
        if (result.isValid())
        {
            fail("Got a valid service result when expecting error: " + expectedError);
        }
        assertThat(result.getErrorCollection().getErrorMessages(), contains(expectedError));
    }

    @SuppressWarnings("unchecked")
    private <T> void mockEntityEngineSelect(Matcher<SelectQuery<T>> expectedQuery, List<T> result)
    {
        final SelectQuery.ExecutionContext<T> executionContext = mock(SelectQuery.ExecutionContext.class, strict());
        doReturn(executionContext).when(entityEngine).run(argThat(expectedQuery));
        doReturn(result).when(executionContext).asList();
        doReturn(result).when(executionContext).consumeWith(any(EntityListConsumer.class));
    }

    @SuppressWarnings("unchecked")
    private <T> void mockEntityEngineCount(Matcher<SelectQuery<T>> expectedQuery, long count)
    {
        final SelectQuery.ExecutionContext<T> executionContext = mock(SelectQuery.ExecutionContext.class, strict());
        doReturn(executionContext).when(entityEngine).run(argThat(expectedQuery));
        doReturn(count).when(executionContext).count();
    }

    private static Project mockProject(Long id)
    {
        return new MockProject(id, "KEY" + id, "Name " + id);
    }

    private static Version mockVersion(Long id, Project project)
    {
        return new MockVersion(id, "Version " + id, project);
    }

    private static Matcher<String> substrings(String... substrings)
    {
        return allOf(transform(asList(substrings), new Function<String, Matcher<? super String>>()
        {
            @Override
            public Matcher<? super String> apply(final String substring)
            {
                return containsString(substring);
            }
        }));
    }

    private static <T> Matcher<SelectQuery<T>> selectQuery(Class<T> tClass, String... substrings)
    {
        return hasToString(substrings(substrings));
    }
}
