<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:shared-filters-admin");
    %>
    <meta name="admin.active.section" content="admin_users_menu/shared_section"/>
    <meta name="admin.active.tab" content="shared_filters"/>
    <%@ include file="/includes/decorators/xsrftoken.jsp" %>
	<title><ww:text name="'admin.issues.filters.shared.title'"/></title>
</head>
<body>
    <page:applyDecorator id="search-filters-form" name="auiform">
        <page:param name="action">ViewSharedFilters.jspa</page:param>
        <page:param name="cssClass">top-label</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.concepts.search'"/></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'sharedfilters.search.title'"/></aui:param>
        </aui:component>
        <p><ww:text name="'sharedfilters.search.long.desc'"/></p>
        <div class="aui-group">
            <div class="aui-item">
                <page:applyDecorator name="auifieldgroup">
                    <aui:textfield label="text('common.concepts.search')" maxlength="50" id="'searchName'" name="'searchName'" theme="'aui'">
                        <aui:param name="'description'" value="text('filters.search.text.desc')"/>
                    </aui:textfield>
                </page:applyDecorator>
            </div>
            <div class="aui-item">
                <page:applyDecorator name="auifieldgroup">
                    <aui:component label="text('admin.common.words.owner')" id="'searchOwnerUserName'" name="'searchOwnerUserName'" template="userselect.jsp" theme="'aui'">
                        <ui:param name="'formname'" value="'search-filters-form'"/>
                        <ui:param name="'mandatory'" value="false"/>
                    </aui:component>
                </page:applyDecorator>
            </div>
        </div>
    </page:applyDecorator>
    <div id="shared-filter-search-results">
         <jsp:include page="shared-filters-content.jsp" />
    </div>
</body>
</html>
