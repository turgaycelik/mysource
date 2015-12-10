
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.issuesecurity.delete.title'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="security_schemes"/>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">DeleteIssueSecurity.jspa</page:param>
    <page:param name="submitId">delete_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    <page:param name="cancelURI"><ww:url page="EditIssueSecurities!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
    <page:param name="title"><ww:text name="'admin.schemes.issuesecurity.delete.title'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="description">
    <input type="hidden" name="schemeId" value="<ww:property value="schemeId" />">
    <input type="hidden" name="id" value="<ww:property value="id" />">
    <input type="hidden" name="confirmed" value="true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <ww:text name="'admin.schemes.issuesecurity.delete.confirmation'">
                    <ww:param name="'value0'"><b><ww:property value="issueSecurityDisplayName" /> <ww:if test="issueSecurityParameter != null"> (<ww:property value="issueSecurityParameter"/>) </ww:if> </b></ww:param>
                    <ww:param name="'value1'"><b><ww:property value="issueSecurityName" /></b></ww:param>
                </ww:text>
            </aui:param>
        </aui:component>
    </page:param>
</page:applyDecorator>

</body>
</html>
