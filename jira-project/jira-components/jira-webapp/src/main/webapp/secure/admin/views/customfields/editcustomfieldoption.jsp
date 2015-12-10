<%@ taglib uri="webwork" prefix="webwork" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
	<title><webwork:text name="'admin.issuefields.customfields.edit.option'">
	    <webwork:param name="'value0'"> <webwork:property value="/customField/name" /></webwork:param>
	</webwork:text></title>
</head>
<body>



<page:applyDecorator name="jiraform">
	<page:param name="title"><webwork:text name="'admin.issuefields.customfields.edit.option'">
	    <webwork:param name="'value0'"> <webwork:property value="/customField/name" /></webwork:param>
	</webwork:text>
    </page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="width">100%</page:param>
	<page:param name="description">
        <webwork:if test='/fieldLocked == false'>
            <webwork:if test='/fieldManaged == true'>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><webwork:text name="/managedFieldDescriptionKey" /></p>
                    </aui:param>
                </aui:component>
            </webwork:if>
            <p>
                <ui:textfield label="text('admin.issuefields.customfields.edit.option.value')" name="'value'" value="value" mandatory="true"/>
            </p>
        </webwork:if>
    </page:param>
	<page:param name="action">EditCustomFieldOptions!update.jspa</page:param>
    <webwork:if test='/fieldLocked == false'>
	    <page:param name="submitName"><webwork:text name="'common.words.update'"/></page:param>
    </webwork:if>
	<page:param name="cancelURI"><webwork:property value="/urlWithParent('default')" /></page:param>

	<ui:component name="'fieldConfigId'" template="hidden.jsp" theme="'single'"  />
	<ui:component name="'selectedParentOptionId'" template="hidden.jsp" theme="'single'"  />
	<ui:component name="'selectedValue'" template="hidden.jsp" theme="'single'"  />

</page:applyDecorator>

</body>
</html>
