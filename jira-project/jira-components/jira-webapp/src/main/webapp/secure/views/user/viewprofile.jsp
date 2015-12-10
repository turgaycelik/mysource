<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:jira-fields");
    %>
    <title><ww:text name="'user.profile'"/>: <ww:property value="user/displayName"/></title>
    <script type="text/javascript">window.dhtmlHistory.create();</script>
</head>
<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-image">
                <div class="aui-avatar aui-avatar-large">
                    <div class="aui-avatar-inner">
                        <img alt="<ww:text name="'common.concepts.profile.avatar'"><ww:param name="'value0'"><ww:property value="user/displayName" /></ww:param></ww:text>"
                             src="<ww:property value="/avatarUrl(user)"/>" />
                    </div>
                </div>
            </div>
            <div class="aui-page-header-main">
                <h1 id="up-user-title"><ww:text name="'common.concepts.profile'"/>: <span id="up-user-title-name"><ww:property value="user/displayName"/></span><ww:if test="user/active == false"> (<ww:text name="'admin.common.words.inactive'"/>)</ww:if></h1>
            </div>
            <jsp:include page="profile/viewprofile-tools.jsp" />
        </div>
    </header>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ww:if test="/hasMoreThanOneProfileTabs == true">
                <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelNav'">
                    <ui:param name="'content'">
                        <jsp:include page="profile/viewprofile-tabs.jsp" />
                    </ui:param>
                </ui:soy>
            </ww:if>
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <jsp:include page="profile/viewprofile-content.jsp" />
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>