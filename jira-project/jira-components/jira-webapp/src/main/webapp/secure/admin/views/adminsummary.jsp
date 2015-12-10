<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <title><ww:text name="'menu.admin.header.title'"/></title>
    <meta name="admin.active.section" content="system.admin.top.navigation.bar"/>
    <meta name="admin.active.tab" content="admin_summary"/>
</head>
<body>
<div id="admin-summary-panel">
    <div class="admin-search">
        <a id="admin-search-link" href="#"><span class="aui-icon aui-icon-small aui-iconfont-search"> </span><span class="seach-admin-text">Search JIRA admin</span></a>
    </div>
    <ww:iterator value="/topPanels">
        <div id="admin-summary-webpanel-<ww:property value='panelKey'/>" class="module toggle-wrap admin-summary-webpanel">
            <ww:property value='contentHtml' escape="false"/>
        </div>
    </ww:iterator>
    <div class="aui-group">
        <div class="aui-item">
            <ww:iterator value="/leftPanels">
                <div id="admin-summary-webpanel-<ww:property value='panelKey'/>" class="module toggle-wrap admin-summary-webpanel">
                    <ww:property value='contentHtml' escape="false"/>
                </div>
            </ww:iterator>
        </div>
        <div class="aui-item">
            <ww:iterator value="/rightPanels">
                <div id="admin-summary-webpanel-<ww:property value='panelKey'/>" class="module toggle-wrap admin-summary-webpanel">
                    <ww:property value='contentHtml' escape="false"/>
                </div>
            </ww:iterator>
        </div>
    </div>
</div>
</body>
</html>
