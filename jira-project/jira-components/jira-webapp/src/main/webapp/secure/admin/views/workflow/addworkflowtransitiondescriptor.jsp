<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title>
        <ww:text name="'admin.workflowtransitions.add.something.to.transition'">
            <ww:param name="'value0'"><ww:text name="/descriptorNameKey"/></ww:param>
        </ww:text>
    </title>
</head>

<body>

    <page:applyDecorator name="jiraform">
        <page:param name="action"><ww:property value="/actionName"/>.jspa</page:param>
        <%-- NOTE!!! If you are changing the submit button name also need to change the
        AbstractAddWorkflowTransitionDescriptorParams.setupWorkflowDescriptorParams() method as it expects the
        name of the button as a parameter --%>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    	<page:param name="cancelURI"><ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url></page:param>
        <page:param name="title"><ww:text name="'admin.workflowtransitions.add.something.to.transition'">
            <ww:param name="'value0'"><ww:text name="/descriptorNameKey"/></ww:param>
        </ww:text></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="columns">1</page:param>

        <ui:component name="'workflowStep'" value="step/id" template="hidden.jsp" />
        <ww:property value="'standard'">
            <%@ include file="basicworkflowhiddenparameters.jsp" %>
        </ww:property>
        <ui:component name="'workflowTransition'" value="transition/id" template="hidden.jsp" />
        <ui:component name="'count'" template="hidden.jsp" />
        <ui:component name="'nested'" template="hidden.jsp" />

        <tr>
            <td>
            <ww:if test="/workflowModuleDescriptors && /workflowModuleDescriptors/empty == false">
                <table id="descriptors_table" class="aui aui-table-rowhover">
                    <thead>
                        <tr>
                            <th width="1%">&nbsp;</th>
                            <th width="20%"><ww:text name="'common.words.name'"/></th>
                            <th><ww:text name="'common.words.description'"/></th>
                        </tr>
                    </thead>
                    <tbody>
                    <ww:iterator value="/workflowModuleDescriptors" status="'status'">
                        <tr>
                            <td>
                                <input type="radio" name="type" id="<ww:property value="completeKey"/>" value="<ww:property value="completeKey"/>"/>
                            </td>
                            <td>
                                <label for="<ww:property value="completeKey"/>" >
                                    <ww:property value="./name"/>
                                </label>
                            </td>
                            <td>
                                <ww:property value="./description"/>
                            </td>
                        </tr>
                    </ww:iterator>
                    </tbody>
                </table>
            </ww:if>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.schemes.there.are.no.available.descriptors.to.add'">
                                <ww:param name="'value0'"><ww:text name="/descriptorNameKey"/></ww:param>
                            </ww:text>
                        </p>
                    </aui:param>
                </aui:component>
            </ww:else>
            </td>
        </tr>

    </page:applyDecorator>

</body>
</html>
