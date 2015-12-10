package com.atlassian.jira.rest.v2.admin.licenserole;

import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.license.LicenseRoleService;
import com.atlassian.jira.bc.license.MockLicenseRole;
import com.atlassian.jira.bc.license.MockLicenseRoleService;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.license.LicenseRole;
import com.atlassian.jira.license.LicenseRoleId;
import com.atlassian.jira.rest.matchers.ResponseMatchers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.v2.admin.licenserole.LicenseRoleResourceTest.LicenseRoleBeanMatcher.fromRole;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class LicenseRoleResourceTest
{
    @Rule
    public RuleChain chain = MockitoMocksInContainer.forTest(this);

    public I18nHelper helper = new NoopI18nHelper(Locale.ENGLISH);

    @Test
    public void getAllWorksOnSuccess()
    {
        final MockLicenseRoleService service = new MockLicenseRoleService();
        final MockLicenseRole role1 = service.addLicenseRole("id").addGroups("one", "two").setName("Name");
        final MockLicenseRole role2 = service.addLicenseRole("id3").addGroups().setName("Empty");

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.getAll();

        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.OK));

        @SuppressWarnings ("unchecked")
        final List<LicenseRoleBean> entity = (List<LicenseRoleBean>) response.getEntity();
        final Matcher<Iterable<LicenseRoleBean>> contains = Matchers.containsInAnyOrder(fromRole(role1), fromRole(role2));
        assertThat(entity, contains);
    }

    @Test
    public void getAllFailsOnError()
    {
        final String error1 = "Error";
        final ErrorCollection.Reason forbidden = ErrorCollection.Reason.FORBIDDEN;

        final ServiceOutcomeImpl<Set<LicenseRole>> error
                = ServiceOutcomeImpl.error(error1, forbidden);

        final LicenseRoleService service = Mockito.mock(LicenseRoleService.class);
        when(service.getRoles()).thenReturn(error);

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.getAll();

        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.FORBIDDEN));
        assertThat(response, ResponseMatchers.errorBody(error1));
    }

    @Test
    public void getRoleReturnsRoleWhenItExistsOnSuccess()
    {
        final MockLicenseRoleService service = new MockLicenseRoleService();
        final MockLicenseRole role1 = service.addLicenseRole("id").addGroups("one", "two").setName("Name");
        service.addLicenseRole("id3").addGroups().setName("Empty");

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.get(role1.getId().getName());

        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.OK));
        assertThat(response, ResponseMatchers.body(LicenseRoleBean.class, fromRole(role1)));
    }

    @Test
    public void getRoleReturns404WhenItDoesNotExist()
    {
        final MockLicenseRoleService service = new MockLicenseRoleService();
        service.addLicenseRole("id").addGroups("one", "two").setName("Name");
        service.addLicenseRole("id3").addGroups().setName("Empty");

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.get("whatIDont'Exist");

        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.NOT_FOUND));
        assertThat(response, ResponseMatchers.errorBody(MockLicenseRoleService.NOT_FOUND));
    }

    @Test
    public void getRoleReturns400WhenNoRolePassed()
    {
        final MockLicenseRoleService service = new MockLicenseRoleService();
        service.addLicenseRole("id").addGroups("one", "two").setName("Name");
        service.addLicenseRole("id3").addGroups().setName("Empty");

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.get(null);

        assertNoRole(response);
    }

    @Test
    public void putRoleReturns400WhenNoRolePassed()
    {
        final MockLicenseRoleService service = new MockLicenseRoleService();
        service.addLicenseRole("id").addGroups("one", "two").setName("Name");
        service.addLicenseRole("id3").addGroups().setName("Empty");

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.put(null, new LicenseRoleBean());

        assertNoRole(response);
    }

    @Test
    public void putRoleReturns404WhenItDoesNotExist()
    {
        final MockLicenseRoleService service = new MockLicenseRoleService();
        service.addLicenseRole("id").addGroups("one", "two").setName("Name");
        service.addLicenseRole("id3").addGroups().setName("Empty");

        final LicenseRoleBean licenseRoleBean = new LicenseRoleBean();
        licenseRoleBean.setGroups(Collections.<String>emptySet());

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.put("whatIDont'Exist", licenseRoleBean);

        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.NOT_FOUND));
        assertThat(response, ResponseMatchers.errorBody(MockLicenseRoleService.NOT_FOUND));
    }

    @Test
    public void putRoleUpdatesAndReturnsRoleWhenSuccessful()
    {
        final MockLicenseRoleService service = new MockLicenseRoleService();
        final Set<String> newGroups = Sets.newHashSet("three", "four");
        final MockLicenseRole role1 = service
                .addLicenseRole("id")
                .addGroups("one", "two")
                .setName("Name");

        service.addLicenseRole("id3").addGroups().setName("Empty");

        final LicenseRoleBean licenseRoleBean = new LicenseRoleBean();
        licenseRoleBean.setGroups(newGroups);

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.put(role1.getId().getName(), licenseRoleBean);

        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.OK));
        assertThat(response, ResponseMatchers.body(LicenseRoleBean.class, fromRole(role1.copy().setGroups(newGroups))));
    }

    @Test
    public void putRoleReturnsRoleWhenNoUpdateRequested()
    {
        final MockLicenseRoleService service = new MockLicenseRoleService();
        final MockLicenseRole role1 = service
                .addLicenseRole("id")
                .addGroups("one", "two")
                .setName("Name")
                .copy();

        service.addLicenseRole("id3").addGroups().setName("Empty");

        final LicenseRoleBean licenseRoleBean = new LicenseRoleBean();

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.put(role1.getId().getName(), licenseRoleBean);

        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.OK));
        assertThat(response, ResponseMatchers.body(LicenseRoleBean.class, fromRole(role1)));
    }

    @Test
    public void putRoleReturns404WhenNoUpdateRequestedToBadRole()
    {
        final MockLicenseRoleService service = new MockLicenseRoleService();
        service.addLicenseRole("id").addGroups("one", "two").setName("Name");
        service.addLicenseRole("id3").addGroups().setName("Empty");

        final LicenseRoleBean licenseRoleBean = new LicenseRoleBean();

        final LicenseRoleResource licenseRoleResource = new LicenseRoleResource(service, helper);
        final Response response = licenseRoleResource.put("what", licenseRoleBean);

        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.NOT_FOUND));
        assertThat(response, ResponseMatchers.errorBody(MockLicenseRoleService.NOT_FOUND));
    }

    @Test
    public void webSudoEnabled()
    {
        final WebSudoRequired sudoRequired = LicenseRoleResource.class.getAnnotation(WebSudoRequired.class);
        assertThat(sudoRequired, Matchers.notNullValue());
    }

    @Test
    public void xsrfProtectionThroughMediaTypes()
    {
        final Consumes consumes = LicenseRoleResource.class.getAnnotation(Consumes.class);
        assertThat(consumes, Matchers.notNullValue());
        assertThat(Arrays.asList(consumes.value()), Matchers.contains(MediaType.APPLICATION_JSON));

        final Produces produces = LicenseRoleResource.class.getAnnotation(Produces.class);
        assertThat(produces, Matchers.notNullValue());
        assertThat(Arrays.asList(produces.value()), Matchers.contains(MediaType.APPLICATION_JSON));
    }

    private void assertNoRole(final Response response)
    {
        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.BAD_REQUEST));
        assertThat(response, ResponseMatchers.errorBody(NoopI18nHelper.makeTranslation("rest.error.no.license.role", "null")));
    }

    public static class LicenseRoleBeanMatcher extends TypeSafeDiagnosingMatcher<LicenseRoleBean>
    {
        private final String name;
        private final Set<String> groups;
        private final LicenseRoleId id;

        public static LicenseRoleBeanMatcher fromRole(LicenseRole role)
        {
            return new LicenseRoleBeanMatcher(role);
        }

        public LicenseRoleBeanMatcher(LicenseRole role)
        {
            this.name = role.getName();
            this.groups = role.getGroups();
            this.id = role.getId();
        }

        @Override
        protected boolean matchesSafely(final LicenseRoleBean item, final Description mismatchDescription)
        {
            if (Objects.equal(name, item.getName())
                    && Objects.equal(groups, item.getGroups())
                    && Objects.equal(id, new LicenseRoleId(item.getId())))
            {
                return true;
            }
            else
            {
                mismatchDescription.appendValue(String.format("[name: %s, groups: %s, id: %s]",
                        item.getName(), item.getGroups(), item.getId()));

                return false;
            }
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText(String.format("[name: %s, groups: %s, id: %s]",
                    name, groups, id.getName()));
        }
    }
}