<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	  <title><ww:text name="'admin.workflowtransitions.add.workflow.transition'"/></title>
</head>
<body>
    <page:applyDecorator id="add-workflow" name="auiform">
        <page:param name="action">AddWorkflowTransition.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="submitButtonName">Add</page:param>
        <page:param name="cancelLinkURI"><ww:property value="/cancelUrl" /></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.workflowtransitions.add.workflow.transition'"/></aui:param>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.workflowtransitions.create.a.transition.from'">
                        <ww:param name="'value0'"><b><ww:property value="/workflow/descriptor/step(step/id)/name" /></b></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('admin.workflowtransitions.transition.name')" name="'transitionName'" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.description')" name="'description'" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:select label="text('admin.workflowtransitions.destination.step')" name="'destinationStep'" list="workflow/descriptor/steps" listKey="'id'" listValue="'name'" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:select label="text('admin.workflowtransitions.transition.view')" name="'view'" list="/fieldScreens" listKey="'id'" listValue="'name'" theme="'aui'">
                <aui:param name="'defaultOptionText'"><ww:text name="'admin.workflowtransitions.no.view.for.transition'"/></aui:param>
                <aui:param name="'defaultOptionValue'" value="''" />
            </aui:select>
            <page:param name="description"><ww:text name="'admin.workflowtransitions.the.screen.that.appears.for.this.transition'"/></page:param>
        </page:applyDecorator>

        <aui:component name="'originatingUrl'" template="hidden.jsp" theme="'aui'"  />
        <aui:component name="'workflowStep'" value="step/id" template="hidden.jsp" theme="'aui'" />
        <ww:property value="'aui'">
            <%@ include file="basicworkflowhiddenparameters.jsp" %>
        </ww:property>

    </page:applyDecorator>
</body>
</html>
