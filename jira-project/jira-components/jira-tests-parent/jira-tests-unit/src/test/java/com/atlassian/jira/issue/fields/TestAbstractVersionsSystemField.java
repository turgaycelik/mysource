package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

public class TestAbstractVersionsSystemField
{
    private AbstractVersionsSystemField versionSystemField;

    private I18nHelper i18n = new MockI18nHelper();
    @Mock
    private FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem;
    @Mock
    private LongIdsValueHolder versionIds;
    @Mock
    private Project project;
    @Mock
    private Issue issue;
    @Mock
    private VersionManager versionManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(issue.getProjectObject()).thenReturn(project);
        when(versionManager.getVersionsArchived(project)).thenReturn(Lists.<Version>newArrayList());

        this.versionSystemField = new TestableVersionSystemField("fixfor", "Fix For", versionManager);
    }

    @Test
    public void fieldValidationPassesWhenVersionFieldIsNotRequired()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(false);

        final ErrorCollection errors = new SimpleErrorCollection();
        versionSystemField.validateForRequiredField(errors, i18n, issue, fieldScreenRenderLayoutItem, versionIds);

        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void fieldValidationPassesWhenExistingVersionProvided()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(true);
        when(versionIds.isEmpty()).thenReturn(false);

        final ErrorCollection errors = new SimpleErrorCollection();
        versionSystemField.validateForRequiredField(errors, i18n, issue, fieldScreenRenderLayoutItem, versionIds);

        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void fieldValidationPassesWhenUnknownVersionsProvidedDuringBulkOperation()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(true);
        when(versionIds.contains(-1L)).thenReturn(false);

        final ErrorCollection errors = new SimpleErrorCollection();
        versionSystemField.validateForRequiredField(errors, i18n, issue, fieldScreenRenderLayoutItem, versionIds);

        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void fieldValidationPassesWhenNewVersionsProvided()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(true);
        when(versionIds.getValuesToAdd()).thenReturn(Sets.newHashSet("1.0"));

        final ErrorCollection errors = new SimpleErrorCollection();
        versionSystemField.validateForRequiredField(errors, i18n, issue, fieldScreenRenderLayoutItem, versionIds);

        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void fieldValidationFailsWhenNoVersionsProvided()
    {
        when(fieldScreenRenderLayoutItem.isRequired()).thenReturn(true);
        when(versionIds.isEmpty()).thenReturn(true);
        when(project.getId()).thenReturn(123L);

        final ErrorCollection errors = new SimpleErrorCollection();
        versionSystemField.validateForRequiredField(errors, i18n, issue, fieldScreenRenderLayoutItem, versionIds);

        assertEquals("field.required.message", errors.getErrors().get("fixfor"));
    }

    private static class TestableVersionSystemField extends AbstractVersionsSystemField
    {
        public TestableVersionSystemField(
                final String id,
                final String name,
                final VersionManager versionManager)
        {
            super(id, name, null, null, versionManager, null, null, null, null, null);
        }

        @Override
        protected Collection<Version> getCurrentVersions(final Issue issue)
        {
            return null;
        }

        @Override
        protected String getArchivedVersionsFieldTitle()
        {
            return null;
        }

        @Override
        protected String getArchivedVersionsFieldSearchParam()
        {
            return null;
        }

        @Override
        protected boolean getUnreleasedVersionsFirst()
        {
            return false;
        }

        @Override
        protected void addFieldRequiredErrorMessage(final Issue issue, final ErrorCollection errorCollectionToAddTo, final I18nHelper i18n)
        {
            errorCollectionToAddTo.addError(getId(), "field.required.message");
        }

        @Override
        protected String getModifiedWithoutPermissionErrorMessage(final I18nHelper i18n)
        {
            return null;
        }

        @Override
        protected String getChangeItemFieldName()
        {
            return null;
        }

        @Override
        protected String getIssueRelationName()
        {
            return null;
        }

        @Override
        public String getColumnHeadingKey()
        {
            return null;
        }

        @Override
        public LuceneFieldSorter getSorter()
        {
            return null;
        }

        @Override
        public boolean isShown(final Issue issue)
        {
            return false;
        }

        @Override
        public void updateIssue(final FieldLayoutItem fieldLayoutItem, final MutableIssue issue, final Map fieldValueHolder)
        {

        }

        @Override
        public void removeValueFromIssueObject(final MutableIssue issue)
        {

        }

        @Override
        public boolean canRemoveValueFromIssueObject(final Issue issue)
        {
            return false;
        }
    }
}