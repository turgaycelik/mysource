<%@ taglib prefix="ww" uri="webwork" %>
<html>
<head>
    <title><ww:text name="'admin.globalsettings.defaultdashboard.configure.default.dashboard'" /></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/user_interface"/>
    <meta name="admin.active.tab" content="edit_default_dashboard"/>
</head>
<body>
<div class="jiraform maxWidth">
    <div class="jiraformheader">
        <h3 class="formtitle"><ww:text name="'admin.globalsettings.defaultdashboard.configure.default.dashboard'" /></h3>
        <br/>
        <ww:text name="'admin.globalsettings.defaultdashboard.note'" value0="'<span class=\"note\">'" value1="'</span>'"/>
    </div>
    <div class="jiraformcontents"><ww:property value="/dashboardHtml" escape="false"/></div>
</div>
</body>
</html>
