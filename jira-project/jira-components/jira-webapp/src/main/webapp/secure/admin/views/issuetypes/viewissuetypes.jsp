<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%-- The page is used for the manageable option object --%>
<ww:property value="/manageableOption" >
<html>
<head>
	<title><ww:text name="'admin.issuesettings.issuetypes.view.title'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_types"/>
</head>
<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2><ww:text name="'admin.issuesettings.issuetypes.view.title'"/></h2>
            </div>
            <div class="aui-page-header-actions">
                <div class="aui-buttons">
                    <a id="add-issue-type" class="aui-button trigger-dialog" href="AddNewIssueType.jspa">
                        <span class="icon jira-icon-add"></span>
                        <ww:text name="'admin.issuesettings.issuetypes.add.new.button.label'"/>
                    </a>
                </div>
                <aui:component name="'manageIssueTypes'" template="help.jsp" theme="'aui'" />
            </div>
        </div>
    </header>
    <jsp:include page="/secure/admin/views/issuetypes/issuetypes.jsp" />
</body>
</html>
</ww:property>
