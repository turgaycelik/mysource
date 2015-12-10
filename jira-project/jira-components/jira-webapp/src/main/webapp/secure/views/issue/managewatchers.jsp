<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'watcher.manage.title'"/></title>
    <meta name="decorator" content="issueaction" />
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false"/>" />
    <%@ include file="/includes/js/multipickerutils.jsp" %>
</head>
<body>
    <div class="command-bar">
        <div class="ops-cont">
            <ul class="ops">
                <li id="back-lnk-section" class="last">
                    <a id="back-lnk" class="button first last" href="<%= request.getContextPath() %>/browse/<ww:property value="/issueObject/key" />"><span class="icon icon-back"><span><ww:text name="'opsbar.back.to.issue'"/></span></span><ww:text name="'opsbar.back.to.issue'"/></a>
                </li>
            </ul>
            <ww:if test="/watchingEnabled == true">
            <ul class="ops">
                <li>
                    <ww:if test="canStartWatching == true">
                        <a class="button first last" id="watch" href="<ww:url value="'ManageWatchers!startWatching.jspa'"><ww:param name="'key'" value="/issue/string('key')"/></ww:url>"><ww:text name="'watcher.manage.start.watch'" /></a>
                    </ww:if>
                    <ww:elseIf test="canStopWatching == true">
                        <a class="button first last" id="unwatch" href="<ww:url value="'ManageWatchers!stopWatching.jspa'"><ww:param name="'key'" value="/issue/string('key')"/></ww:url>"><ww:text name="'watcher.manage.stop.watch'" /></a>
                    </ww:elseIf>
                </li>
            </ul>
            </ww:if>
        </div>
    </div>
    <h2><ww:text name="'watcher.manage.title'"/></h2>
    <ww:if test="hasErrorMessages == 'true'">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <ul>
                    <ww:iterator value="flushedErrorMessages">
                        <li><ww:property /></li>
                    </ww:iterator>
                </ul>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:if test="/watchingEnabled == false">
        <p><span class="warning"><ww:text name="'watcher.disabled'"/>.</span></p>
    </ww:if>
    <ww:elseIf test="/issueValid == true">
        <%-- Renders the list of watchers and the picker --%>
        <ww:property value="/userPickerHtml" escape="false"/>
    </ww:elseIf>
</body>
</html>
