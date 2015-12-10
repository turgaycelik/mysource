<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.notifications.delete.notification.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="notification_schemes"/>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">DeleteNotificationScheme.jspa</page:param>
    <page:param name="submitId">delete_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    <page:param name="cancelURI">ViewNotificationSchemes.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.schemes.notifications.delete.notification.scheme'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="description">
        <input type="hidden" name="schemeId" value="<ww:property value="schemeId" />">
        <input type="hidden" name="confirmed" value="true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                <ww:text name="'admin.schemes.delete.confirmation'">
                    <ww:param name="'value0'"><b><ww:property value="name" /></b></ww:param>
                </ww:text>
                </p>
                <ww:if test="description" >
                    <div class="description"><ww:property value="description" /></div>
                </ww:if>
            </aui:param>
        </aui:component>

        <ww:if test="active == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                    <ww:text name="'admin.schemes.notifications.delete.warning'">
                        <ww:param name="'value0'"><b></ww:param>
                        <ww:param name="'value1'"><ww:property value="name" /></b></ww:param>
                    </ww:text>
                <ww:iterator value="projects(schemeObject)" status="'liststatus'">
                    <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="key"/>/summary">
                    <ww:property value="name" /></a><ww:if test="@liststatus/last == false">, </ww:if><ww:else>.</ww:else>
                </ww:iterator><br>
                <ww:text name="'admin.schemes.notifications.delete.may.wish.to.delete.another'"/></p>
                </aui:param>
            </aui:component>
        </ww:if>

    </page:param>
</page:applyDecorator>

</body>
</html>
