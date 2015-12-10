<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.issuesecurity.delete.issue.security.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="security_schemes"/>
</head>

<body>

<ww:if test="active == true">
    <page:applyDecorator name="jiraform">
        <page:param name="action">ViewIssueSecuritySchemes.jspa</page:param>
        <page:param name="submitId">cancel_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.cancel'"/></page:param>
        <page:param name="autoSelectFirst">false</page:param>
        <page:param name="title"><ww:text name="'admin.schemes.issuesecurity.delete.issue.security.scheme'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.schemes.issuesecurity.error.scheme.associated.with.projects'"/></p>
                    <ul>
                    <ww:iterator value="projects(schemeObject)" status="'liststatus'">
                        <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="key"/>/summary"><ww:property value="name" /></a></li>
                    </ww:iterator>
                    </ul>
                    <p><ww:text name="'admin.schemes.issuesecurity.cannot.delete.scheme'"/></p>
                </aui:param>
            </aui:component>
        </page:param>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jiraform">
        <page:param name="action">DeleteIssueSecurityScheme.jspa</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="cancelURI">ViewIssueSecuritySchemes.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.schemes.issuesecurity.delete.issue.security.scheme'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="autoSelectFirst">false</page:param>
        <page:param name="description">
        <input type="hidden" name="schemeId" value="<ww:property value="schemeId" />">
        <input type="hidden" name="confirmed" value="true">
        <ww:if test="errorMessages/size == 0" >
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.schemes.issuesecurity.are.you.sure'"/></p>
                    <ww:text name="'admin.common.words.scheme'"/>: <strong><ww:property value="name" /></strong>
                    <ww:if test="description" >
                        <div class="description"><ww:text name="'common.words.description'"/>: "<ww:property value="description" />"</div>
                    </ww:if>
                </aui:param>
            </aui:component>
        </ww:if>
        </page:param>
    </page:applyDecorator>
</ww:else>

</body>
</html>
