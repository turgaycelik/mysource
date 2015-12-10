
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.trustedapps.delete.app'"/>: <ww:property value="/name" /></title>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.trustedapps.delete.app'"/>: <ww:property value="/name" /></page:param>

    <ww:if test="/editable == true">
    	<page:param name="submitId">delete_submit</page:param>
    	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="description">
            <p><ww:text name="'admin.trustedapps.delete.confirmation'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"><ww:property value="/name" /></ww:param>
                <ww:param name="'value2'"></strong></ww:param>
            </ww:text></p>
        </page:param>
    </ww:if>

	<page:param name="width">100%</page:param>
	<page:param name="action">DeleteTrustedApplication.jspa</page:param>
    <page:param name="cancelURI">ViewTrustedApplications.jspa</page:param>
	<page:param name="autoSelectFirst">false</page:param>

	<ui:component name="'id'" template="hidden.jsp" />
	<input type="hidden" name="confirm" value="true">
</page:applyDecorator>

</body>
</html>
