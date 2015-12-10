<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_users_menu/shared_section"/>
    <meta name="admin.active.tab" content="shared_dashboards"/>
    <%@ include file="/includes/decorators/xsrftoken.jsp" %>
	<title><ww:text name="'admin.issues.dashboards.shared.title'"/></title>
    <jira:web-resource-require modules="jira.webresources:shared-dashboards-admin" />
</head>
<body>
    <page:applyDecorator id="search-dashboards-form" name="auiform">
        <page:param name="action">ViewSharedDashboards.jspa</page:param>
        <page:param name="cssClass">top-label</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.concepts.search'"/></page:param>
        <page:param name="submitButtonName">Search</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'shareddashboards.search.title'"/></aui:param>
        </aui:component>

        <div class="aui-group">
            <div class="aui-item">
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="description"><ww:text name="'portalpage.search.text.desc'"/></page:param>
                    <aui:component label="text('common.concepts.search')" name="'searchName'" template="text.jsp" theme="'aui'">
                        <aui:param name="'maxlength'" value="50"/>
                    </aui:component>
                </page:applyDecorator>
            </div>
            <div class="aui-item">
                <page:applyDecorator name="auifieldgroup">
                    <aui:component label="text('admin.common.words.owner')"  id="'searchOwnerUserName'" name="'searchOwnerUserName'" template="userselect.jsp" theme="'aui'" />
                </page:applyDecorator>
            </div>
        </div>
    </page:applyDecorator>
    <div id="shared-dashboard-search-results" class="module">
         <jsp:include page="shared-dashboards-contents.jsp" />
    </div>
</body>
</html>
