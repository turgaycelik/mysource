<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title>
    	<ww:text name="'moveissue.title'"/>
    </title>
    <meta name="decorator" content="panel-general" />
</head>
<body>
    <page:applyDecorator name="jirapanel">
    	<page:param name="title"><ww:text name="'moveissue.title'"/></page:param>
    </page:applyDecorator>
</body>
</html>
