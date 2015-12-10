
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'admin.xmlimport.import.data.from.xml'"/></title>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">XmlImport.jspa</page:param>
    <page:param name="cancelURI"><ww:url value="'/secure/admin/views/ExternalImport.jspa'" /></page:param>
    <page:param name="submitId">import_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.import'"/></page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="title"><ww:text name="'admin.xmlimport.import.data.from.xml'"/></page:param>
    <page:param name="width">100%</page:param>
</page:applyDecorator>

<%@include file="importlogs.jsp" %>

</body>
</html>
