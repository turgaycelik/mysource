package com.atlassian.jira.license;

import java.util.Set;

import com.atlassian.extras.decoder.api.LicenseDecoder;
import com.atlassian.extras.decoder.v2.Version2LicenseDecoder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Tests {@link com.atlassian.jira.license.DefaultLicenseRoleDetails}.
 *
 * @since v6.3
 */
public class TestDefaultLicenseRoleDetails
{
    public static final LicenseDecoder LICENSE_DECODER = new Version2LicenseDecoder();

    /** A license role named "Developer".
     * This must exactly match the role declared in json and the license string. */
    public static final LicenseRoleId SOFTWARE_DEVELOPER = new LicenseRoleId( "Developer" );

    /** A license role named "Agent".
     * This must exactly match the role declared in json and the license string. */
    public static final LicenseRoleId SERVICEDESK_AGENT = new LicenseRoleId( "Agent" );

    /** A license role named "User".
     * This must exactly match the role declared in json and the license string. */
    public static final LicenseRoleId BUSINESS_USER = new LicenseRoleId( "User" );

    /**
     * Sample (version 2) license with 3 embedded roles.
     *
     * Corresponds to the following properties (as JSON):
     * <pre>
     *    {
     *        "licenseVersion": "2",
     *
     *        "Organisation": "Dolphin Handlers Inc",
     *        "ContactEMail": "flipper@dhi.com",
     *        "ContactName": "flipper",
     *        "ServerID": "BZQK-F9EL-I0CG-KANJ",
     *
     *        "CreationDate": "2020-01-01",
     *        "PurchaseDate": "2020-01-01",
     *        "LicenseExpiryDate": "2020-01-01",
     *        "MaintenanceExpiryDate": "2020-01-01",
     *
     *        "SEN": "2016178",
     *        "NumberOfUsers": "101",
     *        "Evaluation": "false",
     *
     *        "jira.LicenseEdition": "ENTERPRISE",
     *        "jira.LicenseTypeName": "COMMERCIAL",
     *        "jira.active": "true",
     *
     *        "JiraProduct.Software.active": "true",
     *        "JiraProduct.Software.Role.Developer.NumberOfUsers": "33",
     *
     *        "JiraProduct.ServiceDesk.active": "true",
     *        "JiraProduct.ServiceDesk.Role.Agent.NumberOfUsers": "22",
     *
     *        "JiraProduct.Business.active": "true",
     *        "JiraProduct.Business.Role.User.NumberOfUsers": "11"
     *    }
     * </pre>
     */
    public static final String LICENSE_WITH_3_ROLES
        = "AAABXA0ODAoPeNqNkltPgzAYhu/7K0i8hkBZPCUkMqjKDmzC1MS7Ch9btSukLej+vd3mLphbYtKbp\n"
        + "l+f99BevEJphY20sG95+NYf3GLXIvnCwq43QGm7fgc5q54VSBV4roeiWmha6JSuIag4axqQaN7KY\n"
        + "kUVxFRDgF3s2q5nFpqwAoQC8t0wuTk+/AWRKWX8QLorV8wp6jXKQXYgkzgYvj2N7fsbMrETN3qwx\n"
        + "2E6QiMm6VzWZVtoJ68r/UUlOAbFOgi0bAF9mAHnIF4yzWoRkHRBsnmW5AQZRaFBUFGcsdYTME4MK\n"
        + "Qb12dPISWqueJfe1fVpQ1nNwYmhA16bZE6/Sd9HM7mkgim6cxfXvFkxYT1SUXIzYCWi6GGHrWICl\n"
        + "Npjt5AjomcalbCj/TfODhUuQegjFsaI79t7MdutP3zazJ/Sz73CYtPA7stEs+mUZFESThDpKG/38\n"
        + "SvKFaAfSXHeqzAsAhRTJSxcPElj1cdbJvdgZJHV8Lah+gIUahcqwTRV/jehCQ+KCMoM8z1pPSM=X\n"
        + "02h5";

    /**
     * Sample (version 2) license with 1 embedded role.
     *
     * Corresponds to the following properties (as JSON):
     * <pre>
     *    {
     *        "licenseVersion": "2",
     *
     *        "Organisation": "Dolphin Handlers Inc",
     *        "ContactEMail": "flipper@dhi.com",
     *        "ContactName": "flipper",
     *        "ServerID": "BZQK-F9EL-I0CG-KANJ",
     *
     *        "CreationDate": "2020-01-01",
     *        "PurchaseDate": "2020-01-01",
     *        "LicenseExpiryDate": "2020-01-01",
     *        "MaintenanceExpiryDate": "2020-01-01",
     *
     *        "SEN": "2016178",
     *        "NumberOfUsers": "101",
     *        "Evaluation": "false",
     *
     *        "jira.LicenseEdition": "ENTERPRISE",
     *        "jira.LicenseTypeName": "COMMERCIAL",
     *        "jira.active": "true",
     *
     *        "JiraProduct.Software.active": "true",
     *        "JiraProduct.Software.Role.Developer.NumberOfUsers": "69"
     *    }
     * </pre>
     */
    public static final String LICENSE_WITH_1_ROLE
        = "AAABNg0ODAoPeNptkF9PwjAUxd/7KZr4vGUdZgpJE3GrOv4M3FAT3+p2JzWlXbpuyrenQEiE8HrvP\n"
        + "ed3zr35gAqPG4PDASZkNIhGJMKsWOEwILco6zZfYBb1WwumpSQgKDbArdAq4RZoGISBFxBvP9fK8\n"
        + "tJmfAO0lqJpwKAfYbg/EyWoFlgl9jLKshXLl3laMMR6LruDGa25bOHkweZcyJPJQ7UWfqk3Z2arb\n"
        + "QMHUryYz1kep+PZce/UogdqTQdIHm/fXfI9IkQTd7E0uupK6xe6tr/cgJ9rCX4CPUjtaP554Wh4X\n"
        + "fQf48IqC4qrEthfI8z28jML882VaI9FEy2btVD4hatKOgROVYkKljkBicjdPSrA9GDShD5+vk69p\n"
        + "yGbeWkQP3vTcTZBp19e5Sw7U655C5fzHVGDoEIwLAIUW235WjQRjEVEJ6N1z25K3X23ZOQCFA/p9\n"
        + "3WTRxZAwKr4Zqz6yeClULP1X02ff";

    /**
     * Sample (version 2) license with no embedded roles.
     *
     * Corresponds to the following properties (as JSON):
     * <pre>
     *    {
     *        "licenseVersion": "2",
     *
     *        "Organisation": "Dolphin Handlers Inc",
     *        "ContactEMail": "flipper@dhi.com",
     *        "ContactName": "flipper",
     *        "ServerID": "BZQK-F9EL-I0CG-KANJ",
     *
     *        "CreationDate": "2020-01-01",
     *        "PurchaseDate": "2020-01-01",
     *        "LicenseExpiryDate": "2020-01-01",
     *        "MaintenanceExpiryDate": "2020-01-01",
     *
     *        "SEN": "2016178",
     *        "NumberOfUsers": "101",
     *        "Evaluation": "false",
     *
     *        "jira.LicenseEdition": "ENTERPRISE",
     *        "jira.LicenseTypeName": "COMMERCIAL",
     *        "jira.active": "true"
     *    }
     * </pre>
     */
    public static final String LICENSE_WITH_0_ROLES
        = "AAABEw0ODAoPeNptj11PgzAUhu/7K5p4zdKyRd2SJiJUxTGYgJp4V+EgNaWQ8hH374URLly8Pee8z\n"
        + "3Peq3fIsdMYbK8xpbv1drchmCcptgndoLCvPsFExWsLpmWUUOQaEJ2stSc6YDaxiUWoNc1r3YmsC\n"
        + "0UFrFCyacCgb2nEKpAZ6BZ4LqcY42HK42PsJ3yJ8IOQasnc5aVcZXWF+CBUfzaxQqgW/sDSUwNnk\n"
        + "xsdDjx2fSeY9yNODsA60wNS8+3b+PlEsdHo0R1ooTPgP400p8sOkfkSWraz1atVU0qNn4TO1cjAv\n"
        + "s5QwsMxQK/pzS1KwAxgfI/df7zsrYctDyyfuI/W3gmf0dL6X8+xN1kpWric/wJBm4ApMCwCFGri/\n"
        + "90rlr9ONmQXDvjLnV3nXFygAhRbfAiUjMS9glP0L6z5R0Iwt6X1mA==X02e2";

    /**
     * Sample (version 2) license with 3 embedded roles, 1 inactive.
     *
     * Corresponds to the following properties (as JSON):
     * <pre>
     *    {
     *        "licenseVersion": "2",
     *
     *        "Organisation": "Dolphin Handlers Inc",
     *        "ContactEMail": "flipper@dhi.com",
     *        "ContactName": "flipper",
     *        "ServerID": "BZQK-F9EL-I0CG-KANJ",
     *
     *        "CreationDate": "2020-01-01",
     *        "PurchaseDate": "2020-01-01",
     *        "LicenseExpiryDate": "2020-01-01",
     *        "MaintenanceExpiryDate": "2020-01-01",
     *
     *        "SEN": "2016178",
     *        "NumberOfUsers": "101",
     *        "Evaluation": "false",
     *
     *        "jira.LicenseEdition": "ENTERPRISE",
     *        "jira.LicenseTypeName": "COMMERCIAL",
     *        "jira.active": "true",
     *
     *        "JiraProduct.Software.active": "true",
     *        "JiraProduct.Software.Role.Developer.NumberOfUsers": "33",
     *
     *        "JiraProduct.ServiceDesk.active": "false",
     *        "JiraProduct.ServiceDesk.Role.Agent.NumberOfUsers": "22",
     *
     *        "JiraProduct.Business.active": "true",
     *        "JiraProduct.Business.Role.User.NumberOfUsers": "11"
     *    }
     * </pre>
     */
    public static final String LICENSE_WITH_INACTIVE_ROLES
        = "AAABXw0ODAoPeNqNkltPgzAYhu/7K0i8hnBYnC4hkUFVdmATpibeVfjYql0hbUH37+3AXWxuiUlvm\n"
        + "n593kN79QqFEdTCcD3DcUeeNxoMDZytDNd2Bihptu8gFuWzBCF9x3ZQWHFFcpWQLfglo3UNAi0bk\n"
        + "W+IhIgo8F3btU3b0QvNaA5cAv6uqdidHv6C8JxQdiDdFRtq5dUWZSBaEHHkj9+epub9LZ6ZsR0+m\n"
        + "NMgmaAJFWQpqqLJlZVVpfoiAiyNoi34SjSAPvSAdRAvqKIV93GywukyjTOMtCJXwAnPL1g7EtBON\n"
        + "CkC+XnQKAmTgDKc6DvOtTO8Oe8orRhYEbTAKh3NOq7S89BCrAmnknT2oorVG8qNR8ILpgeMmOdH2\n"
        + "HEjKQcpe+weckJ0dKUCOtp/83SoYA1cnbBcF7G+vhe93ftzz5v50/qlZ1jtauj+TLiYz3EaxsEM4\n"
        + "Zawpo/fV/oDJyne/jAtAhQ1nBeiCDzRxhavmYk8lXUNd5OWsAIVAJUUw2WLFceUto2Q4kBvdo9DX\n"
        + "JDoX02h9";

    /**
     * Sample (version 2) license with 3 embedded roles, 1 unlimited, 1 with 0 seats, 1 with 1 seat.
     *
     * Corresponds to the following properties (as JSON):
     * <pre>
     *    {
     *        "licenseVersion": "2",
     *
     *        "Organisation": "Dolphin Handlers Inc",
     *        "ContactEMail": "flipper@dhi.com",
     *        "ContactName": "flipper",
     *        "ServerID": "BZQK-F9EL-I0CG-KANJ",
     *
     *        "CreationDate": "2020-01-01",
     *        "PurchaseDate": "2020-01-01",
     *        "LicenseExpiryDate": "2020-01-01",
     *        "MaintenanceExpiryDate": "2020-01-01",
     *
     *        "SEN": "2016178",
     *        "NumberOfUsers": "101",
     *        "Evaluation": "false",
     *
     *        "jira.LicenseEdition": "ENTERPRISE",
     *        "jira.LicenseTypeName": "COMMERCIAL",
     *        "jira.active": "true",
     *
     *        "JiraProduct.Software.active": "true",
     *        "JiraProduct.Software.Role.Developer.NumberOfUsers": "-1",
     *
     *        "JiraProduct.ServiceDesk.active": "false",
     *        "JiraProduct.ServiceDesk.Role.Agent.NumberOfUsers": "0",
     *
     *        "JiraProduct.Business.active": "true",
     *        "JiraProduct.Business.Role.User.NumberOfUsers": "1"
     *    }
     * </pre>
     */
    public static final String LICENSE_WITH_UNLIMITED_ROLES
        = "AAABWw0ODAoPeNqNkk1PgzAYx+/9FCSeIZT5uoREBlWZG5swNfFW4WGrdoW0Bd23t2PuwNwSk16aP\n"
        + "v39X9qzVyisoJaWN7CwNxwMhvjCItnC8lx8jpJm/Q5yVj4rkMrHLkZhJTTNdULX4Jec1TVING9kv\n"
        + "qIKIqrB91zPtV1sFpqwHIQC8l0zuTk8/AWRKWV8T7otVszJqzXKQLYg48gfvT092nc3ZGLHbnhvP\n"
        + "wbJGI2ZpHNZFU2unawq9ReV4BgUa8HXsgH0YQacvXjBNKuET5IFSedpnBFkFIUGQUV+wlpPwDgxp\n"
        + "AjUZ08jI4m5gi/x1fVxQ2nFwYmgBV6ZZE6/SRujmVxSwRTt3EUVr1dMWA9UFNwMWLHIe9hRo5gAp\n"
        + "XbYLeSAaAqV0MH+m6YjBUsQ+gDlIr7r7sXstu6841b+VH7qDRabGroPE86mU5KGcTBBpKW82YUvK\n"
        + "VeAfgC85N5BMCwCFA/l3miKNsF2+GRsOVfnMMDJAcvAAhQdyy0kxZvJd0w0rrPqP0S2tMJQHg==X\n"
        + "02h5";


    @Test
    public void testSingleLicenseRoleIsDetected()
    {
        LicenseRoleDetails lic = new DefaultLicenseRoleDetails( LICENSE_WITH_1_ROLE, LICENSE_DECODER );
        Set<LicenseRoleId> role = lic.getLicenseRoles();

        assertEquals( "1 license role is detected", 1, role.size() );

        assertEquals( "LicenseRoleIds test equality by value",
            SOFTWARE_DEVELOPER, new LicenseRoleId( "Developer" ) );

        assertEquals(
            "Role '" + SOFTWARE_DEVELOPER.getName() + "' provides 33 seats",
            69, lic.getUserLimit( SOFTWARE_DEVELOPER ) );

        assertEquals(
            "Role '" + SERVICEDESK_AGENT.getName() + "' provides 0 seats",
            0, lic.getUserLimit( SERVICEDESK_AGENT ) );

        assertEquals(
            "Role '" + BUSINESS_USER.getName() + "' provides 0 seats",
            0, lic.getUserLimit( BUSINESS_USER ) );
    }


    @Test
    public void testMultipleLicenseRolesAreDetected()
    {
        LicenseRoleDetails lic = new DefaultLicenseRoleDetails( LICENSE_WITH_3_ROLES, LICENSE_DECODER );
        Set<LicenseRoleId> role = lic.getLicenseRoles();

        assertEquals( "3 license roles are detected", 3, role.size() );

        assertEquals( "LicenseRoleIds test equality by value",
            SOFTWARE_DEVELOPER, new LicenseRoleId( "Developer" ) );

        assertEquals(
            "Role '" + SOFTWARE_DEVELOPER.getName() + "' provides 33 seats",
            33, lic.getUserLimit( SOFTWARE_DEVELOPER ) );

        assertEquals(
            "Role '" + SERVICEDESK_AGENT.getName() + "' provides 22 seats",
            22, lic.getUserLimit( SERVICEDESK_AGENT ) );

        assertEquals(
            "Role '" + BUSINESS_USER.getName() + "' provides 11 seats",
            11, lic.getUserLimit( BUSINESS_USER ) );
    }


    @Test
    public void testNoLicenseRolesAreDetected()
    {
        LicenseRoleDetails lic = new DefaultLicenseRoleDetails( LICENSE_WITH_0_ROLES, LICENSE_DECODER );
        Set<LicenseRoleId> role = lic.getLicenseRoles();

        assertEquals( "0 license roles are detected", 0, role.size() );

        assertEquals( "LicenseRoleIds test equality by value",
            SOFTWARE_DEVELOPER, new LicenseRoleId( "Developer" ) );

        assertEquals(
            "Role '" + SOFTWARE_DEVELOPER.getName() + "' provides 0 seats",
            0, lic.getUserLimit( SOFTWARE_DEVELOPER ) );

        assertEquals(
            "Role '" + SERVICEDESK_AGENT.getName() + "' provides 0 seats",
            0, lic.getUserLimit( SERVICEDESK_AGENT ) );

        assertEquals(
            "Role '" + BUSINESS_USER.getName() + "' provides 0 seats",
            0, lic.getUserLimit( BUSINESS_USER ) );
    }


    @Test
    public void testUnlimitedRoleLimits()
    {
        LicenseRoleDetails lic = new DefaultLicenseRoleDetails( LICENSE_WITH_UNLIMITED_ROLES, LICENSE_DECODER );
        Set<LicenseRoleId> role = lic.getLicenseRoles();

        assertEquals( "3 license roles are detected", 3, role.size() );

        assertEquals( "LicenseRoleIds test equality by value",
            SOFTWARE_DEVELOPER, new LicenseRoleId( "Developer" ) );

        assertEquals(
            "Role '" + SOFTWARE_DEVELOPER.getName() + "' provides unlimited seats",
            -1, lic.getUserLimit( SOFTWARE_DEVELOPER ) );

        assertEquals(
            "Role '" + SERVICEDESK_AGENT.getName() + "' provides 0 seats",
            0, lic.getUserLimit( SERVICEDESK_AGENT ) );

        assertEquals(
            "Role '" + BUSINESS_USER.getName() + "' provides 0 seats",
            1, lic.getUserLimit( BUSINESS_USER ) );
    }
}
