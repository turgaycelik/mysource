<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.translations.delete.title'">
	    <ww:param name="'value0'"><ww:property value="/issueConstantName" /></ww:param>
	    <ww:param name="'value1'"><ww:property value="/issueConstantTypeName" /></ww:param>
	</ww:text></title>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.translations.delete.title'">
	    <ww:param name="'value0'"><ww:property value="/issueConstantName" /></ww:param>
	    <ww:param name="'value1'"><ww:property value="/issueConstantTypeName" /></ww:param>
	</ww:text></page:param>
	<page:param name="description">
		<p><ww:text name="'admin.translations.delete.confirmation'"/></p>
	</page:param>

	<page:param name="action"><ww:property value="/deleteName" />.jspa?typeId=<ww:property value="typeId" />&selectedLocale=<ww:property value="/selectedLocale"/></page:param>
	<page:param name="submitId">delete_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="cancelURI"><ww:property value="/viewName" />!default.jspa?typeId=<ww:property value="typeId" /></page:param>

    <tr><td bgcolor="#ffffff">
    <table border="0" cellpadding="3" cellspacing="1" width=100% align="center" class="gridBox">
        <tr width="100%" bgcolor="#f0f0f0">
                <td align=absmiddle class="colHeaderLink" width="20%" >
                    <b><ww:text name="'admin.issuesettings.translations.locale'"/></b>
                </td>
                <td align=absmiddle class="colHeaderLink">
                    <b><ww:text name="'admin.common.words.translation'"/></b>
                </td>
        </tr>
        <tr bgcolor="#ffffff">
        <td align=lef>
            <b><ww:property value="./selectedLocale" /></b>
        </td>
        <td align=left>
            <b><ww:text name="'common.words.name'"/>:</b> <ww:property value="./nameTranslation" />
            <br>
            <b><ww:text name="'common.words.description'"/>:</b> <ww:property value="./descTranslation" />
        </td>
        </tr>
    </table>
    </td></tr>

	<ui:component name="'typeId'" template="hidden.jsp" />
	<ui:component name="'translation'" template="hidden.jsp" />

</page:applyDecorator>

</body>
</html>
