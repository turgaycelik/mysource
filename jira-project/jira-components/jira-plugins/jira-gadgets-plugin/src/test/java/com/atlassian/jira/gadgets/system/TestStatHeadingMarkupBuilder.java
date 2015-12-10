package com.atlassian.jira.gadgets.system;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentImpl;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.gadgets.system.util.StatsMarkupFieldValueToDisplayTransformer;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.ObjectToFieldValueMapper;
import com.atlassian.jira.issue.status.SimpleStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import org.ofbiz.core.entity.GenericValue;

import junit.framework.TestCase;

import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.anyObject;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;

/**
 *
 */
public class TestStatHeadingMarkupBuilder extends TestCase
{
    private static final String BASE_URL = "http://www.lolcats.com";
    private static final String FULL_URL = BASE_URL + "/someUrl";

    JiraAuthenticationContext authenticationContext = createMock(JiraAuthenticationContext.class);
    I18nHelper i18n = createMock(I18nHelper.class);
    ConstantsManager constantsManager = createMock(ConstantsManager.class);
    CustomFieldManager cfm = createMock(CustomFieldManager.class);
    SoyTemplateRendererProvider soyTemplateRendererProvider = createMock(SoyTemplateRendererProvider.class);

    private User user;
    private SoyTemplateRenderer soyTemplateRenderer;
    private FieldValueToDisplayTransformer<StatsMarkup> transformer;

    /*public void testNumberResults() {
        TwoDimensionalStatsMap map = new TwoDimensionalStatsMap(new MockStatsMapper());
    }*/


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        user = new MockUser("fooGuy", "Foo Guy", "foo@bar.com");
        soyTemplateRenderer = createMock(SoyTemplateRenderer.class);

        transformer = new StatsMarkupFieldValueToDisplayTransformer(authenticationContext, constantsManager, cfm, null, soyTemplateRendererProvider);

        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        expect(authenticationContext.getI18nHelper()).andReturn(i18n);
        expect(soyTemplateRendererProvider.getRenderer()).andReturn(soyTemplateRenderer);

        replay(authenticationContext);
    }

    public void testHeadingMarkupForProjectGV()
    {
        Map theMap = new HashMap();
        theMap.put("name", "theProject");
        GenericValue value = new MockGenericValue("dubya", theMap);
        String s = ObjectToFieldValueMapper.transform("project", value, FULL_URL, transformer).getHtml();
        assertEquals("<a href='" + FULL_URL + "'>theProject</a>", s);
    }

    public void testHeadingMarkupForProject()
    {
        Map theMap = new HashMap();
        theMap.put("name", "theProject");
        GenericValue value = new MockGenericValue("dubya", theMap);
        Project project = new ProjectImpl(value);
        String s = ObjectToFieldValueMapper.transform("project", project, FULL_URL, transformer).getHtml();
        assertEquals("<a href='" + FULL_URL + "'>theProject</a>", s);
    }

    public void testHeadingMarkupForAssigness()
    {

        String s = ObjectToFieldValueMapper.transform("assignees", user, FULL_URL, transformer).getHtml();

        assertEquals("<a href='" + FULL_URL + "'>Foo Guy</a>", s);

    }

    public void testHeadingMarkupForNoAssignee()
    {
        expect(i18n.getText("gadget.filterstats.assignee.unassigned")).andReturn("nouser");

        replay(i18n);

        String s = ObjectToFieldValueMapper.transform("assignees", null, FULL_URL, transformer).getHtml();

        assertEquals("<a href='" + FULL_URL + "'>nouser</a>", s);
    }

    public void testHeadingMarkupForResolutionGV()
    {

        final IssueConstant issueConstant = createMock(IssueConstant.class);

        expect(issueConstant.getNameTranslation()).andStubReturn("theNameTranslated");
        expect(issueConstant.getDescTranslation()).andStubReturn("theDescTranslated");
        expect(issueConstant.getIconUrl()).andStubReturn("http://jira/icon.gif");

        replay(issueConstant);


        expect(constantsManager.getIssueConstant((GenericValue) anyObject())).andReturn(issueConstant);

        replay(constantsManager);

        String s = ObjectToFieldValueMapper.transform("resolution", new MockGenericValue("foo"), FULL_URL, transformer).getHtml();

        assertEquals("<img src=\"http://jira/icon.gif\" height=\"16\" width=\"16\" alt=\"theNameTranslated\" title=\"theNameTranslated - theDescTranslated\"/><a href='" + FULL_URL + "' title='theDescTranslated'>theNameTranslated</a>", s);

    }

    public void testHeadingMarkupForResolution()
    {

        final IssueConstant issueConstant = createMock(IssueConstant.class);

        expect(issueConstant.getNameTranslation()).andStubReturn("theNameTranslated");
        expect(issueConstant.getDescTranslation()).andStubReturn("theDescTranslated");
        expect(issueConstant.getIconUrl()).andStubReturn("http://jira/icon.gif");

        replay(issueConstant);


        String s = ObjectToFieldValueMapper.transform("resolution", issueConstant, FULL_URL, transformer).getHtml();

        assertEquals("<img src=\"http://jira/icon.gif\" height=\"16\" width=\"16\" alt=\"theNameTranslated\" title=\"theNameTranslated - theDescTranslated\"/><a href='" + FULL_URL + "' title='theDescTranslated'>theNameTranslated</a>", s);

    }

    public void testHeadingMarkupForNoResolution()
    {
        expect(i18n.getText("common.resolution.unresolved")).andReturn("nouser");

        replay(i18n);

        String s = ObjectToFieldValueMapper.transform("resolution", null, FULL_URL, transformer).getHtml();

        assertEquals("<a href='" + FULL_URL + "'>nouser</a>", s);
    }

    /*
     * Check here if soy renderer is called once and transformer returns correct markup
     */
    public void testHeadingMarkupForStatusesGV() throws SoyException
    {
        final IssueConstant issueConstant = createMock(IssueConstant.class);
        final Status status = createMock(Status.class);
        final SimpleStatus simpleStatus = createMock(SimpleStatus.class);

        expect(issueConstant.getId()).andReturn("1");

        expect(status.getSimpleStatus()).andReturn(simpleStatus);
        expect(constantsManager.getIssueConstant((GenericValue) anyObject())).andReturn(issueConstant);
        expect(constantsManager.getStatusObject((String) anyObject())).andReturn(status);
        expect(soyTemplateRenderer.render((String) anyObject(), (String) anyObject(), (Map) anyObject())).andReturn("<span>Status lozenge</span>").once();

        replay(issueConstant, status, constantsManager, soyTemplateRenderer, soyTemplateRendererProvider);

        final String html = ObjectToFieldValueMapper.transform("statuses", issueConstant, FULL_URL, transformer).getHtml();
        assertEquals("<span>Status lozenge</span>", html);

        verify(soyTemplateRenderer);
    }

    /*
     * Check here if soy renderer is called once and transformer returns correct markup
     */
    public void testHeadingMarkupForStatuses() throws SoyException
    {
        final IssueConstant issueConstant = createMock(IssueConstant.class);
        final Status status = createMock(Status.class);
        final SimpleStatus simpleStatus = createMock(SimpleStatus.class);

        expect(issueConstant.getId()).andReturn("1");

        expect(status.getSimpleStatus()).andReturn(simpleStatus);
        expect(constantsManager.getStatusObject((String) anyObject())).andReturn(status);
        expect(soyTemplateRenderer.render((String) anyObject(), (String) anyObject(), (Map) anyObject())).andReturn("<span>Status lozenge</span>").once();

        replay(issueConstant, status, constantsManager, soyTemplateRenderer, soyTemplateRendererProvider);

        final String html = ObjectToFieldValueMapper.transform("statuses", issueConstant, FULL_URL, transformer).getHtml();
        assertEquals("<span>Status lozenge</span>", html);

        verify(soyTemplateRenderer);
    }

    public void testHeadingMarkupForIssueTypesGV()
    {
        final IssueConstant issueConstant = createMock(IssueConstant.class);

        expect(issueConstant.getNameTranslation()).andStubReturn("theNameTranslated");
        expect(issueConstant.getDescTranslation()).andStubReturn("theDescTranslated");
        expect(issueConstant.getIconUrl()).andStubReturn("http://jira/icon.gif");

        expect(constantsManager.getIssueConstant((GenericValue) anyObject())).andReturn(issueConstant);

        replay(issueConstant);
        replay(constantsManager);

        String s = ObjectToFieldValueMapper.transform("issuetype", new MockGenericValue("foo"), FULL_URL, transformer).getHtml();

        assertEquals("<img src=\"http://jira/icon.gif\" height=\"16\" width=\"16\" alt=\"theNameTranslated\" title=\"theNameTranslated - theDescTranslated\"/><a href='" + FULL_URL + "' title='theDescTranslated'>theNameTranslated</a>", s);

    }

    public void testHeadingMarkupForIssueTypes()
    {
        final IssueConstant issueConstant = createMock(IssueConstant.class);

        expect(issueConstant.getNameTranslation()).andStubReturn("theNameTranslated");
        expect(issueConstant.getDescTranslation()).andStubReturn("theDescTranslated");
        expect(issueConstant.getIconUrl()).andStubReturn("http://jira/icon.gif");

        replay(issueConstant);

        String s = ObjectToFieldValueMapper.transform("issuetype", issueConstant, FULL_URL, transformer).getHtml();

        assertEquals("<img src=\"http://jira/icon.gif\" height=\"16\" width=\"16\" alt=\"theNameTranslated\" title=\"theNameTranslated - theDescTranslated\"/><a href='" + FULL_URL + "' title='theDescTranslated'>theNameTranslated</a>", s);

    }

    public void testHeadingMarkupForPrioritiesGV()
    {
        final IssueConstant issueConstant = createMock(IssueConstant.class);

        expect(issueConstant.getNameTranslation()).andStubReturn("theNameTranslated");
        expect(issueConstant.getDescTranslation()).andStubReturn("theDescTranslated");
        expect(issueConstant.getIconUrl()).andStubReturn("http://jira/icon.gif");

        expect(constantsManager.getIssueConstant((GenericValue) anyObject())).andReturn(issueConstant);

        replay(issueConstant);
        replay(constantsManager);

        String s = ObjectToFieldValueMapper.transform("priorities", new MockGenericValue("foo"), FULL_URL, transformer).getHtml();

        assertEquals("<img src=\"http://jira/icon.gif\" height=\"16\" width=\"16\" alt=\"theNameTranslated\" title=\"theNameTranslated - theDescTranslated\"/><a href='" + FULL_URL + "' title='theDescTranslated'>theNameTranslated</a>", s);

    }

    public void testHeadingMarkupForPriorities()
    {
        final IssueConstant issueConstant = createMock(IssueConstant.class);

        expect(issueConstant.getNameTranslation()).andStubReturn("theNameTranslated");
        expect(issueConstant.getDescTranslation()).andStubReturn("theDescTranslated");
        expect(issueConstant.getIconUrl()).andStubReturn("http://jira/icon.gif");

        replay(issueConstant);
        String s = ObjectToFieldValueMapper.transform("priorities", issueConstant, FULL_URL, transformer).getHtml();

        assertEquals("<img src=\"http://jira/icon.gif\" height=\"16\" width=\"16\" alt=\"theNameTranslated\" title=\"theNameTranslated - theDescTranslated\"/><a href='" + FULL_URL + "' title='theDescTranslated'>theNameTranslated</a>", s);

    }

    public void testHeadingMarkupForNoPriority()
    {
        expect(i18n.getText("gadget.filterstats.priority.nopriority")).andReturn("nopriority");

        replay(i18n);

        String s = ObjectToFieldValueMapper.transform("priorities", null, FULL_URL, transformer).getHtml();

        assertEquals("<a href='" + FULL_URL + "'>nopriority</a>", s);

    }

    public void testHeadingMarkupForComponentsGV()
    {
        Map theMap = new HashMap();
        theMap.put("name", "theComponent");
        GenericValue value = new MockGenericValue("dubya", theMap);

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("components", value, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "'>theComponent</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("default_image_component"));
    }

    public void testHeadingMarkupForComponents()
    {
        ProjectComponent comp = new ProjectComponentImpl("theComponent", "theComponentDesc", null, 1);

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("components", comp, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='theComponentDesc'>theComponent</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("default_image_component"));
    }

    public void testHeadingMarkupForNoComponent()
    {
        expect(i18n.getText("gadget.filterstats.component.nocomponent")).andReturn("nocomps");

        replay(i18n);

        String s = ObjectToFieldValueMapper.transform("components", null, FULL_URL, transformer).getHtml();

        assertEquals("<a href='" + FULL_URL + "'>nocomps</a>", s);

    }

    public void testHeadingMarkupForVersionArchivedAndReleased()
    {
        Version version = makeVersion(true, true, "archivedAndReleased", "archivedAndReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("version", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='archivedAndReleasedDesc'>archivedAndReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("released_archived_version"));

    }

    public void testHeadingMarkupForAllVersionArchivedAndReleased()
    {
        Version version = makeVersion(true, true, "archivedAndReleased", "archivedAndReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("allVersion", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='archivedAndReleasedDesc'>archivedAndReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("released_archived_version"));

    }

    public void testHeadingMarkupForVersionNotArchivedAndReleased()
    {
        Version version = makeVersion(false, true, "notArchivedAndReleased", "notArchivedAndReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("version", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='notArchivedAndReleasedDesc'>notArchivedAndReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("released_unarchived_version"));

    }

    public void testHeadingMarkupForAllVersionNotArchivedAndReleased()
    {
        Version version = makeVersion(false, true, "notArchivedAndReleased", "notArchivedAndReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("allVersion", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='notArchivedAndReleasedDesc'>notArchivedAndReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("released_unarchived_version"));

    }

    public void testHeadingMarkupForVersionArchivedAndNotReleased()
    {
        Version version = makeVersion(true, false, "archivedAndNotReleased", "archivedAndNotReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("version", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='archivedAndNotReleasedDesc'>archivedAndNotReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("unreleased_archived_version"));

    }

    public void testHeadingMarkupForAllVersionArchivedAndNotReleased()
    {
        Version version = makeVersion(true, false, "archivedAndNotReleased", "archivedAndNotReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("allVersion", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='archivedAndNotReleasedDesc'>archivedAndNotReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("unreleased_archived_version"));

    }

    public void testHeadingMarkupForVersionNotArchivedAndNotReleased()
    {
        Version version = makeVersion(false, false, "notArchivedAndNotReleased", "notArchivedAndNotReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("version", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='notArchivedAndNotReleasedDesc'>notArchivedAndNotReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("unreleased_unarchived_version"));

    }

    public void testHeadingMarkupForAllVersionNotArchivedAndNotReleased()
    {
        Version version = makeVersion(false, false, "notArchivedAndNotReleased", "notArchivedAndNotReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("allVersion", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='notArchivedAndNotReleasedDesc'>notArchivedAndNotReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("unreleased_unarchived_version"));

    }
    public void testHeadingMarkupForNoVersions()
    {
        Version version = null;
        expect(i18n.getText("gadget.filterstats.raisedin.unscheduled")).andReturn("none");

        replay(i18n);

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("version", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "'>none</a>", s);

    }

    public void testHeadingMarkupForFixForArchivedAndReleased()
    {
        Version version = makeVersion(true, true, "archivedAndReleased", "archivedAndReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("fixfor", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='archivedAndReleasedDesc'>archivedAndReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("released_archived_version"));

    }

    public void testHeadingMarkupForAllFixForArchivedAndReleased()
    {
        Version version = makeVersion(true, true, "archivedAndReleased", "archivedAndReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("allFixfor", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='archivedAndReleasedDesc'>archivedAndReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("released_archived_version"));

    }

    public void testHeadingMarkupForFixForNotArchivedAndReleased()
    {
        Version version = makeVersion(false, true, "notArchivedAndReleased", "notArchivedAndReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("fixfor", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='notArchivedAndReleasedDesc'>notArchivedAndReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("released_unarchived_version"));

    }

    public void testHeadingMarkupForAllFixForNotArchivedAndReleased()
    {
        Version version = makeVersion(false, true, "notArchivedAndReleased", "notArchivedAndReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("allFixfor", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='notArchivedAndReleasedDesc'>notArchivedAndReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("released_unarchived_version"));

    }

    public void testHeadingMarkupForFixForArchivedAndNotReleased()
    {
        Version version = makeVersion(true, false, "archivedAndNotReleased", "archivedAndNotReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("fixfor", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='archivedAndNotReleasedDesc'>archivedAndNotReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("unreleased_archived_version"));

    }

    public void testHeadingMarkupForAllFixForArchivedAndNotReleased()
    {
        Version version = makeVersion(true, false, "archivedAndNotReleased", "archivedAndNotReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("allFixfor", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='archivedAndNotReleasedDesc'>archivedAndNotReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("unreleased_archived_version"));

    }

    public void testHeadingMarkupForFixForNotArchivedAndNotReleased()
    {
        Version version = makeVersion(false, false, "notArchivedAndNotReleased", "notArchivedAndNotReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("fixfor", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='notArchivedAndNotReleasedDesc'>notArchivedAndNotReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("unreleased_unarchived_version"));

    }

    public void testHeadingMarkupForAllFixForNotArchivedAndNotReleased()
    {
        Version version = makeVersion(false, false, "notArchivedAndNotReleased", "notArchivedAndNotReleasedDesc");

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("allFixfor", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "' title='notArchivedAndNotReleasedDesc'>notArchivedAndNotReleased</a>", s);
        assertTrue(statsMarkup.getClasses().contains("default_image"));
        assertTrue(statsMarkup.getClasses().contains("unreleased_unarchived_version"));

    }
    public void testHeadingMarkupForNoFixFor()
    {
        Version version = null;
        expect(i18n.getText("gadget.filterstats.fixfor.unscheduled")).andReturn("none");

        replay(i18n);

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("fixfor", version, FULL_URL, transformer);

        String s = statsMarkup.getHtml();
        assertEquals("<a href='" + FULL_URL + "'>none</a>", s);

    }


    private Version makeVersion(boolean archived, boolean released, String name, String desc)
    {

        Version version = createMock(Version.class);
        expect(version.isArchived()).andStubReturn(archived);
        expect(version.isReleased()).andStubReturn(released);
        expect(version.getName()).andStubReturn(name);
        expect(version.getDescription()).andStubReturn(desc);

        replay(version);

        return version;
    }

    public void testHeadingMarkupForCustomFields()
    {

        CustomField cf = createMock(CustomField.class);
        CustomFieldSearcher cfs = createMock(CustomFieldSearcher.class);
        CustomFieldSearcherModuleDescriptor cfmd = createMock(CustomFieldSearcherModuleDescriptor.class);

        expect(cfm.getCustomFieldObject("customfield_nick")).andReturn(cf);
        replay(cfm);

        expect(cf.getCustomFieldSearcher()).andReturn(cfs);
        replay(cf);

        expect(cfs.getDescriptor()).andReturn(cfmd);
        replay(cfs);

        expect(cfmd.getStatHtml(cf, "nick", FULL_URL)).andReturn("CustomField Markup");
        replay(cfmd);

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("customfield_nick", "nick", FULL_URL, transformer);

        assertEquals("CustomField Markup", statsMarkup.getHtml());

    }
    public void testHeadingMarkupForNoCustomFields()
    {


        expect(cfm.getCustomFieldObject("customfield_nick")).andReturn(null);
        replay(cfm);

        expect(i18n.getText("common.words.none")).andReturn("none");

        replay(i18n);

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("customfield_nick", "nick", FULL_URL, transformer);

        assertEquals("<a href='" + FULL_URL + "'>none</a>", statsMarkup.getHtml());

    }
    public void testHeadingMarkupForNoValueCustomFields()
    {
        expect(i18n.getText("common.words.none")).andReturn("none");

        replay(i18n);

        final StatsMarkup statsMarkup = ObjectToFieldValueMapper.transform("customfield_nick", null, FULL_URL, transformer);

        assertEquals("<a href='" + FULL_URL + "'>none</a>", statsMarkup.getHtml());

    }

}
