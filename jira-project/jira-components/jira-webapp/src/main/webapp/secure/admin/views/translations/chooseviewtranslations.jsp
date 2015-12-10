<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title>
        <ww:text name="'admin.issuesettings.translations'">
            <ww:param name="'value0'"><ww:property value="/issueConstantName" /></ww:param>
        </ww:text>
    </title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_types"/>
</head>
<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2>
                    <ww:text name="'admin.issuesettings.translations'">
                        <ww:param name="'value0'"><ww:property value="/issueConstantName" /></ww:param>
                    </ww:text>
                </h2>
            </div>
        </div>
    </header>
    <%@include file="viewtranslations.jsp"%>
</body>
</html>

