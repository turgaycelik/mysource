/*
package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.export.ExportService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.system.SystemInfoUtils;
import com.atlassian.jira.web.util.OutlookDate;
import com.opensymphony.user.User;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.Response;

*/
/**
 *
 */
/*
public class TestAdminGadgetResource extends TestCase
{
    public void testWithBackupAndHsql()
    {
        doAssertions(true, true);
    }
    
    public void testWithoutBackupOrHsql()
    {
        doAssertions(false, false);
    }

    private void doAssertions(boolean isUsingHsql, boolean backupService)
    {
        final LicenseDetails details = createMock(LicenseDetails.class);

        JiraAuthenticationContext jac = createMock(JiraAuthenticationContext.class);
        PermissionManager pm = createMock(PermissionManager.class);
        ApplicationProperties props = createMock(ApplicationProperties.class);
        SystemInfoUtils utils = createMock(SystemInfoUtils.class);
        ServiceManager sm = createMock(ServiceManager.class);
        GlobalPermissionManager gpm =createMock(GlobalPermissionManager.class);
        UserUtil userUtil = createMock(UserUtil.class);
        JiraServiceContainer container= createMock(JiraServiceContainer.class);
        JiraLicenseService service= createMock(JiraLicenseService.class);
        expect(service.getLicense()).andReturn(details);
        replay(service);

        if(backupService){
        expect(container.getServiceClass()).andReturn(ExportService.class.getName());
        }
        else {
            expect(container.getServiceClass()).andReturn("NotExportService");
        }

        replay(container);

        expect(sm.getServices()).andReturn(Arrays.asList(container));

        I18nHelper helper = createMock(I18nHelper.class);
        expect(jac.getI18nHelper()).andStubReturn(helper);

        OutlookDate date = new OutlookDate(null, props);
        expect(jac.getOutlookDate()).andStubReturn(date);

        expect(userUtil.hasExceededUserLimit()).andReturn(false);
        expect(props.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT)).andReturn(false);
        User user = new MockUser("foo");
        expect(jac.getUser()).andStubReturn(user);
        expect(pm.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);
        expect(details.getMaximumNumberOfUsers()).andReturn(5);
        expect(userUtil.canActivateNumberOfUsers(1)).andReturn(true);
        expect(gpm.hasPermission(Permissions.SYSTEM_ADMIN, user)).andReturn(true);

        expect(details.isLicenseAlmostExpired()).andReturn(false);
        expect(details.getPartnerName()).andReturn("partner name");
        expect(details.getDescription()).andReturn("description");

        Locale locale = new Locale("foo");
        expect(jac.getLocale()).andReturn(locale);

        expect(details.getLicenseStatusMessage(helper, date, "<br/><br/>")).andReturn("foo message");
        expect(details.getLicenseExpiryStatusMessage(helper, date)).andReturn("foo expiry message");


        if (isUsingHsql)
        {
            expect(utils.getDatabaseType()).andReturn("hsql");
        }
        else
        {
            expect(utils.getDatabaseType()).andReturn("notHsql");
        }

        replay(details, jac, pm , props, utils, sm, gpm, userUtil);

        AdminGadgetResource resource = new AdminGadgetResource(jac, pm, props, utils, sm, gpm, userUtil, service) {

            protected List<String> getWarningMessages()
            {
                return Arrays.asList("fooMessage");
            }
        };
        Response response = resource.getData();
        assertEquals(200, response.getStatus());
        AdminGadgetResource.AdminProperties properties = (AdminGadgetResource.AdminProperties) response.getEntity();
        assertEquals(isUsingHsql, properties.isUsingHsql);
        assertEquals(backupService, properties.hasBackupService);
        assertFalse(properties.hasReachedUserLimit);
        assertFalse(properties.hasExceededUserLimit);
        assertFalse(properties.hasExceededUserLimit);
        assertTrue(properties.notExternalUserManagement);
    }
}
*/
