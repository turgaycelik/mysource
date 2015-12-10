<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %> 
<html>
<head>
    <ww:if test="/categories/size > 0">
        <title><ww:text name="'browseprojects.title'" /></title>
        <content tag="section">browse_link</content>
        <%
            // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "browse.projects"
            WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
            webResourceManager.requireResourcesForContext("jira.browse");
            webResourceManager.requireResourcesForContext("jira.browse.projects");
        %>
        <script type="text/javascript">window.dhtmlHistory.create();</script>
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/categories/size > 0">
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeader'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderMain'">
                <ui:param name="'content'">
                    <h1><ww:text name="'browseprojects.title'"/></h1>
                </ui:param>
            </ui:soy>
            <ww:property value="/operationLinks" >
                <ww:if test="./empty == false">
                    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderActions'">
                        <ui:param name="'content'">
                            <ww:iterator value=".">
                                <a href='<ww:property value="./url" />'
                                   class='aui-button <ww:property value="./styleClass" />'
                                   id='<ww:property value="./id" />'
                                   title='<ww:property value="./title" />'>
                                    <ww:property value="./label" />
                                </a>
                            </ww:iterator>
                        </ui:param>
                    </ui:soy>
                </ww:if>
            </ww:property>
        </ui:param>
    </ui:soy>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ww:if test="/showTabs() == true">
                <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelNav'">
                    <ui:param name="'content'">
                        <ul class="vertical tabs">
                            <ww:iterator value="tabs" status="'status'">
                                <li id="<ww:property value="./id" />-panel-tab" class="<ww:if test="@status/first == true">first </ww:if><ww:if test="/selectedCategory == ./id">active</ww:if>">
                                   <a id="<ww:property value="./id" />-panel-tab-lnk" rel="<ww:property value="./id" />" title="<ww:property value="./description" />" href="<%= request.getContextPath() %>/secure/BrowseProjects.jspa?selectedCategory=<ww:property value="./id" />"><strong><ww:property value="./name" /></strong></a>
                                </li>
                            </ww:iterator>
                            <li id="all-panel-tab" class="<ww:if test="/selectedCategory == 'all'">active</ww:if>">
                               <a id="all-panel-tab-lnk" rel="all" title="<ww:text name="'browse.projects.all.desc'"/>" href="<%= request.getContextPath() %>/secure/BrowseProjects.jspa?selectedCategory=all"><strong><ww:text name="'browse.projects.all'"/></strong></a>
                            </li>
                        </ul>
                    </ui:param>
                </ui:soy>
            </ww:if>
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <ww:property value="/infoPanelHtml" escape="false"/>
                    <ww:iterator value="categories" status="'status'">
                        <div>
                            <div class="module<ww:if test="./all == true"> inall</ww:if> <ww:if test="/selectedCategory == ./id || (/selectedCategory == 'all' && ./all == true)">active</ww:if><ww:else>hidden</ww:else>" id="<ww:property value="./id" />-panel">
                                <ww:if test="./name != null && ./name/empty == false">
                                    <div class="mod-header">
                                        <h2><ww:property value="./name" /></h2>
                                    </div>
                                </ww:if>
                                <div class="mod-content">
                                <ww:property value="./projects">
                                    <%@ include file="/includes/project/projectstable.jsp" %>
                                </ww:property>
                                </div>
                            </div>
                        </div>
                    </ww:iterator>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <%@ include file="/includes/noprojects.jsp" %>
            </aui:param>
        </aui:component>
    </div>
</ww:else>
</body>
</html>
