package com.atlassian.jira.rest.v2.issue.version;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLink;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkImpl;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.rest.v2.common.SimpleLinkBean;
import com.atlassian.jira.rest.v2.entity.RemoteEntityLinkJsonBean;
import com.atlassian.jira.rest.v2.issue.VersionResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkServiceResultFactory.remoteVersionLinkListResult;
import static com.atlassian.jira.matchers.ReflectionEqualTo.reflectionEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.4
 */
@RunWith(MockitoJUnitRunner.class)
public class TestVersionBeanFactoryImpl
{
    @Mock VersionService versionService;
    @Mock RemoteVersionLinkService remoteVersionLinkService;
    @Mock JiraAuthenticationContext authContext;
    @Mock SimpleLinkManager simpleLinkManager;
    @Mock UriInfo info;
    @Mock UriBuilder builder;
    @Mock DateFieldFormat dateFieldFormat;

    @Test
    public void testCreateVersionBeanNoStartReleaseDate() throws Exception
    {
        final URI uri = new URI("http://localhost:8090/jira");
        final String pKey = "KEY";

        final MockProject project = new MockProject(67, pKey);
        final MockVersion version = new MockVersion();

        version.setId(54748L);
        version.setProjectObject(project);

        when(info.getBaseUriBuilder()).thenReturn(builder);
        when(builder.path(VersionResource.class)).thenReturn(builder);
        when(builder.path(version.getId().toString())).thenReturn(builder);
        when(builder.build()).thenReturn(uri);

        final VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, remoteVersionLinkService, info, dateFieldFormat, authContext, simpleLinkManager);
        final VersionBean bean = factory.createVersionBean(version);

        final VersionBean expected = new VersionBean.Builder().setVersion(version)
                .setProjectId(project.getId())
                .setSelf(uri)
                .build();

        assertThat(bean, reflectionEqualTo(expected));
    }

    @Test
    public void testCreateVersionBeansWithStartDate() throws Exception
    {
        final URI uri = new URI("http://localhost:8090/jira");
        final String datePretty = "Great Date";

        final MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setStartDate(new Date());

        when(info.getBaseUriBuilder()).thenReturn(builder);
        when(builder.path(VersionResource.class)).thenReturn(builder);
        when(builder.path(version.getId().toString())).thenReturn(builder);
        when(builder.build()).thenReturn(uri);

        when(dateFieldFormat.format(version.getStartDate())).thenReturn(datePretty);

        final VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, remoteVersionLinkService, info, dateFieldFormat, authContext, simpleLinkManager);
        final VersionBean bean = factory.createVersionBean(version);

        final VersionBean expected = new VersionBean.Builder().setVersion(version)
                .setUserStartDate(datePretty)
                .setSelf(uri)
                .build();

        assertThat(bean, reflectionEqualTo(expected));
    }

    @Test
    public void testCreateVersionBeansReleasedAndReleaseDate() throws Exception
    {
        final URI uri = new URI("http://localhost:8090/jira");
        final String datePretty = "Great Date";


        final MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setReleaseDate(new Date());
        version.setReleased(true);
        version.setDescription("    ");


        when(info.getBaseUriBuilder()).thenReturn(builder);
        when(builder.path(VersionResource.class)).thenReturn(builder);
        when(builder.path(version.getId().toString())).thenReturn(builder);
        when(builder.build()).thenReturn(uri);

        when(dateFieldFormat.format(version.getReleaseDate())).thenReturn(datePretty);

        final VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, remoteVersionLinkService, info, dateFieldFormat, authContext, simpleLinkManager);
        final VersionBean bean = factory.createVersionBean(version);

        final VersionBean expected = new VersionBean.Builder().setVersion(version)
                .setUserReleaseDate(datePretty)
                .setSelf(uri)
                .build();

        assertThat(bean, reflectionEqualTo(expected));
    }

    @Test
    public void testCreateVersionBeansWithReleaseDate() throws Exception
    {
        final URI uri = new URI("http://localhost:8090/jira");
        final String datePretty = "Great Date";

        final MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setReleaseDate(new Date());
        version.setReleased(false);

        when(info.getBaseUriBuilder()).thenReturn(builder);
        when(builder.path(VersionResource.class)).thenReturn(builder);
        when(builder.path(version.getId().toString())).thenReturn(builder);
        when(builder.build()).thenReturn(uri);

        when(versionService.isOverdue(version)).thenReturn(true);

        when(dateFieldFormat.format(version.getReleaseDate())).thenReturn(datePretty);

        final VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, remoteVersionLinkService, info, dateFieldFormat, authContext, simpleLinkManager);
        final VersionBean bean = factory.createVersionBean(version);

        final VersionBean expected = new VersionBean.Builder().setVersion(version)
                .setOverdue(true)
                .setUserReleaseDate(datePretty)
                .setSelf(uri)
                .build();

        assertThat(bean, reflectionEqualTo(expected));
    }

    @Test
    public void testCreateVersionBeansWithEmptyOperations() throws Exception
    {
        final URI uri = new URI("http://localhost:8090/jira");
        final String datePretty = "Great Date";

        final MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setStartDate(new Date());
        version.setReleaseDate(new Date());
        version.setReleased(false);

        final MockProject project = new MockProject(101010, "HSP", "homo sapiens");
        version.setProjectObject(project);
        final MockUser fred = new MockUser("fred");

        final JiraHelper helper = new JiraHelper(null, project, MapBuilder.build("version", version, "user", fred, "project", project));


        when(info.getBaseUriBuilder()).thenReturn(builder);
        when(builder.path(VersionResource.class)).thenReturn(builder);
        when(builder.path(version.getId().toString())).thenReturn(builder);
        when(builder.build()).thenReturn(uri);

        when(versionService.isOverdue(version)).thenReturn(true);

        when(dateFieldFormat.format(version.getStartDate())).thenReturn(datePretty);
        when(dateFieldFormat.format(version.getReleaseDate())).thenReturn(datePretty);

        when(authContext.getLoggedInUser()).thenReturn(fred);
        when(simpleLinkManager.getLinksForSection(VersionBeanFactory.VERSION_OPERATIONS_WEB_LOCATION, fred, helper)).thenReturn(new ArrayList<SimpleLink>());

        final VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, remoteVersionLinkService, info, dateFieldFormat, authContext, simpleLinkManager);
        final VersionBean bean = factory.createVersionBean(version, true, false);

        final VersionBean expected = new VersionBean.Builder().setVersion(version)
                .setOverdue(true)
                .setUserReleaseDate(datePretty)
                .setUserStartDate(datePretty)
                .setSelf(uri)
                .setProjectId(project.getId())
                .setOperations(new ArrayList<SimpleLinkBean>())
                .setExpand(VersionBean.EXPAND_OPERATIONS)
                .build();
        assertThat(bean, reflectionEqualTo(expected));
    }


    @Test
    public void testCreateVersionBeansWithSomeOperations() throws Exception
    {
        final URI uri = new URI("http://localhost:8090/jira");
        final String datePretty = "Great Date";

        final MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setStartDate(new Date());
        version.setReleaseDate(new Date());
        version.setReleased(false);

        final MockProject project = new MockProject(101010, "HSP", "homo sapiens");
        version.setProjectObject(project);
        final MockUser fred = new MockUser("fred");

        final JiraHelper helper = new JiraHelper(null, project, MapBuilder.build("version", version, "user", fred, "project", project));


        when(info.getBaseUriBuilder()).thenReturn(builder);
        when(builder.path(VersionResource.class)).thenReturn(builder);
        when(builder.path(version.getId().toString())).thenReturn(builder);
        when(builder.build()).thenReturn(uri);

        when(versionService.isOverdue(version)).thenReturn(true);

        when(dateFieldFormat.format(version.getStartDate())).thenReturn(datePretty);
        when(dateFieldFormat.format(version.getReleaseDate())).thenReturn(datePretty);

        when(authContext.getLoggedInUser()).thenReturn(fred);
        final ArrayList<SimpleLink> operations = new ArrayList<SimpleLink>();
        final SimpleLinkImpl link1 = new SimpleLinkImpl("link1", "Link One", "Link One Title", null, "style 1", "", null);
        final SimpleLinkImpl link2 = new SimpleLinkImpl("link2", "Link Two", "Link Two Title", null, "style 2", "", null);
        operations.add(link1);
        operations.add(link2);
        when(simpleLinkManager.getLinksForSection(VersionBeanFactory.VERSION_OPERATIONS_WEB_LOCATION, fred, helper)).thenReturn(operations);

        final VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, remoteVersionLinkService, info, dateFieldFormat, authContext, simpleLinkManager);
        final VersionBean bean = factory.createVersionBean(version, true, false);

        final ArrayList<SimpleLinkBean> expectedOperations = new ArrayList<SimpleLinkBean>();
        expectedOperations.add(new SimpleLinkBean(link1));
        expectedOperations.add(new SimpleLinkBean(link2));
        final VersionBean expected = new VersionBean.Builder()
                .setVersion(version)
                .setOverdue(true)
                .setUserReleaseDate(datePretty)
                .setUserStartDate(datePretty)
                .setSelf(uri)
                .setProjectId(project.getId())
                .setOperations(expectedOperations)
                .setExpand(VersionBean.EXPAND_OPERATIONS)
                .build();
        assertThat(bean, reflectionEqualTo(expected));
    }


    @Test
    public void testCreateVersionBeansWithSomeOperationsAndRemoteLinks() throws Exception
    {
        final URI uri = new URI("http://localhost:8090/jira");
        final String datePretty = "Great Date";

        final MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setStartDate(new Date());
        version.setReleaseDate(new Date());
        version.setReleased(false);
        final URI versionUri = UriBuilder.fromUri(uri).path("version").path(version.getId().toString()).build();

        final MockProject project = new MockProject(101010, "HSP", "homo sapiens");
        version.setProjectObject(project);
        final ApplicationUser appFred = new MockApplicationUser("Fred");
        final User fred = appFred.getDirectoryUser();

        final JiraHelper helper = new JiraHelper(null, project, MapBuilder.build("version", version, "user", fred, "project", project));


        when(info.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(uri));
        when(versionService.isOverdue(version)).thenReturn(true);
        when(dateFieldFormat.format(version.getStartDate())).thenReturn(datePretty);
        when(dateFieldFormat.format(version.getReleaseDate())).thenReturn(datePretty);
        when(authContext.getLoggedInUser()).thenReturn(fred);
        when(authContext.getUser()).thenReturn(appFred);

        final ArrayList<SimpleLink> operations = new ArrayList<SimpleLink>();
        final SimpleLinkImpl link1 = new SimpleLinkImpl("link1", "Link One", "Link One Title", null, "style 1", "", null);
        final SimpleLinkImpl link2 = new SimpleLinkImpl("link2", "Link Two", "Link Two Title", null, "style 2", "", null);
        operations.add(link1);
        operations.add(link2);

        final RemoteVersionLink remoteLink1 = new RemoteVersionLinkImpl(version, "gid1", "true");
        final RemoteVersionLink remoteLink2 = new RemoteVersionLinkImpl(version, "gid2", "{\"hello\":\"world\"}");
        final List<RemoteVersionLink> remoteLinks = Arrays.asList(remoteLink1, remoteLink2);

        when(simpleLinkManager.getLinksForSection(VersionBeanFactory.VERSION_OPERATIONS_WEB_LOCATION, fred, helper)).thenReturn(operations);
        when(remoteVersionLinkService.getRemoteVersionLinksByVersionId(appFred, version.getId())).thenReturn(remoteVersionLinkListResult(remoteLinks));

        final VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, remoteVersionLinkService, info, dateFieldFormat, authContext, simpleLinkManager);
        final VersionBean bean = factory.createVersionBean(version, true, true);

        final ArrayList<SimpleLinkBean> expectedOperations = new ArrayList<SimpleLinkBean>();
        expectedOperations.add(new SimpleLinkBean(link1));
        expectedOperations.add(new SimpleLinkBean(link2));

        final List<RemoteEntityLinkJsonBean> expectedRemoteLinks = new ArrayList<RemoteEntityLinkJsonBean>();
        expectedRemoteLinks.add(new RemoteEntityLinkJsonBean()
                .self(UriBuilder.fromUri(versionUri).path("remotelink").path("gid1").build())
                .name(version.getName())
                .link(remoteLink1.getJsonString()));
        expectedRemoteLinks.add(new RemoteEntityLinkJsonBean()
                .self(UriBuilder.fromUri(versionUri).path("remotelink").path("gid2").build())
                .name(version.getName())
                .link(remoteLink2.getJsonString()));

        final VersionBean expected = new VersionBean.Builder()
                .setVersion(version)
                .setOverdue(true)
                .setUserReleaseDate(datePretty)
                .setUserStartDate(datePretty)
                .setSelf(versionUri)
                .setProjectId(project.getId())
                .setOperations(expectedOperations)
                .setRemoteLinks(expectedRemoteLinks)
                .setExpand(VersionBean.EXPAND_OPERATIONS + ',' + VersionBean.EXPAND_REMOTE_LINKS)
                .build();
        assertThat(bean, reflectionEqualTo(expected));
    }

    @Test
    public void testCreateVersionBeansWithSomeRemoteLinks() throws Exception
    {
        final URI uri = new URI("http://localhost:8090/jira");
        final String datePretty = "Great Date";

        final MockVersion version = new MockVersion();
        version.setId(54748L);
        version.setStartDate(new Date());
        version.setReleaseDate(new Date());
        version.setReleased(false);
        final URI versionUri = UriBuilder.fromUri(uri).path("version").path(version.getId().toString()).build();

        final MockProject project = new MockProject(101010, "HSP", "homo sapiens");
        version.setProjectObject(project);
        final ApplicationUser appFred = new MockApplicationUser("Fred");
        final User fred = appFred.getDirectoryUser();

        when(info.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(uri));
        when(versionService.isOverdue(version)).thenReturn(true);
        when(dateFieldFormat.format(version.getStartDate())).thenReturn(datePretty);
        when(dateFieldFormat.format(version.getReleaseDate())).thenReturn(datePretty);
        when(authContext.getLoggedInUser()).thenReturn(fred);
        when(authContext.getUser()).thenReturn(appFred);

        final RemoteVersionLink remoteLink1 = new RemoteVersionLinkImpl(version, "gid1", "true");
        final RemoteVersionLink remoteLink2 = new RemoteVersionLinkImpl(version, "gid2", "{\"hello\":\"world\"}");
        final List<RemoteVersionLink> remoteLinks = Arrays.asList(remoteLink1, remoteLink2);

        when(remoteVersionLinkService.getRemoteVersionLinksByVersionId(appFred, version.getId())).thenReturn(remoteVersionLinkListResult(remoteLinks));

        final VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, remoteVersionLinkService, info, dateFieldFormat, authContext, simpleLinkManager);
        final VersionBean bean = factory.createVersionBean(version, false, true);

        final List<RemoteEntityLinkJsonBean> expectedRemoteLinks = new ArrayList<RemoteEntityLinkJsonBean>();
        expectedRemoteLinks.add(new RemoteEntityLinkJsonBean()
                .self(UriBuilder.fromUri(versionUri).path("remotelink").path("gid1").build())
                .name(version.getName())
                .link(remoteLink1.getJsonString()));
        expectedRemoteLinks.add(new RemoteEntityLinkJsonBean()
                .self(UriBuilder.fromUri(versionUri).path("remotelink").path("gid2").build())
                .name(version.getName())
                .link(remoteLink2.getJsonString()));

        final VersionBean expected = new VersionBean.Builder()
                .setVersion(version)
                .setOverdue(true)
                .setUserReleaseDate(datePretty)
                .setUserStartDate(datePretty)
                .setSelf(versionUri)
                .setProjectId(project.getId())
                .setRemoteLinks(expectedRemoteLinks)
                .setExpand(VersionBean.EXPAND_REMOTE_LINKS)
                .build();
        assertThat(bean, reflectionEqualTo(expected));
    }

    @Test
    public void testCreateVersionBeanMultiple() throws Exception
    {
        final URI uri = new URI("http://localhost:8090/jira");
        final String datePretty = "Great Date";

        final MockVersion version1 = new MockVersion();
        version1.setId(67473784534L);
        version1.setStartDate(new Date());

        final MockVersion version2 = new MockVersion();
        version2.setId(54748L);
        version2.setReleaseDate(new Date());


        when(info.getBaseUriBuilder()).thenReturn(builder);
        when(builder.path(VersionResource.class)).thenReturn(builder);
        when(builder.path(version1.getId().toString())).thenReturn(builder);
        when(builder.build()).thenReturn(uri);

        when(dateFieldFormat.format(version1.getStartDate())).thenReturn(datePretty);

        when(info.getBaseUriBuilder()).thenReturn(builder);
        when(builder.path(VersionResource.class)).thenReturn(builder);
        when(builder.path(version2.getId().toString())).thenReturn(builder);
        when(builder.build()).thenReturn(uri);

        when(versionService.isOverdue(version2)).thenReturn(false);
        when(dateFieldFormat.format(version2.getReleaseDate())).thenReturn(datePretty);

        final VersionBeanFactoryImpl factory = new VersionBeanFactoryImpl(versionService, remoteVersionLinkService, info, dateFieldFormat, authContext, simpleLinkManager);
        final List<VersionBean> beans = factory.createVersionBeans(Lists.newArrayList(version1, version2));

        //noinspection unchecked
        assertThat(beans, Matchers.<VersionBean>hasItems(
                reflectionEqualTo(new VersionBean.Builder().setVersion(version1).setUserStartDate(datePretty).setSelf(uri).build()),
                reflectionEqualTo(new VersionBean.Builder().setOverdue(false).setVersion(version2).setUserReleaseDate(datePretty).setSelf(uri).build())
        ));
        assertThat(beans.size(), equalTo(2));
    }
}

