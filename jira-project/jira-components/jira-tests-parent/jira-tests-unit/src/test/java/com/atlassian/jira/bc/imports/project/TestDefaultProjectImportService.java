package com.atlassian.jira.bc.imports.project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.MockAttachmentPathManager;
import com.atlassian.jira.external.ExternalException;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.ProjectImportManager;
import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupOverviewImpl;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.BackupSystemInformationImpl;
import com.atlassian.jira.imports.project.core.MappingResult;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.core.ProjectImportDataImpl;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.handler.AbortImportException;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.util.MockProjectImportTemporaryFiles;
import com.atlassian.jira.imports.project.util.ProjectImportTemporaryFiles;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugin.PluginVersion;
import com.atlassian.jira.plugin.PluginVersionImpl;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollectionAssert;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestDefaultProjectImportService
{
    final User mockUser = new MockUser("test");
    private BuildUtilsInfo buildUtilsInfo;

    @Before
    public void setUp() throws Exception
    {
        buildUtilsInfo = EasyMock.createMock(BuildUtilsInfo.class);
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testValidateGetBackupOverviewHappyPathNoAttachmentPathProvided() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();
        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), null));
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateGetBackupOverviewHappyPathAttachmentPathProvided() throws IOException
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManager.attachmentsEnabled();
        mockAttachmentManagerControl.setReturnValue(true);
        mockAttachmentManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, mockAttachmentManager, new MockAttachmentPathManager("/jira/attachments"), buildUtilsInfo);

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();
        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), System.getProperty("java.io.tmpdir")));
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());

        // Verify Mock ApplicationProperties
        mockAttachmentManagerControl.verify();
    }

    @Test
    public void testValidateGetBackupOverviewPathToBackupNotAFile() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(System.getProperty("java.io.tmpdir"), null));
        assertErrorCollectionContainsOnlyErrorForField(jiraServiceContext, "backupXmlPath", "admin.errors.project.import.invalid.backup.path");
    }

    @Test
    public void testValidateGetBackupOverviewAttachmentPathProvidedDoesNotExist() throws IOException
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManager.attachmentsEnabled();
        mockAttachmentManagerControl.setReturnValue(true);
        mockAttachmentManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, mockAttachmentManager, null, buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), "/thispathwillneverexistonanysystem"));
        assertErrorCollectionContainsOnlyErrorForField(jiraServiceContext, "backupAttachmentPath", "admin.errors.project.import.invalid.attachment.backup.path");

        mockAttachmentManagerControl.verify();
    }

    @Test
    public void testValidateGetBackupOverviewAttachmentPathProvidedIsTheSameAsSystem() throws IOException
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManager.attachmentsEnabled();
        mockAttachmentManagerControl.setReturnValue(true);
        mockAttachmentManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        String attachmentPath = System.getProperty("java.io.tmpdir");

        final MockAttachmentPathManager attachmentPathManager = new MockAttachmentPathManager(attachmentPath);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, mockAttachmentManager, attachmentPathManager, buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), attachmentPath));
        assertErrorCollectionContainsOnlyErrorForField(jiraServiceContext, "backupAttachmentPath", "admin.errors.project.import.attachment.backup.path.same.as.system");

        // Verify Mock ApplicationProperties
        mockAttachmentManagerControl.verify();
    }

    @Test
    public void testValidateGetBackupOverviewAttachmentPathProvidedButAttachmentsDisabled() throws IOException
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManager.attachmentsEnabled();
        mockAttachmentManagerControl.setReturnValue(false);
        mockAttachmentManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, mockAttachmentManager, null, buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), System.getProperty("java.io.tmpdir")));
        assertErrorCollectionContainsOnlyErrorForField(jiraServiceContext, "backupAttachmentPath", "admin.errors.project.import.attachments.not.enabled");

        mockAttachmentManagerControl.verify();
    }

    @Test
    public void testValidateGetBackupOverviewAttachmentPathIsEmptyStringHappyPath() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), ""));
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateGetBackupOverviewAttachmentPathProvidedIsNotADirectory() throws IOException
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManager.attachmentsEnabled();
        mockAttachmentManagerControl.setReturnValue(true);
        mockAttachmentManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, mockAttachmentManager, null, buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), tempFile.getAbsolutePath()));
        assertErrorCollectionContainsOnlyErrorForField(jiraServiceContext, "backupAttachmentPath", "admin.errors.project.import.invalid.attachment.backup.path");

        mockAttachmentManagerControl.verify();
    }

    @Test
    public void testValidateGetBackupOverviewBackupPathDoesNotExist()
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManager.attachmentsEnabled();
        mockAttachmentManagerControl.setReturnValue(true);
        mockAttachmentManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, mockAttachmentManager, new MockAttachmentPathManager("/jira/attachments"), buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl("/thispathwillneverexistonanysystem", System.getProperty("java.io.tmpdir")));
        assertErrorCollectionContainsOnlyErrorForField(jiraServiceContext, "backupXmlPath", "admin.errors.project.import.invalid.backup.path");

        mockAttachmentManagerControl.verify();
    }

    @Test
    public void testValidateGetBackupOverviewBackupPathDoesNotExistAndImportFileIsInvalidToo()
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManager.attachmentsEnabled();
        mockAttachmentManagerControl.setReturnValue(true);
        mockAttachmentManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, mockAttachmentManager, null, buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl("/thispathwillneverexistonanysystem/bak.xml", "/thispathwillneverexistonanysystem"));
        // We now should have 2 errors
        final Map errorMessages = jiraServiceContext.getErrorCollection().getErrors();
        assertEquals(2, errorMessages.size());
        assertEquals("admin.errors.project.import.invalid.backup.path", errorMessages.get("backupXmlPath"));
        assertEquals("admin.errors.project.import.invalid.attachment.backup.path", errorMessages.get("backupAttachmentPath"));

        mockAttachmentManagerControl.verify();
    }

    @Test
    public void testValidateGetBackupOverviewBackupPathDoesNotExistAndImportFileIsMissing()
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManager.attachmentsEnabled();
        mockAttachmentManagerControl.setReturnValue(true);
        mockAttachmentManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, mockAttachmentManager, null, buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl("", "/thispathwillneverexistonanysystem"));
        // We now should have 2 errors
        final Map errorMessages = jiraServiceContext.getErrorCollection().getErrors();
        assertEquals(2, errorMessages.size());
        assertEquals("admin.errors.project.import.provide.backup.path", errorMessages.get("backupXmlPath"));
        assertEquals("admin.errors.project.import.invalid.attachment.backup.path", errorMessages.get("backupAttachmentPath"));

        mockAttachmentManagerControl.verify();
    }

    @Test
    public void testValidateGetBackupOverviewNoBackupPathProvided()
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManager.attachmentsEnabled();
        mockAttachmentManagerControl.setReturnValue(true);
        mockAttachmentManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, mockAttachmentManager, new MockAttachmentPathManager("/jira/attachments"), buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(null, System.getProperty("java.io.tmpdir")));
        assertErrorCollectionContainsOnlyErrorForField(jiraServiceContext, "backupXmlPath", "admin.errors.project.import.provide.backup.path");

        mockAttachmentManagerControl.verify();
    }

    @Test
    public void testValidateGetBackupOverviewNoPermission()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.FALSE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };

        JiraServiceContext jiraServiceContext = getContext();
        projectImportService.validateGetBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl(System.getProperty("java.io.tmpdir"), System.getProperty("java.io.tmpdir")));
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "admin.errors.project.import.must.be.admin");
    }

    @Test
    public void testValidateJiraServiceContext()
    {
        DefaultProjectImportService projectImportService = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        try
        {
            projectImportService.validateJiraServiceContext(null);
            fail("A null service context should generate an IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testUserHasSysAdminPermission()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        assertTrue(projectImportService.userHasSysAdminPermission(null));
        mockPermissionManager.verify();
    }

    @Test
    public void testUserNotHasSysAdminPermission()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.FALSE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        assertFalse(projectImportService.userHasSysAdminPermission(null));
        mockPermissionManager.verify();
    }

    @Test
    public void testGetBackupOverviewHappyPath()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectImportManager = new Mock(ProjectImportManager.class);
        mockProjectImportManager.setStrict(true);
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("1", "Prof", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);
        BackupOverview backupOverview = new BackupOverviewImpl(backupSystemInformation, Collections.EMPTY_LIST);
        mockProjectImportManager.expectAndReturn("getBackupOverview", P.args(P.eq("/some/path"), P.IS_ANYTHING, P.IS_ANYTHING), backupOverview);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), (ProjectImportManager) mockProjectImportManager.proxy(), null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            String getBuildNumber()
            {
                return "1";
            }

        };
        JiraServiceContext jiraServiceContext = getContext();

        BackupOverview returnBackupOverview = projectImportService.getBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl("/some/path", null), null);
        assertEquals(backupOverview, returnBackupOverview);
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        mockPermissionManager.verify();
        mockProjectImportManager.verify();
    }

    @Test
    public void testGetBackupOverviewNoPermission()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.FALSE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            String getText(final I18nHelper i18n, final String key)
            {
                return key;
            }
        };
        JiraServiceContext jiraServiceContext = getContext();

        assertNull(projectImportService.getBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl("/some/path", null), null));
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "admin.errors.project.import.must.be.admin");
        mockPermissionManager.verify();
    }

    @Test
    public void testGetBackupOverviewSaxException()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectImportManager = new Mock(ProjectImportManager.class);
        mockProjectImportManager.setStrict(true);
        mockProjectImportManager.expectAndThrow("getBackupOverview", P.args(P.eq("/some/path"), P.IS_ANYTHING, P.IS_ANYTHING), new SAXException("I am an exception."));

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), (ProjectImportManager) mockProjectImportManager.proxy(), null, null, null, null, null, null, null, null, null, buildUtilsInfo);
        JiraServiceContext jiraServiceContext = getContext();

        assertNull(projectImportService.getBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl("/some/path", null), null));
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "There was a problem parsing the backup XML file at /some/path: I am an exception.");
        mockPermissionManager.verify();
        mockProjectImportManager.verify();
    }

    @Test
    public void testGetBackupOverviewIoException()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectImportManager = new Mock(ProjectImportManager.class);
        mockProjectImportManager.setStrict(true);
        mockProjectImportManager.expectAndThrow("getBackupOverview", P.args(P.eq("/some/path"), P.IS_ANYTHING, P.IS_ANYTHING), new IOException("I am an exception"));

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), (ProjectImportManager) mockProjectImportManager.proxy(), null, null, null, null, null, null, null, null, null, buildUtilsInfo);
        JiraServiceContext jiraServiceContext = getContext();

        assertNull(projectImportService.getBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl("/some/path", null), null));
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "There was a problem accessing the backup XML file at /some/path.");
        mockPermissionManager.verify();
        mockProjectImportManager.verify();
    }

    @Test
    public void testGetBackupOverviewWrongBuildNumber()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectImportManager = new Mock(ProjectImportManager.class);
        mockProjectImportManager.setStrict(true);
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("10", "Professional", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);
        BackupOverview backupOverview = new BackupOverviewImpl(backupSystemInformation, Collections.EMPTY_LIST);
        mockProjectImportManager.expectAndReturn("getBackupOverview", P.args(P.eq("/some/path"), P.IS_ANYTHING, P.IS_ANYTHING), backupOverview);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), (ProjectImportManager) mockProjectImportManager.proxy(), null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {

            String getBuildNumber()
            {
                return "12";
            }

        };
        JiraServiceContext jiraServiceContext = getContext();

        // With wrong build number we return null
        assertNull(projectImportService.getBackupOverview(jiraServiceContext, new ProjectImportOptionsImpl("/some/path", null), null));
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "This data appears to be from an older version of JIRA. Please upgrade the data and try again. The current version of JIRA is at build number '12', but the supplied backup file was for build number '10'.");

        mockPermissionManager.verify();
        mockProjectImportManager.verify();
    }

    @Test
    public void testValidateBackupProjectImportableSystemLevelHappyPath()
    {
        Long projectId = new Long(123);
        String projectKey = "TST";
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.args(P.eq(projectKey)), new MockProject(projectId.longValue(), projectKey));

        Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueManager.expectAndReturn("getIssueCountForProject", P.args(P.eq(projectId)), new Long(0));

        Mock mockVersionManager = new Mock(VersionManager.class);
        mockVersionManager.setStrict(true);
        mockVersionManager.expectAndReturn("getVersions", P.args(P.eq(projectId)), Collections.EMPTY_LIST);

        Mock mockProjectComponentManager = new Mock(ProjectComponentManager.class);
        mockProjectComponentManager.setStrict(true);
        mockProjectComponentManager.expectAndReturn("findAllForProject", P.args(P.eq(projectId)), Collections.EMPTY_LIST);

        PluginInformation pluginInfo = new PluginInformation();
        pluginInfo.setVersion("1.0");

        Mock mockPlugin = new Mock(Plugin.class);
        mockPlugin.setStrict(true);
        mockPlugin.expectAndReturn("getPluginInformation", pluginInfo);

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.setStrict(true);
        // Dont return null if you want to exercise more code
        mockPluginAccessor.expectAndReturn("getPlugin", P.ANY_ARGS, mockPlugin.proxy());

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, (ProjectManager) mockProjectManager.proxy(), null, (IssueManager) mockIssueManager.proxy(), (VersionManager) mockVersionManager.proxy(), (ProjectComponentManager) mockProjectComponentManager.proxy(), (PluginAccessor) mockPluginAccessor.proxy(), null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setId(projectId.toString());
        externalProject.setKey(projectKey);
        externalProject.setAssigneeType("2");

        ExternalCustomField externalCustomField = new ExternalCustomField("678", "cust field", "cust.field.key:module");
        ExternalCustomFieldConfiguration customFieldConfiguration = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField, "321");

        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, EasyList.build(customFieldConfiguration), EasyList.build(new Long(1), new Long(2)));

        PluginVersion pluginVersion = new PluginVersionImpl("cust.field.key", "blah", "1.0", new Date());
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("456", "Prof", EasyList.build(pluginVersion), true, Collections.EMPTY_MAP, 0);

        MessageSet messageSet = projectImportService.validateBackupProjectImportableSystemLevel(jiraServiceContext, backupProject, backupSystemInformation);
        assertFalse(messageSet.hasAnyErrors());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());

        mockPermissionManager.verify();
        mockProjectManager.verify();
        mockIssueManager.verify();
        mockVersionManager.verify();
        mockProjectComponentManager.verify();
        mockPluginAccessor.verify();
        mockPlugin.verify();
    }

    /**
     * This test will test that errors are reported when the Project to be imported into has issues, versions, or components.
     * It tests all three at once on purpose in order to verify that we can handle multiple errors collected in a single validation.
     */
    @Test
    public void testValidateBackupProjectImportableSystemLevelIssueVersionAndComponentCountNonZero()
    {
        Long projectId = new Long(123);
        String projectKey = "TST";
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.args(P.eq(projectKey)), new MockProject(projectId.longValue(), projectKey));

        Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueManager.expectAndReturn("getIssueCountForProject", P.args(P.eq(projectId)), new Long(12));

        Mock mockVersionManager = new Mock(VersionManager.class);
        mockVersionManager.setStrict(true);
        mockVersionManager.expectAndReturn("getVersions", P.args(P.eq(projectId)), EasyList.build("v1.0", "v1.1"));

        Mock mockProjectComponentManager = new Mock(ProjectComponentManager.class);
        mockProjectComponentManager.setStrict(true);
        mockProjectComponentManager.expectAndReturn("findAllForProject", P.args(P.eq(projectId)), EasyList.build("MyComponent"));

        PluginInformation pluginInfo = new PluginInformation();
        pluginInfo.setVersion("1.0");

        Mock mockPlugin = new Mock(Plugin.class);
        mockPlugin.setStrict(true);
        mockPlugin.expectAndReturn("getPluginInformation", pluginInfo);

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.setStrict(true);
        // Dont return null if you want to exercise more code
        mockPluginAccessor.expectAndReturn("getPlugin", P.ANY_ARGS, mockPlugin.proxy());

        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.setStrict(true);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, (ProjectManager) mockProjectManager.proxy(), null, (IssueManager) mockIssueManager.proxy(), (VersionManager) mockVersionManager.proxy(), (ProjectComponentManager) mockProjectComponentManager.proxy(), (PluginAccessor) mockPluginAccessor.proxy(), (ApplicationProperties) mockApplicationProperties.proxy(), null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setId(projectId.toString());
        externalProject.setKey(projectKey);

        ExternalCustomField externalCustomField = new ExternalCustomField("678", "cust field", "cust.field.key:module");
        ExternalCustomFieldConfiguration customFieldConfiguration = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField, "321");

        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, EasyList.build(customFieldConfiguration), EasyList.build(new Long(1), new Long(2)));

        PluginVersion pluginVersion = new PluginVersionImpl("cust.field.key", "blah", "1.0", new Date());
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("456", "Prof", EasyList.build(pluginVersion), true, Collections.EMPTY_MAP, 0);

        MessageSet messageSet = projectImportService.validateBackupProjectImportableSystemLevel(jiraServiceContext, backupProject, backupSystemInformation);
        assertTrue(messageSet.hasAnyErrors());
        // No waarings (only errors)
        assertTrue(messageSet.getWarningMessages().isEmpty());
        // Three errors
        assertEquals(3, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertTrue(jiraServiceContext.getErrorCollection().getErrorMessages().contains("The existing project with key 'TST' contains '12' issues. You can not import a backup project into a project that contains existing issues."));
        assertTrue(jiraServiceContext.getErrorCollection().getErrorMessages().contains("The existing project with key 'TST' contains '2' versions. You can not import a backup project into a project that contains existing versions."));
        assertTrue(jiraServiceContext.getErrorCollection().getErrorMessages().contains("The existing project with key 'TST' contains '1' components. You can not import a backup project into a project that contains existing components."));

        mockPermissionManager.verify();
        mockProjectManager.verify();
        mockIssueManager.verify();
        mockVersionManager.verify();
        mockProjectComponentManager.verify();
        mockPluginAccessor.verify();
        mockPlugin.verify();
        mockApplicationProperties.verify();
    }

    /**
     * Tests the case where the project has Default Assignee as Unassigned, but this is illegal because this JIRA does
     * not allow unassigned issues.
     */
    @Test
    public void testValidateBackupProjectImportableSystemLevelProjectHasDefaultAssigneeUnassignedNontAllowed()
    {
        Long projectId = new Long(123);
        String projectKey = "TST";
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.args(P.eq(projectKey)), new MockProject(projectId.longValue(), projectKey));

        Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueManager.expectAndReturn("getIssueCountForProject", P.args(P.eq(projectId)), new Long(0));

        Mock mockVersionManager = new Mock(VersionManager.class);
        mockVersionManager.setStrict(true);
        mockVersionManager.expectAndReturn("getVersions", P.args(P.eq(projectId)), Collections.EMPTY_LIST);

        Mock mockProjectComponentManager = new Mock(ProjectComponentManager.class);
        mockProjectComponentManager.setStrict(true);
        mockProjectComponentManager.expectAndReturn("findAllForProject", P.args(P.eq(projectId)), Collections.EMPTY_LIST);

        PluginInformation pluginInfo = new PluginInformation();
        pluginInfo.setVersion("1.0");

        Mock mockPlugin = new Mock(Plugin.class);
        mockPlugin.setStrict(true);
        mockPlugin.expectAndReturn("getPluginInformation", pluginInfo);

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.setStrict(true);
        // Dont return null if you want to exercise more code
        mockPluginAccessor.expectAndReturn("getPlugin", P.ANY_ARGS, mockPlugin.proxy());

        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.setStrict(true);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)), Boolean.FALSE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, (ProjectManager) mockProjectManager.proxy(), null, (IssueManager) mockIssueManager.proxy(), (VersionManager) mockVersionManager.proxy(), (ProjectComponentManager) mockProjectComponentManager.proxy(), (PluginAccessor) mockPluginAccessor.proxy(), (ApplicationProperties) mockApplicationProperties.proxy(), null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setId(projectId.toString());
        externalProject.setKey(projectKey);

        ExternalCustomField externalCustomField = new ExternalCustomField("678", "cust field", "cust.field.key:module");
        ExternalCustomFieldConfiguration customFieldConfiguration = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField, "321");

        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, EasyList.build(customFieldConfiguration), EasyList.build(new Long(1), new Long(2)));

        PluginVersion pluginVersion = new PluginVersionImpl("cust.field.key", "blah", "1.0", new Date());
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("456", "Prof", EasyList.build(pluginVersion), true, Collections.EMPTY_MAP, 0);

        MessageSet messageSet = projectImportService.validateBackupProjectImportableSystemLevel(jiraServiceContext, backupProject, backupSystemInformation);
        assertTrue(messageSet.hasAnyErrors());
        // No Warnings
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "The backup project 'null' has 'unassigned' default assignee, but this JIRA instance does not allow unassigned issues.");

        mockPermissionManager.verify();
        mockProjectManager.verify();
        mockIssueManager.verify();
        mockVersionManager.verify();
        mockProjectComponentManager.verify();
        mockPluginAccessor.verify();
        mockPlugin.verify();
        mockApplicationProperties.verify();
    }

    @Test
    public void testValidateBackupProjectImportableSystemLevelProjectDoesntExist()
    {
        Long projectId = new Long(123);
        String projectKey = "TST";
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        // Return a null project - test if it doesnt exist.
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.args(P.eq(projectKey)), null);

        PluginInformation pluginInfo = new PluginInformation();
        pluginInfo.setVersion("1.0");

        Mock mockPlugin = new Mock(Plugin.class);
        mockPlugin.setStrict(true);
        mockPlugin.expectAndReturn("getPluginInformation", pluginInfo);

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.setStrict(true);
        // Dont return null if you want to exercise more code
        mockPluginAccessor.expectAndReturn("getPlugin", P.ANY_ARGS, mockPlugin.proxy());

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, (ProjectManager) mockProjectManager.proxy(), null, null, null, null , (PluginAccessor) mockPluginAccessor.proxy(), null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setId(projectId.toString());
        externalProject.setKey(projectKey);

        ExternalCustomField externalCustomField = new ExternalCustomField("678", "cust field", "cust.field.key:module");
        ExternalCustomFieldConfiguration customFieldConfiguration = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField, "321");

        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, EasyList.build(customFieldConfiguration), EasyList.build(new Long(1), new Long(2)));

        PluginVersion pluginVersion = new PluginVersionImpl("cust.field.key", "blah", "1.0", new Date());
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("456", "Prof", EasyList.build(pluginVersion), true, Collections.EMPTY_MAP, 0);

        MessageSet messageSet = projectImportService.validateBackupProjectImportableSystemLevel(jiraServiceContext, backupProject, backupSystemInformation);
        assertFalse(messageSet.hasAnyErrors());
        assertMessageSetContainsSingleMessage(messageSet.getWarningMessages(), "No project with key 'TST' exists in this instance of JIRA. The importer will create a project with this key and the details of the backup project using the default schemes.");

        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        mockPermissionManager.verify();
        mockProjectManager.verify();
        mockPluginAccessor.verify();
        mockPlugin.verify();
    }

    @Test
    public void testValidateBackupProjectImportableSystemLevelNullBackupProject()
    {
        DefaultProjectImportService projectImportService = new DefaultProjectImportService(null, null, null, null, null, null , null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("456", "Prof", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);

        final MessageSet messageSet = projectImportService.validateBackupProjectImportableSystemLevel(jiraServiceContext, null, backupSystemInformation);
        assertTrue(messageSet.hasAnyErrors());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "You can not import a null project.");
    }

    @Test
    public void testNoPermission()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.FALSE);

        DefaultProjectImportService defaultProjectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);
        JiraServiceContext jiraServiceContext = getContext();
        ExternalProject externalProject = new ExternalProject();
        externalProject.setId("999");
        externalProject.setKey("None");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, EasyList.build(new Long(1), new Long(2)));

        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("456", "Prof", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);
        final MessageSet messageSet = defaultProjectImportService.validateBackupProjectImportableSystemLevel(jiraServiceContext, backupProject, backupSystemInformation);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("You must be a JIRA System Administrator to perform a project import.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testCustomFieldWrongVersion()
    {
        Long projectId = new Long(123);
        String projectKey = "TST";
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.args(P.eq(projectKey)), new MockProject(projectId.longValue(), projectKey));

        Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueManager.expectAndReturn("getIssueCountForProject", P.args(P.eq(projectId)), new Long(0));

        Mock mockVersionManager = new Mock(VersionManager.class);
        mockVersionManager.setStrict(true);
        mockVersionManager.expectAndReturn("getVersions", P.args(P.eq(projectId)), Collections.EMPTY_LIST);

        Mock mockProjectComponentManager = new Mock(ProjectComponentManager.class);
        mockProjectComponentManager.setStrict(true);
        mockProjectComponentManager.expectAndReturn("findAllForProject", P.args(P.eq(projectId)), Collections.EMPTY_LIST);

        Mock mockPlugin = new Mock(Plugin.class);
        mockPlugin.setStrict(true);
        mockPlugin.expectAndReturn("getPluginInformation", getPluginInformation("1.4"));
        Mock mockPlugin2 = new Mock(Plugin.class);
        mockPlugin2.setStrict(true);
        mockPlugin2.expectAndReturn("getPluginInformation", getPluginInformation("1.2"));

        final MockControl mockPluginAccessorControl = MockControl.createNiceControl(PluginAccessor.class);
        final PluginAccessor mockPluginAccessor = (PluginAccessor) mockPluginAccessorControl.getMock();
        mockPluginAccessor.getPlugin("ABC");
        mockPluginAccessorControl.setReturnValue(mockPlugin.proxy());
        mockPluginAccessor.getPlugin("DEF");
        mockPluginAccessorControl.setReturnValue(mockPlugin2.proxy());
        mockPluginAccessor.getPlugin("XYZ");
        mockPluginAccessorControl.setReturnValue(null);
        mockPluginAccessorControl.replay();

        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.setStrict(true);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, (ProjectManager) mockProjectManager.proxy(), null, (IssueManager) mockIssueManager.proxy(), (VersionManager) mockVersionManager.proxy(), (ProjectComponentManager) mockProjectComponentManager.proxy(), mockPluginAccessor, (ApplicationProperties) mockApplicationProperties.proxy(), null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setId(projectId.toString());
        externalProject.setKey(projectKey);

        ExternalCustomField externalCustomField1 = new ExternalCustomField("10", "Stuff", "ABC:stuff");
        ExternalCustomFieldConfiguration customFieldConfiguration1 = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField1, "321");
        ExternalCustomField externalCustomField2 = new ExternalCustomField("14", "More Stuff", "DEF:morestuff");
        ExternalCustomFieldConfiguration customFieldConfiguration2 = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField2, "321");
        ExternalCustomField externalCustomField3 = new ExternalCustomField("12", "Other Stuff", "XYZ:otherstuff");
        ExternalCustomFieldConfiguration customFieldConfiguration3 = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField3, "321");

        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, EasyList.build(customFieldConfiguration1, customFieldConfiguration2, customFieldConfiguration3), EasyList.build(new Long(1), new Long(2)));

        PluginVersion pluginVersion1 = new PluginVersionImpl("XYZ", "otherstuff", "1.0", new Date());
        PluginVersion pluginVersion2 = new PluginVersionImpl("ABC", "stuff", "1.0", new Date());
        PluginVersion pluginVersion3 = new PluginVersionImpl("DEF", "stuff", "1.2", new Date());
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("456", "Prof", EasyList.build(pluginVersion1, pluginVersion2, pluginVersion3), true, Collections.EMPTY_MAP, 0);

        MessageSet messageSet = projectImportService.validateBackupProjectImportableSystemLevel(jiraServiceContext, backupProject, backupSystemInformation);
        assertTrue(messageSet.hasAnyErrors());
        // Note that we should get a whinge on version of ABC, but nothing on XYZ which is missing from the current JIRA.
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "The backup project 'null' requires custom field named 'Stuff' with full key 'ABC:stuff'. In the current instance of JIRA the plugin is at version '1.4', but in the backup it is at version '1.0'.");

        mockPermissionManager.verify();
        mockProjectManager.verify();
        mockIssueManager.verify();
        mockVersionManager.verify();
        mockProjectComponentManager.verify();
        mockPluginAccessorControl.verify();
        mockPlugin.verify();
        mockApplicationProperties.verify();
    }

    @Test
    public void testCustomFieldDoesNotExistInBackup()
    {
        Long projectId = new Long(123);
        String projectKey = "TST";
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        Mock mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);
        mockProjectManager.expectAndReturn("getProjectObjByKey", P.args(P.eq(projectKey)), new MockProject(projectId.longValue(), projectKey));

        Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueManager.expectAndReturn("getIssueCountForProject", P.args(P.eq(projectId)), new Long(0));

        Mock mockVersionManager = new Mock(VersionManager.class);
        mockVersionManager.setStrict(true);
        mockVersionManager.expectAndReturn("getVersions", P.args(P.eq(projectId)), Collections.EMPTY_LIST);

        Mock mockProjectComponentManager = new Mock(ProjectComponentManager.class);
        mockProjectComponentManager.setStrict(true);
        mockProjectComponentManager.expectAndReturn("findAllForProject", P.args(P.eq(projectId)), Collections.EMPTY_LIST);

        Mock mockPlugin = new Mock(Plugin.class);
        mockPlugin.setStrict(true);
        mockPlugin.expectAndReturn("getPluginInformation", getPluginInformation("1.4"));
        Mock mockPlugin2 = new Mock(Plugin.class);
        mockPlugin2.setStrict(true);
        mockPlugin2.expectAndReturn("getPluginInformation", getPluginInformation("1.2"));

        final MockControl mockPluginAccessorControl = MockControl.createNiceControl(PluginAccessor.class);
        final PluginAccessor mockPluginAccessor = (PluginAccessor) mockPluginAccessorControl.getMock();
        mockPluginAccessor.getPlugin("ABC");
        mockPluginAccessorControl.setReturnValue(mockPlugin.proxy());
        mockPluginAccessor.getPlugin("DEF");
        mockPluginAccessorControl.setReturnValue(mockPlugin2.proxy());
        mockPluginAccessor.getPlugin("XYZ");
        mockPluginAccessorControl.setReturnValue(null);
        mockPluginAccessorControl.replay();

        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.setStrict(true);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, (ProjectManager) mockProjectManager.proxy(), null, (IssueManager) mockIssueManager.proxy(), (VersionManager) mockVersionManager.proxy(), (ProjectComponentManager) mockProjectComponentManager.proxy(), mockPluginAccessor, (ApplicationProperties) mockApplicationProperties.proxy(), null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setId(projectId.toString());
        externalProject.setKey(projectKey);

        ExternalCustomField externalCustomField1 = new ExternalCustomField("10", "Stuff", "ABC:stuff");
        ExternalCustomFieldConfiguration customFieldConfiguration1 = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField1, "321");
        ExternalCustomField externalCustomField2 = new ExternalCustomField("14", "More Stuff", "DEF:morestuff");
        ExternalCustomFieldConfiguration customFieldConfiguration2 = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField2, "321");
        ExternalCustomField externalCustomField3 = new ExternalCustomField("12", "Other Stuff", "XYZ:otherstuff");
        ExternalCustomFieldConfiguration customFieldConfiguration3 = new ExternalCustomFieldConfiguration(Collections.EMPTY_LIST, null, externalCustomField3, "321");

        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, EasyList.build(customFieldConfiguration1, customFieldConfiguration2, customFieldConfiguration3), EasyList.build(new Long(1), new Long(2)));

        PluginVersion pluginVersion1 = new PluginVersionImpl("XYZ", "otherstuff", "1.0", new Date());
        PluginVersion pluginVersion3 = new PluginVersionImpl("DEF", "stuff", "1.2", new Date());
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("456", "Prof", EasyList.build(pluginVersion1, pluginVersion3), true, Collections.EMPTY_MAP, 0);

        MessageSet messageSet = projectImportService.validateBackupProjectImportableSystemLevel(jiraServiceContext, backupProject, backupSystemInformation);
        assertTrue(messageSet.hasAnyErrors());
        // Note that we should get a whinge on version of ABC, but nothing on XYZ which is missing from the current JIRA.
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "The backup project 'null' requires custom field named 'Stuff' with full key 'ABC:stuff'. In the current instance of JIRA the plugin is at version '1.4', but this custom field was not installed in the backup data. You may want to create an XML backup with this version of the plugin installed.");

        mockPermissionManager.verify();
        mockProjectManager.verify();
        mockIssueManager.verify();
        mockVersionManager.verify();
        mockProjectComponentManager.verify();
        mockPluginAccessorControl.verify();
        mockPlugin.verify();
        mockApplicationProperties.verify();
    }

    @Test
    public void testValidateDoMappingHappyPath() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();
        JiraServiceContext jiraServiceContext = getContext();
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        projectImportService.validateDoMapping(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), null), backupProject, getBackupSystemInformationImpl());
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateDoMappingNullBackupSystemInformation() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();
        JiraServiceContext jiraServiceContext = getContext();
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        try
        {
            projectImportService.validateDoMapping(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), null), backupProject, null);
            fail("Null not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // Expected.
        }
    }

    @Test
    public void testValidateDoMappingNullProjImportOptions() throws IOException
    {
        DefaultProjectImportService projectImportService = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        try
        {
            projectImportService.validateDoMapping(jiraServiceContext, null, backupProject, getBackupSystemInformationImpl());
            fail("Null not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // Expected.
        }
    }

    private BackupSystemInformationImpl getBackupSystemInformationImpl()
    {
        return new BackupSystemInformationImpl("123", "Pro", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);
    }

    @Test
    public void testValidateDoMappingNullImportPath() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        projectImportService.validateDoMapping(jiraServiceContext, new ProjectImportOptionsImpl(null, null), backupProject, getBackupSystemInformationImpl());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "You must provide a path to the JIRA backup XML file.");
    }

    @Test
    public void testValidateDoMappingInvalidImportFilePath() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        projectImportService.validateDoMapping(jiraServiceContext, new ProjectImportOptionsImpl("/SomeStupidPath/Whatever.xml", null), backupProject, getBackupSystemInformationImpl());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "The path to the JIRA backup XML file is not valid.");
    }

    @Test
    public void testValidateDoMappingNullBackupProject() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();
        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();

        projectImportService.validateDoMapping(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), null), null, getBackupSystemInformationImpl());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "You must provide a backup project to import.");
    }

    @Test
    public void testValidateDoMappingNoPermission() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.FALSE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();
        JiraServiceContext jiraServiceContext = getContext();
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        projectImportService.validateDoMapping(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), null), backupProject, getBackupSystemInformationImpl());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "You must be a JIRA System Administrator to perform a project import.");
    }

    @Test
    public void testDoMappingNoPermission() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.FALSE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        final File tempFile = File.createTempFile("JIRABackup", "xml");
        tempFile.deleteOnExit();
        JiraServiceContext jiraServiceContext = getContext();
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        ProjectImportData projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), temporaryFiles, 0, 0, 0, 0, 0);
        projectImportService.doMapping(jiraServiceContext, new ProjectImportOptionsImpl(tempFile.getAbsolutePath(), null), projectImportData, backupProject, getBackupSystemInformationImpl(), null);
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "You must be a JIRA System Administrator to perform a project import.");
    }

    private JiraServiceContext getContext()
    {
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection()) {
            @Override
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };
        return jiraServiceContext;
    }

    @Test
    public void testDoMappingNullProjectImportOption() throws Exception
    {
        DefaultProjectImportService projectImportService = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);
        JiraServiceContext jiraServiceContext = getContext();
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        ProjectImportData projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);
        try
        {
            projectImportService.doMapping(jiraServiceContext, null, projectImportData, backupProject, getBackupSystemInformationImpl(), null);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //Expected
        }
    }

    @Test
    public void testDoMappingNullBackupProject() throws Exception
    {
        DefaultProjectImportService projectImportService = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);
        JiraServiceContext jiraServiceContext = getContext();
        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/attach/path");
        ProjectImportData projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);

        try
        {
            projectImportService.doMapping(jiraServiceContext, projectImportOptions, projectImportData, null, getBackupSystemInformationImpl(), null);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //Expected
        }
    }

    @Test
    public void testDoMappingNullBackupSystemInformation() throws Exception
    {
        DefaultProjectImportService projectImportService = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);
        JiraServiceContext jiraServiceContext = getContext();
        ProjectImportData projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        try
        {
            projectImportService.doMapping(jiraServiceContext, new ProjectImportOptionsImpl(null, null), projectImportData, backupProject, null, null);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //Expected
        }
    }

    @Test
    public void testDoMappingIOException()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        // Mock out the manager
        Mock mockProjectImportManager = new Mock(ProjectImportManager.class);
        mockProjectImportManager.setStrict(true);
        mockProjectImportManager.expectAndThrow("getProjectImportData", P.ANY_ARGS, new IOException("File exists - not!"));

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), (ProjectImportManager) mockProjectImportManager.proxy(), null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        projectImportService.getProjectImportData(jiraServiceContext, new ProjectImportOptionsImpl("/ged/njk.xml", null), backupProject, getBackupSystemInformationImpl(), null);
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "There was a problem accessing the backup XML file at /ged/njk.xml.");
        mockProjectImportManager.verify();
    }

    @Test
    public void testDoMappingSAXException()
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.TRUE);

        // Mock out the manager
        Mock mockProjectImportManager = new Mock(ProjectImportManager.class);
        mockProjectImportManager.setStrict(true);
        mockProjectImportManager.expectAndThrow("getProjectImportData", P.ANY_ARGS, new SAXException("Parse Exception."));

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), (ProjectImportManager) mockProjectImportManager.proxy(), null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        projectImportService.getProjectImportData(jiraServiceContext, new ProjectImportOptionsImpl("/ged/njk.xml", null), backupProject, getBackupSystemInformationImpl(), null);
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "There was a problem parsing the backup XML file at /ged/njk.xml: Parse Exception.");
        mockProjectImportManager.verify();
    }

    @Test
    public void testGetProjectImportNoPermission() throws IOException
    {
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.SYSTEM_ADMIN), P.IS_ANYTHING), Boolean.FALSE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager) mockPermissionManager.proxy(), null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext jiraServiceContext = getContext();
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(),  Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        projectImportService.getProjectImportData(jiraServiceContext, new ProjectImportOptionsImpl("/blah", null), backupProject, getBackupSystemInformationImpl(), null);
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "You must be a JIRA System Administrator to perform a project import.");
    }
    
    @Test
    public void testGetProjectImportDataNullBackupProject() throws Exception
    {
        DefaultProjectImportService projectImportService = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext serviceContext = getContext();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/attach/path");
        final BackupSystemInformationImpl backupSystemInformation = getBackupSystemInformationImpl();
        try
        {
            projectImportService.getProjectImportData(serviceContext, projectImportOptions, null, backupSystemInformation, null);
            fail("expected IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testGetProjectImportDataNullProjectImportOptions() throws Exception
    {
        DefaultProjectImportService projectImportService = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext serviceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        final BackupSystemInformationImpl backupSystemInformation = getBackupSystemInformationImpl();
        try
        {
            projectImportService.getProjectImportData(serviceContext, null, backupProject, backupSystemInformation, null);
            fail("expected IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testGetProjectImportDataNullBackupSystemInformation() throws Exception
    {
        DefaultProjectImportService projectImportService = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext serviceContext = getContext();

        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/attach/path");
        try
        {
            projectImportService.getProjectImportData(serviceContext, projectImportOptions, backupProject, null, null);
            fail("expected IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testGetProjectImportDataHappyPath() throws Exception
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/attach/path");
        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);

        final BackupSystemInformationImpl backupSystemInformation = getBackupSystemInformationImpl();
        mockProjectImportManager.getProjectImportData(projectImportOptions, backupProject, backupSystemInformation, null);
        mockProjectImportManagerControl.setReturnValue(projectImportData);

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        assertEquals(projectImportData, projectImportService.getProjectImportData(serviceContext, projectImportOptions, backupProject, backupSystemInformation, null));

        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoMappingHappyPath() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/attach/path");
        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final BackupSystemInformationImpl backupSystemInformation = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setIssueTypeMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapAndValidateCustomFields(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setCustomFieldMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapSystemFields(projectImportData, backupProject);
        initialMappingResult.setPriorityMessageSet(new MessageSetImpl());
        initialMappingResult.setStatusMessageSet(new MessageSetImpl());
        initialMappingResult.setResolutionMessageSet(new MessageSetImpl());
        initialMappingResult.setGroupMessageSet(new MessageSetImpl());
        initialMappingResult.setIssueLinkTypeMessageSet(new MessageSetImpl());
        initialMappingResult.setUserMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapProjectRoles(projectImportData);
        initialMappingResult.setProjectRoleMessageSet(new MessageSetImpl());
        initialMappingResult.setProjectRoleActorMessageSet(new MessageSetImpl());
        initialMappingResult.setFileAttachmentMessageSet(new MessageSetImpl());
        initialMappingResult.setIssueSecurityLevelMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapCustomFieldOptions(projectImportData, backupProject);

        mockProjectImportManager.validateCustomFieldValues(projectImportData, initialMappingResult, backupProject, null, i18n);

        mockProjectImportManager.validateSystemFields(projectImportData, initialMappingResult, projectImportOptions, backupProject, null, i18n);

        mockProjectImportManager.validateFileAttachments(projectImportOptions, projectImportData, initialMappingResult, backupProject, backupSystemInformation, null, i18n);

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        final MappingResult mappingResult = projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, backupProject, backupSystemInformation, null);

        assertTrue(mappingResult.canImport());
        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoMappingIssueTypeInvalid() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        final ProjectImportDataImpl importData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", null);
        final BackupSystemInformationImpl backupSystemImpl = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(importData, initialMappingResult, backupProject, i18n);

        MessageSet issueTypeMessageSet = new MessageSetImpl();
        issueTypeMessageSet.addErrorMessage("I am an error");
        initialMappingResult.setIssueTypeMessageSet(issueTypeMessageSet);

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        final MappingResult mappingResult = projectImportService.doMapping(serviceContext, projectImportOptions, importData, backupProject, backupSystemImpl, null);

        assertTrue(mappingResult.getIssueTypeMessageSet().hasAnyErrors());
        assertErrorCollectionContainsOnlyError(serviceContext, "The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");

        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoMappingCustomFieldsInvalid() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", null);
        final BackupSystemInformationImpl backupSystemImpl = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setIssueTypeMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapAndValidateCustomFields(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setCustomFieldMessageSet(new MessageSetImpl());

        MessageSet customFieldMessageSet = new MessageSetImpl();
        customFieldMessageSet.addErrorMessage("I am an error");
        initialMappingResult.setCustomFieldMessageSet(customFieldMessageSet);

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        final MappingResult mappingResult = projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, backupProject, backupSystemImpl, null);

        assertTrue(mappingResult.getCustomFieldMessageSet().hasAnyErrors());
        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoMappingCustomFieldsInvalidCheckMessageSetForValues() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        final ProjectImportDataImpl importData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", null);
        final BackupSystemInformationImpl backupSystemImpl = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(importData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setIssueTypeMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapAndValidateCustomFields(importData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setCustomFieldMessageSet(new MessageSetImpl());

        MessageSet customFieldMessageSet = new MessageSetImpl();
        customFieldMessageSet.addErrorMessage("I am an error");
        initialMappingResult.setCustomFieldMessageSet(customFieldMessageSet);

        // We want to have some required custom fields so that they get added with null message sets
        importData.getProjectImportMapper().getCustomFieldMapper().flagValueAsRequired("23", "12");
        importData.getProjectImportMapper().getCustomFieldMapper().flagValueAsRequired("25", "12");

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        final MappingResult mappingResult = projectImportService.doMapping(serviceContext, projectImportOptions, importData, backupProject, backupSystemImpl, null);

        assertEquals(2, mappingResult.getCustomFieldValueMessageSets().size());
        assertTrue(mappingResult.getCustomFieldValueMessageSets().keySet().contains("23"));
        assertNull(mappingResult.getCustomFieldValueMessageSets().get("23"));
        assertTrue(mappingResult.getCustomFieldValueMessageSets().keySet().contains("25"));
        assertNull(mappingResult.getCustomFieldValueMessageSets().get("25"));
        assertTrue(mappingResult.getCustomFieldMessageSet().hasAnyErrors());
        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoMappingCustomFieldsValuesInvalid() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", null);
        final BackupSystemInformationImpl backupSystemImpl = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setIssueTypeMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapAndValidateCustomFields(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setCustomFieldMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapSystemFields(projectImportData, backupProject);
        initialMappingResult.setPriorityMessageSet(new MessageSetImpl());
        initialMappingResult.setStatusMessageSet(new MessageSetImpl());
        initialMappingResult.setResolutionMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapProjectRoles(projectImportData);

        mockProjectImportManager.autoMapCustomFieldOptions(projectImportData, backupProject);

        mockProjectImportManager.validateCustomFieldValues(projectImportData, initialMappingResult, backupProject, null, i18n);

        mockProjectImportManager.validateSystemFields(projectImportData, initialMappingResult, projectImportOptions, backupProject, null, i18n);

        MessageSet customFieldValueMesageSet = new MessageSetImpl();
        customFieldValueMesageSet.addErrorMessage("I am an error");
        HashMap customFieldValuesErrors  = new HashMap();
        customFieldValuesErrors.put("customFieldName", customFieldValueMesageSet);
        initialMappingResult.setCustomFieldValueMessageSets(customFieldValuesErrors);

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        final MappingResult mappingResult = projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, backupProject, backupSystemImpl, null);

        assertTrue(mappingResult.hasAnyCustomFieldValueErrors());
        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoMappingCustomFieldsValuesSaxException() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), temporaryFiles, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", null);
        final BackupSystemInformationImpl backupSystemImpl = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setIssueTypeMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapAndValidateCustomFields(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setCustomFieldMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapSystemFields(projectImportData, backupProject);
        initialMappingResult.setPriorityMessageSet(new MessageSetImpl());
        initialMappingResult.setStatusMessageSet(new MessageSetImpl());
        initialMappingResult.setResolutionMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapProjectRoles(projectImportData);

        mockProjectImportManager.autoMapCustomFieldOptions(projectImportData, backupProject);

        mockProjectImportManager.validateCustomFieldValues(projectImportData, initialMappingResult, backupProject, null, i18n);
        mockProjectImportManagerControl.setThrowable(new SAXException("I am an exception"));

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        assertNull(projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, backupProject, backupSystemImpl, null));

        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        assertEquals("There was a SAX parsing problem accessing the custom field value XML file at " + temporaryFiles.getCustomFieldValuesXmlFile().getAbsolutePath() +
                     ", I am an exception.", serviceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoMappingCustomFieldsValuesIOException() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), temporaryFiles, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", null);
        final BackupSystemInformationImpl backupSystemImpl = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setIssueTypeMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapAndValidateCustomFields(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setCustomFieldMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapSystemFields(projectImportData, backupProject);
        initialMappingResult.setPriorityMessageSet(new MessageSetImpl());
        initialMappingResult.setStatusMessageSet(new MessageSetImpl());
        initialMappingResult.setResolutionMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapProjectRoles(projectImportData);

        mockProjectImportManager.autoMapCustomFieldOptions(projectImportData, backupProject);

        mockProjectImportManager.validateCustomFieldValues(projectImportData, initialMappingResult, backupProject, null, i18n);
        mockProjectImportManagerControl.setThrowable(new IOException("I am an exception"));

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        assertNull(projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, backupProject, backupSystemImpl, null));

        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        assertEquals("There was a problem accessing the custom field value XML file at " + temporaryFiles.getCustomFieldValuesXmlFile().getAbsolutePath() +
                     ".", serviceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoMappingFileAttachmentsIOException() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), temporaryFiles, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/attach/path");
        final BackupSystemInformationImpl backupSystemImpl = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setIssueTypeMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapAndValidateCustomFields(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setCustomFieldMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapSystemFields(projectImportData, backupProject);
        initialMappingResult.setPriorityMessageSet(new MessageSetImpl());
        initialMappingResult.setStatusMessageSet(new MessageSetImpl());
        initialMappingResult.setResolutionMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapProjectRoles(projectImportData);

        mockProjectImportManager.autoMapCustomFieldOptions(projectImportData, backupProject);

        mockProjectImportManager.validateCustomFieldValues(projectImportData, initialMappingResult, backupProject, null, i18n);
        
        mockProjectImportManager.validateSystemFields(projectImportData, initialMappingResult, projectImportOptions, backupProject, null, i18n);

        mockProjectImportManager.validateFileAttachments(projectImportOptions, projectImportData, initialMappingResult, backupProject, backupSystemImpl, null, i18n);
        mockProjectImportManagerControl.setThrowable(new IOException("I am an exception"));

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        assertNull(projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, backupProject, backupSystemImpl, null));

        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        assertEquals("There was a problem accessing the file attachment XML file at " + temporaryFiles.getFileAttachmentEntitiesXmlFile().getAbsolutePath() +
                     ".", serviceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoMappingFileAttachmentsSaxException() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), temporaryFiles, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/attach/path");
        final BackupSystemInformationImpl backupSystemImpl = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setIssueTypeMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapAndValidateCustomFields(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setCustomFieldMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapSystemFields(projectImportData, backupProject);
        initialMappingResult.setPriorityMessageSet(new MessageSetImpl());
        initialMappingResult.setStatusMessageSet(new MessageSetImpl());
        initialMappingResult.setResolutionMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapProjectRoles(projectImportData);

        mockProjectImportManager.autoMapCustomFieldOptions(projectImportData, backupProject);

        mockProjectImportManager.validateCustomFieldValues(projectImportData, initialMappingResult, backupProject, null, i18n);

        mockProjectImportManager.validateSystemFields(projectImportData, initialMappingResult, projectImportOptions, backupProject, null, i18n);

        mockProjectImportManager.validateFileAttachments(projectImportOptions, projectImportData, initialMappingResult, backupProject, backupSystemImpl, null, i18n);
        mockProjectImportManagerControl.setThrowable(new SAXException("I am an exception"));

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        assertNull(projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, backupProject, backupSystemImpl, null));

        assertTrue(serviceContext.getErrorCollection().hasAnyErrors());
        assertEquals("There was a SAX parsing problem accessing the custom field value XML file at " + temporaryFiles.getFileAttachmentEntitiesXmlFile().getAbsolutePath() +
                     ", I am an exception.", serviceContext.getErrorCollection().getErrorMessages().iterator().next());
        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    private Object getPluginInformation(final String version)
    {
        PluginInformation pluginInformation = new PluginInformation();
        pluginInformation.setVersion(version);
        return pluginInformation;
    }

    private void assertErrorCollectionContainsOnlyError(JiraServiceContext jiraServiceContext, String errorMsg)
    {
        assertErrorCollectionContainsOnlyError(jiraServiceContext.getErrorCollection(), errorMsg);
    }

    private void assertMessageSetContainsSingleMessage(Set messages, String errorMsg)
    {
        assertEquals(1, messages.size());
        assertEquals(errorMsg, messages.iterator().next());
    }

    private void assertErrorCollectionContainsOnlyError(ErrorCollection errorCollection, String errorMsg)
    {
        final Collection errorMessages = errorCollection.getErrorMessages();
        assertEquals(1, errorMessages.size());
        assertEquals(errorMsg, errorMessages.iterator().next());
    }

    private void assertErrorCollectionContainsOnlyErrorForField(JiraServiceContext jiraServiceContext, final String fieldName, final String errorMsg)
    {
        final Map errorMessages = jiraServiceContext.getErrorCollection().getErrors();
        assertEquals(1, errorMessages.size());
        assertEquals(errorMsg, errorMessages.get(fieldName));
    }

    @Test
    public void testDoMappingNo() throws IOException, SAXException
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final I18nHelper i18n = new MockI18nBean();

        final MockControl mockProjectImportManagerControl = MockControl.createStrictControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();

        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0);
        final MappingResult initialMappingResult = new MappingResult();

        final ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl("/some/path", null);
        final BackupSystemInformationImpl backupSystemImpl = getBackupSystemInformationImpl();

        mockProjectImportManager.autoMapAndValidateIssueTypes(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setIssueTypeMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapAndValidateCustomFields(projectImportData, initialMappingResult, backupProject, i18n);
        initialMappingResult.setCustomFieldMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapSystemFields(projectImportData, backupProject);
        initialMappingResult.setPriorityMessageSet(new MessageSetImpl());
        initialMappingResult.setStatusMessageSet(new MessageSetImpl());
        initialMappingResult.setResolutionMessageSet(new MessageSetImpl());
        initialMappingResult.setGroupMessageSet(new MessageSetImpl());
        initialMappingResult.setIssueLinkTypeMessageSet(new MessageSetImpl());
        initialMappingResult.setUserMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapProjectRoles(projectImportData);
        initialMappingResult.setProjectRoleMessageSet(new MessageSetImpl());
        initialMappingResult.setProjectRoleActorMessageSet(new MessageSetImpl());
        initialMappingResult.setIssueSecurityLevelMessageSet(new MessageSetImpl());

        mockProjectImportManager.autoMapCustomFieldOptions(projectImportData, backupProject);

        mockProjectImportManager.validateCustomFieldValues(projectImportData, initialMappingResult, backupProject, null, i18n);

        mockProjectImportManager.validateSystemFields(projectImportData, initialMappingResult, projectImportOptions, backupProject, null, i18n);

        mockProjectImportManagerControl.replay();

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        DefaultProjectImportService projectImportService = new DefaultProjectImportService((PermissionManager)mockPermissionManager.proxy(), mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            MappingResult buildMappingResult()
            {
                return initialMappingResult;
            }
        };

        JiraServiceContext serviceContext = new JiraServiceContextImpl(mockUser, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return i18n;
            }
        };

        final MappingResult mappingResult = projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, backupProject, backupSystemImpl, null);

        assertTrue(mappingResult.canImport());
        mockPermissionManager.verify();
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testCreateValidationMessageListAttachmentsNotIncluded() throws IOException
    {
        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(projectImportMapper, null, 0, 0, 0, 0, 0);
        final MappingResult mappingResult = new MappingResult();
        // Make out we have validated Issue Types with no problems
        mappingResult.setIssueTypeMessageSet(new MessageSetImpl());
        // Make Custom Fields have errors and warnings
        final MessageSetImpl customFieldMessageSet = new MessageSetImpl();
        customFieldMessageSet.addErrorMessage("Custom Field 'knob' has green dots.");
        customFieldMessageSet.addWarningMessage("Eat hot lead cowbuy.");
        customFieldMessageSet.addWarningMessage("You Suck");
        mappingResult.setCustomFieldMessageSet(customFieldMessageSet);

        // Add some custom field values
        final MessageSetImpl fredMessageSet = new MessageSetImpl();
        fredMessageSet.addErrorMessage("Not happy Jan!");
        mappingResult.setCustomFieldValueMessageSets(MapBuilder.<String, MessageSet>build("12", fredMessageSet));
        projectImportData.getProjectImportMapper().getCustomFieldMapper().registerOldValue("12", "Fred");

        defaultProjectImportService.createValidationMessageList(mappingResult, projectImportData, new MockI18nBean());

        // Now assert the list:
        List messages = mappingResult.getSystemFieldsMessageList();
        Iterator iter = messages.iterator();
        // Issue Type
        MappingResult.ValidationMessage validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Issue Type", validationMessage.getDisplayName());
        assertFalse(validationMessage.getMessageSet().hasAnyMessages());
        // Custom Field
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Custom Field Configuration", validationMessage.getDisplayName());
        assertTrue(validationMessage.getMessageSet().hasAnyErrors());
        assertTrue(validationMessage.getMessageSet().hasAnyWarnings());
        // Status
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Status", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Priority
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Priority", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Resolution
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Resolution", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Users
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Users", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // ProjectRole
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Project Role", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // ProjectRoleActor
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Project Role Membership", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Group
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Group", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Issue Link Type
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Issue Link Type", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Issue Security Level
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Issue Security Level", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // File Attachments
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Attachments", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Assert end of list
        assertFalse(iter.hasNext());

        List custMessage = mappingResult.getCustomFieldsMessageList();
        // Custom Field Values for Fred
        validationMessage = (MappingResult.ValidationMessage) custMessage.iterator().next();
        assertEquals("Fred", validationMessage.getDisplayName());
        assertTrue(validationMessage.isValidated());
        assertEquals("Not happy Jan!", validationMessage.getMessageSet().getErrorMessages().iterator().next());
    }
    
    @Test
    public void testCreateValidationMessageList() throws Exception
    {
        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(projectImportMapper, null, 0, 0, 0, 0, 0);
        final MappingResult mappingResult = new MappingResult();
        // Make out we have validated Issue Types with no problems
        mappingResult.setIssueTypeMessageSet(new MessageSetImpl());
        // Make Custom Fields have errors and warnings
        final MessageSetImpl customFieldMessageSet = new MessageSetImpl();
        customFieldMessageSet.addErrorMessage("Custom Field 'knob' has green dots.");
        customFieldMessageSet.addWarningMessage("Eat hot lead cowbuy.");
        customFieldMessageSet.addWarningMessage("You Suck");
        mappingResult.setCustomFieldMessageSet(customFieldMessageSet);

        // Add some custom field values
        final MessageSetImpl fredMessageSet = new MessageSetImpl();
        fredMessageSet.addErrorMessage("Not happy Jan!");
        mappingResult.setCustomFieldValueMessageSets(MapBuilder.<String, MessageSet>build("12", fredMessageSet));
        projectImportData.getProjectImportMapper().getCustomFieldMapper().registerOldValue("12", "Fred");

        defaultProjectImportService.createValidationMessageList(mappingResult, projectImportData, new MockI18nBean());

        // Now assert the list:
        List messages = mappingResult.getSystemFieldsMessageList();
        Iterator iter = messages.iterator();
        // Issue Type
        MappingResult.ValidationMessage validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Issue Type", validationMessage.getDisplayName());
        assertFalse(validationMessage.getMessageSet().hasAnyMessages());
        // Custom Field
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Custom Field Configuration", validationMessage.getDisplayName());
        assertTrue(validationMessage.getMessageSet().hasAnyErrors());
        assertTrue(validationMessage.getMessageSet().hasAnyWarnings());
        // Status
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Status", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Priority
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Priority", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Resolution
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Resolution", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Users
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Users", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // ProjectRole
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Project Role", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // ProjectRoleActor
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Project Role Membership", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Group
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Group", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Issue Link Type
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Issue Link Type", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // Issue Security Level
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Issue Security Level", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());
        // File Attachments
        validationMessage = (MappingResult.ValidationMessage) iter.next();
        assertEquals("Attachments", validationMessage.getDisplayName());
        assertFalse(validationMessage.isValidated());

        List custMessage = mappingResult.getCustomFieldsMessageList();
        // Custom Field Values for Fred
        validationMessage = (MappingResult.ValidationMessage) custMessage.iterator().next();
        assertEquals("Fred", validationMessage.getDisplayName());
        assertTrue(validationMessage.isValidated());
        assertEquals("Not happy Jan!", validationMessage.getMessageSet().getErrorMessages().iterator().next());

        // Assert end of list
        assertFalse(iter.hasNext());
    }

    @Test
    public void testDoImportHappyPath() throws ExternalException, IndexException, IOException, SAXException {
        final MockJiraServiceContext mockJiraServiceContext = new MockJiraServiceContext();
        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, mockJiraServiceContext.getI18nBean());

        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/path", "/attach/path");

        final MockControl mockProjectImportManagerControl = MockControl.createControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();
        mockProjectImportManager.createMissingUsers(projectImportMapper.getUserMapper(), projectImportResults, null);
        mockProjectImportManager.importProject(projectImportOptions, projectImportMapper, backupProject, projectImportResults, null);
        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        final ProjectImportDataImpl importData = new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0);
        final BackupSystemInformationImpl backupSystemInfo = getBackupSystemInformationImpl();
        mockProjectImportManager.doImport(projectImportOptions, importData, backupProject, backupSystemInfo, projectImportResults, null, mockJiraServiceContext.getI18nBean(), mockJiraServiceContext.getLoggedInUser());
        mockProjectImportManagerControl.replay();

        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            boolean userHasSysAdminPermission(User user) {
                return true;
            }
            boolean isExternalUserManagementEnabled()
            {
                return false;
            }

            ProjectImportResults getInitialImportResults(final ProjectImportData projectImportData, final I18nHelper i18n, final int usersToCreate)
            {
                return projectImportResults;
            }
        };

        defaultProjectImportService.doImport(mockJiraServiceContext, projectImportOptions, backupProject, backupSystemInfo, importData, null);

        assertTrue(projectImportResults.isImportCompleted());
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoImportNullProjectImportOption() throws Exception
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        try
        {
            defaultProjectImportService.doImport(new MockJiraServiceContext(), null, backupProject, getBackupSystemInformationImpl(), new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0), null);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testDoImportNullBackupSystemInformation() throws Exception
    {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, null, null, null, null, null, null, null, null, null, null, buildUtilsInfo);

        try
        {
            defaultProjectImportService.doImport(new MockJiraServiceContext(), new ProjectImportOptionsImpl("", ""), backupProject, null, new ProjectImportDataImpl(new ProjectImportMapperImpl(null, null), null, 0, 0, 0, 0, 0), null);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testDoImportNoPermission() throws ExternalException, IndexException, IOException, SAXException {
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final MockControl mockProjectImportManagerControl = MockControl.createControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();
        mockProjectImportManagerControl.replay();

        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            boolean userHasSysAdminPermission(User user) {
                return false;
            }

            boolean isExternalUserManagementEnabled()
            {
                return false;
            }
        };

        MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        defaultProjectImportService.doImport(jiraServiceContext, new ProjectImportOptionsImpl(null, null), backupProject, getBackupSystemInformationImpl(), new ProjectImportDataImpl(projectImportMapper, null, 0, 0, 0, 0, 0), null);
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(),  "You must be a JIRA System Administrator to perform a project import.");

        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoImportErrorCreatingProject() throws ExternalException, IndexException, IOException, SAXException {
        final MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, jiraServiceContext.getI18nBean());

        ProjectImportOptionsImpl projectImportOptions = new ProjectImportOptionsImpl(null, null);

        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final MockControl mockProjectImportManagerControl = MockControl.createControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();
        mockProjectImportManager.importProject(projectImportOptions, projectImportMapper, backupProject, projectImportResults, null);
        mockProjectImportManagerControl.setThrowable(new AbortImportException());
        mockProjectImportManagerControl.replay();

        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            boolean userHasSysAdminPermission(User user) {
                return true;
            }
            boolean isExternalUserManagementEnabled()
            {
                return true;
            }
            ProjectImportResults getInitialImportResults(final ProjectImportData projectImportData, final I18nHelper i18n, final int usersToCreate)
            {
                return projectImportResults;
            }
        };

        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        defaultProjectImportService.doImport(jiraServiceContext, projectImportOptions, backupProject, getBackupSystemInformationImpl(), new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0), null);
        assertEquals(2, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertTrue(jiraServiceContext.getErrorCollection().getErrorMessages().contains("There was a problem creating/updating the project and its details."));
        assertTrue(jiraServiceContext.getErrorCollection().getErrorMessages().contains("The import was aborted because there were too many errors. Some errors are listed below. For full details about the errors please see your logs. Please note that some elements have been created. You may want to consult your logs to see what caused the errors, delete the project, and perform the import again."));

        assertFalse(projectImportResults.isImportCompleted());
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoImportIOException() throws ExternalException, IndexException, IOException, SAXException {
        final MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, jiraServiceContext.getI18nBean());
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/path", "/attach/path");

        final MockControl mockProjectImportManagerControl = MockControl.createControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();
        mockProjectImportManager.importProject(projectImportOptions, projectImportMapper, backupProject, projectImportResults, null);
        final BackupSystemInformationImpl backupSystemInfo = getBackupSystemInformationImpl();
        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        final ProjectImportDataImpl projectImportData = new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0);
        mockProjectImportManager.doImport(projectImportOptions, projectImportData, backupProject, backupSystemInfo, projectImportResults, null, jiraServiceContext.getI18nBean(), jiraServiceContext.getLoggedInUser());
        mockProjectImportManagerControl.setThrowable(new IOException("Whoops"));
        mockProjectImportManagerControl.replay();

        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            boolean userHasSysAdminPermission(User user) {
                return true;
            }
            boolean isExternalUserManagementEnabled()
            {
                return true;
            }
            ProjectImportResults getInitialImportResults(final ProjectImportData projectImportData, final I18nHelper i18n, final int usersToCreate)
            {
                return projectImportResults;
            }
        };

        defaultProjectImportService.doImport(jiraServiceContext, projectImportOptions, backupProject, backupSystemInfo, projectImportData, null);
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(),  "There was a problem accessing the partitioned XML files: Whoops.");

        assertFalse(projectImportResults.isImportCompleted());
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoImportSAXException() throws ExternalException, IndexException, IOException, SAXException {
        MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, jiraServiceContext.getI18nBean());
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/path", "/attach/path");

        final MockControl mockProjectImportManagerControl = MockControl.createControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();
        mockProjectImportManager.importProject(projectImportOptions, projectImportMapper, backupProject, projectImportResults, null);
        final BackupSystemInformationImpl backupSystemInfo = getBackupSystemInformationImpl();
        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles
                ("TST");
        mockProjectImportManager.doImport(projectImportOptions, new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0), backupProject, backupSystemInfo, projectImportResults, null, jiraServiceContext.getI18nBean(), jiraServiceContext.getLoggedInUser());
        mockProjectImportManagerControl.setThrowable(new SAXException("Whoops"));
        mockProjectImportManagerControl.replay();

        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            boolean userHasSysAdminPermission(User user) {
                return true;
            }
            boolean isExternalUserManagementEnabled()
            {
                return true;
            }
            ProjectImportResults getInitialImportResults(final ProjectImportData projectImportData, final I18nHelper i18n, final int usersToCreate)
            {
                return projectImportResults;
            }
        };

        defaultProjectImportService.doImport(jiraServiceContext, projectImportOptions, backupProject, backupSystemInfo, new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0), null);
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(),  "There was a SAX parsing problem accessing the partitioned XML files: Whoops.");

        assertFalse(projectImportResults.isImportCompleted());
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoImportIndexException() throws ExternalException, IndexException, IOException, SAXException {
        MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, jiraServiceContext.getI18nBean());
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/path", "/attach/path");

        final MockControl mockProjectImportManagerControl = MockControl.createControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();
        mockProjectImportManager.importProject(projectImportOptions, projectImportMapper, backupProject, projectImportResults, null);
        final BackupSystemInformationImpl backupSystemInfo = getBackupSystemInformationImpl();
        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        mockProjectImportManager.doImport(projectImportOptions, new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0), backupProject, backupSystemInfo, projectImportResults, null, jiraServiceContext.getI18nBean(), jiraServiceContext.getLoggedInUser());
        mockProjectImportManagerControl.setThrowable(new IndexException("Whoops"));
        mockProjectImportManagerControl.replay();

        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            boolean userHasSysAdminPermission(User user) {
                return true;
            }
            boolean isExternalUserManagementEnabled()
            {
                return true;
            }
            ProjectImportResults getInitialImportResults(final ProjectImportData projectImportData, final I18nHelper i18n, final int usersToCreate)
            {
                return projectImportResults;
            }
        };

        defaultProjectImportService.doImport(jiraServiceContext, projectImportOptions, backupProject, backupSystemInfo, new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0), null);
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(),  "There was a problem reIndexing the newly imported project. Please manually perform a full system reindex: Whoops.");

        assertFalse(projectImportResults.isImportCompleted());
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoImportAbortImportxceptionFromDoImport() throws ExternalException, IndexException, IOException, SAXException
    {
        MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, jiraServiceContext.getI18nBean());
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/path", "/attach/path");

        final MockControl mockProjectImportManagerControl = MockControl.createControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();
        mockProjectImportManager.importProject(projectImportOptions, projectImportMapper, backupProject, projectImportResults, null);
        final BackupSystemInformationImpl backupSystemInfo = getBackupSystemInformationImpl();
        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        mockProjectImportManager.doImport(projectImportOptions, new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0), backupProject, backupSystemInfo, projectImportResults, null, jiraServiceContext.getI18nBean(), jiraServiceContext.getLoggedInUser());
        mockProjectImportManagerControl.setThrowable(new AbortImportException());
        mockProjectImportManagerControl.replay();

        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            boolean userHasSysAdminPermission(User user) {
                return true;
            }
            boolean isExternalUserManagementEnabled()
            {
                return true;
            }
            ProjectImportResults getInitialImportResults(final ProjectImportData projectImportData, final I18nHelper i18n, final int usersToCreate)
            {
                return projectImportResults;
            }
        };

        defaultProjectImportService.doImport(jiraServiceContext, projectImportOptions, backupProject, backupSystemInfo, new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0), null);

        assertFalse(projectImportResults.isImportCompleted());
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "The import was aborted because there were too many errors. Some errors are listed below. For full details about the errors please see your logs. Please note that some elements have been created. You may want to consult your logs to see what caused the errors, delete the project, and perform the import again.");
        mockProjectImportManagerControl.verify();
    }

    @Test
    public void testDoImportAbortImportxceptionFromCreateUsers() throws ExternalException, IndexException, IOException, SAXException
    {
        MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, jiraServiceContext.getI18nBean());
        ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/path", "/attach/path");

        final MockControl mockProjectImportManagerControl = MockControl.createControl(ProjectImportManager.class);
        final ProjectImportManager mockProjectImportManager = (ProjectImportManager) mockProjectImportManagerControl.getMock();
        final BackupSystemInformationImpl backupSystemInfo = getBackupSystemInformationImpl();
        mockProjectImportManager.createMissingUsers(projectImportMapper.getUserMapper(), projectImportResults, null);
        mockProjectImportManagerControl.setThrowable(new AbortImportException());
        mockProjectImportManagerControl.replay();

        DefaultProjectImportService defaultProjectImportService  = new DefaultProjectImportService(null, mockProjectImportManager, null, null, null, null, null, null, null, null, null, buildUtilsInfo)
        {
            boolean userHasSysAdminPermission(User user) {
                return true;
            }
            boolean isExternalUserManagementEnabled()
            {
                return false;
            }
            ProjectImportResults getInitialImportResults(final ProjectImportData projectImportData, final I18nHelper i18n, final int usersToCreate)
            {
                return projectImportResults;
            }
        };

        ProjectImportTemporaryFiles temporaryFiles = new MockProjectImportTemporaryFiles("TST");
        defaultProjectImportService.doImport(jiraServiceContext, projectImportOptions, backupProject, backupSystemInfo, new ProjectImportDataImpl(projectImportMapper, temporaryFiles, 0, 0, 0, 0, 0), null);

        assertFalse(projectImportResults.isImportCompleted());
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertErrorCollectionContainsOnlyError(jiraServiceContext, "The import was aborted because there were too many errors. Some errors are listed below. For full details about the errors please see your logs. Please note that some elements have been created. You may want to consult your logs to see what caused the errors, delete the project, and perform the import again.");
        mockProjectImportManagerControl.verify();
    }
}
