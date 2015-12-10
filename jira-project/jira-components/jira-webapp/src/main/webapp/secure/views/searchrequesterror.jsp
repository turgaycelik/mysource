<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
	<title><ww:text name="'search.request.error.title'"/></title>
</head>
<body>
	<table cellpadding=1 cellspacing=0 border=0 align=center width=80% bgcolor=#cc0000><tr><td>
	<table cellpadding=4 cellspacing=0 border=0 width=100% bgcolor=#ffffff><tr><td>
		<font color="#cc0000"><b><ww:text name="'search.request.error.title'"/></b></font>
		<p>
            <ww:text name="'search.request.invalid.permission'"/>
		</p>
		<p>
        <ww:text name="'contact.admin.for.perm'">
            <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
        </ww:text>
		</p>
	</td></tr></table>
	</td></tr></table>
</body>
</html>
