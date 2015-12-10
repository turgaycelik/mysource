<%@ page import="com.atlassian.jira.web.util.ExternalLinkUtilImpl"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<% com.atlassian.jira.web.util.ExternalLinkUtil externalLinkUtil = ExternalLinkUtilImpl.getInstance(); %>
<html>
<head>
	<title><ww:text name="'admin.license.information'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/licensing_section"/>
    <meta name="admin.active.tab" content="license_details"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.license.information'"/></page:param>
    <page:param name="width">100%</page:param>
    <p><ww:text name="'admin.license.this.page.shows'"/> <ww:text name="'admin.license.you.instructions'"/></p>
</page:applyDecorator>

<form action="RefreshActiveUserCount.jspa" method="post">
    <ww:iterator value="/allLicenseDetails">
    <table id="license_table" class="aui aui-table-rowhover">
        <tbody>
        <tr>
            <td width="20%">
                <span class="item-label"><ww:text name="'admin.license.organisation'"/></span>
            </td>
            <td>
                <b><ww:property value="./organisation"/></b>
            </td>
        </tr>
        <tr>
            <td>
                <span class="item-label"><ww:text name="'admin.license.date.purchased'"/></span>
            </td>
            <td>
                <b><ww:property value="/purchaseDate(.)"/></b>
            </td>
        </tr>
        <tr>
            <td>
                <span class="item-label"><ww:text name="'admin.license.type'"/></span>
            </td>
            <td>
                <b><ww:property value="./description"/></b><br/>
                <small><ww:property value="/licenseExpiryStatusMessage(.)" escape="false"/></small>
            </td>
        </tr>
        <tr>
            <td>
                <span class="item-label"><ww:text name="'admin.server.id'"/></span>
            </td>
            <td>
                <b><span id="serverId"><ww:property value="/serverId"/></span></b>
            </td>
        </tr>
        <tr>
            <td>
                <span class="item-label"><ww:text name="'admin.license.sen'"/></span>
            </td>
            <td>
                <b><ww:property value="./supportEntitlementNumber"/></b>
            </td>
        </tr>
        <ww:if test="/licenseRequiresUserLimit(.) == true">
            <tr>
                <td>
                    <span class="item-label"><ww:text name="'admin.license.user.limit'"/></span>
                </td>
                <td <ww:if test="/hasExceededUserLimit == true">style="color: #ff0000;"</ww:if>>
                    <b><ww:if test="./unlimitedNumberOfUsers == true"><ww:text name="'common.words.unlimited'"/></ww:if><ww:else><ww:property value="./maximumNumberOfUsers"/></ww:else></b>
                    (<ww:text name="'admin.license.active.user.count'">
                        <ww:param name="'value0'"><ww:property value="/activeUserCount"/></ww:param>
                    </ww:text>) &nbsp;
                    <input class="aui-button" type="submit" name="<ww:text name="'admin.common.words.refresh'"/>" value="<ww:text name="'admin.common.words.refresh'"/>"/>
                </td>
            </tr>
        </ww:if>
        <ww:else>
            <tr>
                <td>
                    <span class="item-label"><ww:text name="'admin.license.user.limit'"/></span>
                </td>
                <td>
                    <b><ww:if test="./unlimitedNumberOfUsers == true"><ww:text name="'common.words.unlimited'"/></ww:if><ww:else><ww:property value="./maximumNumberOfUsers"/></ww:else></b>
                </td>
            </tr>
        </ww:else>
        <ww:property value="./partnerName">
            <ww:if test=". != null && . != ''">
                <tr>
                    <td>
                        <span class="item-label"><ww:text name="'admin.license.partner.name'"/></span>
                    </td>
                    <td>
                        <b><ww:property value="."/></b>
                    </td>
                </tr>
            </ww:if>
        </ww:property>

        <ww:if test="/licenseStatusMessage(.) != null">
            <tr>
                <td>
                    <span class="item-label"><ww:text name="'admin.license.expiry.information'"/></span>
                </td>
                <td>
                    <ww:property value="/licenseStatusMessage(.)" escape="false"/>
                </td>
            </tr>
        </ww:if>
        </tbody>
    </table>
    </ww:iterator>
</form>

<aui:component template="module.jsp" theme="'aui'">
    <aui:param name="'contentHtml'">
        <page:applyDecorator name="jiraform">
            <page:param name="action">ViewLicense.jspa</page:param>
            <page:param name="submitId">add_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
            <page:param name="width">100%</page:param>
            <page:param name="title"><ww:if test="/licenseSet == true"><ww:text name="'admin.license.update.license'"/></ww:if><ww:else><ww:text name="'admin.license.add.license'"/></ww:else></page:param>
            <page:param name="description"><ww:text name="'admin.license.copy.and.paste'">
                <ww:param name="'value0'"><a href="<%= externalLinkUtil.getProperty("external.link.atlassian.my.account") %>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text></page:param>
            <ui:textarea label="text('admin.license')" name="'license'" cols="50" rows="10" />
        </page:applyDecorator>
    </aui:param>
</aui:component>
</body>
</html>
