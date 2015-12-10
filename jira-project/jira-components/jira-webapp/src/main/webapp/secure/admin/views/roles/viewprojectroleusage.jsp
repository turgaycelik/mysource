<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title>
        <ww:text name="'admin.projectroles.usage.title'">
            <ww:param value="/htmlEncode(/role/name)" />
        </ww:text>
    </title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="project_role_browser"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
	<page:param name="title">
        <ww:text name="'admin.projectroles.usage.title'"><ww:param value="/htmlEncode(/role/name)" /></ww:text>
    </page:param>
    <page:param name="helpURL">project_roles</page:param>
    <page:param name="helpURLFragment">#project_role_browser</page:param>
    <page:param name="description">
        <p>
        <ww:text name="'admin.projectroles.usage.desc'">
           <ww:param name="'value0'"><b></ww:param>
           <ww:param name="'value1'"><ww:param value="/htmlEncode(/role/name)" /></ww:param>
           <ww:param name="'value2'"></b></ww:param>
        </ww:text>
        </p>
    </page:param>
    <page:param name="width">100%</page:param>
    <ul class="optionslist">
        <li><a id="return_link" href="ViewProjectRoles.jspa"><ww:text name="'admin.projectroles.returnlink'"/></a></li>
    </ul>
</page:applyDecorator>
   <jsp:include page="associatedschemestables.jsp"/>
</body>
</html>
