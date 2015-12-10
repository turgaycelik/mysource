<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigurations.associate.field.to.schemes'">
	    <ww:param name="'value0'"><ww:property value="/field/name" /></ww:param>
	</ww:text></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screens"/>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="jiraformId">add-field-to-screen</page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigurations.associate.field.to.schemes'">
        <ww:param name="'value0'"><ww:property value="/field/name" /></ww:param>
    </ww:text></page:param>
    <page:param name="description">
        <ww:text name="'admin.issuefields.fieldconfigurations.associate.description'">
            <ww:param name="'value0'"><ww:property value="/field/name" /></ww:param>
            <ww:param name="'value1'"><ww:text name="'admin.issuefields.fieldconfigurations.associate.a.tab'"/></ww:param>
        </ww:text>
    </page:param>
    <page:param name="action">AssociateFieldToScreens.jspa</page:param>
    <page:param name="cancelURI"><ww:url page="ViewFieldScreens.jspa" /></page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>

    <ui:component name="'fieldId'" template="hidden.jsp" theme="'single'" />

    <tr>
        <td colspan="2">
            <table id="screenAssociations" class="aui aui-table-rowhover">
                <thead>
                    <tr>
                        <th><ww:text name="'admin.common.words.screen'"/></th>
                        <th><ww:text name="'admin.associatefield.tab'"/></th>
                        <th class="minWidth"><ww:text name="'common.words.select'"/></th>
                    </tr>
                </thead>
                <tbody>
                <ww:iterator value="/screens" status="'status'">
                    <tr>
                        <td><ww:property value="./name" /></td>
                    <ww:if test="./tabs/size == 1">
                        <td>
                            <ww:property value="./tabs/iterator()/next()" id="tabObject" />
                            <ww:property value="@tabObject/name" />
                            <ui:component name="./id" template="hidden.jsp"  value="@tabObject/id" />
                        </td>
                    </ww:if>
                    <ww:else>
                        <ui:select label="''" name="./id"
                                   list="./tabs" listKey="'id'" listValue="'name'"
                                   template="selectmap.jsp" theme="'single'" value="/selectedTabForScreen(.)/id"
                                   >
                        </ui:select>
                    </ww:else>
                        <td class="fullyCentered minWidth">
                            <ui:checkbox theme="'single'" label="''" name="'associatedScreens'" fieldValue="./id">
                                <ui:param name="'checked'"><ww:if test="/selectedTabForScreen(.)">true</ww:if></ui:param>
                            </ui:checkbox>
                        </td>
                    </tr>
                </ww:iterator>
                </tbody>
            </table>
        </td>
    </tr>

</page:applyDecorator>

</body>
</html>
