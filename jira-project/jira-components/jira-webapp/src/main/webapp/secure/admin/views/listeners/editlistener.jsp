
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.listeners.edit.listener'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="listeners"/>
</head>

<body>

<page:applyDecorator name="jiraform">
	<page:param name="action">EditListener.jspa</page:param>
	<page:param name="submitId">update_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="width">100%</page:param>
	<page:param name="cancelURI">ViewListeners!default.jspa</page:param>
	<page:param name="title"><ww:text name="'admin.listeners.edit.listener2'">
	    <ww:param name="'value0'"><ww:property value="listener/string('name')" /></ww:param>
	</ww:text></page:param>
	<page:param name="description">
        <ww:if test="/jiraListener/description">
            <p>
                <b><ww:text name="'common.words.description'"/>:</b><br/>
                <ww:property value="/jiraListener/description" escape="false" />
            </p>
        </ww:if>
            <p><ww:text name="'admin.listeners.edit.instructions'"/></p>
    </page:param>

	<ww:iterator value="acceptedParams">
	<tr>
		<td class="fieldLabelArea">
			<ww:property value="." />
		</td>
		<td class="fieldValueArea">
			<input type="text" name="<ww:property value="." />" value="<ww:property value="paramValue(.)" />"/>
		</td>
	</tr>
	</ww:iterator>

	<ui:component name="'id'" template="hidden.jsp" />
</page:applyDecorator>

</body>
</html>
