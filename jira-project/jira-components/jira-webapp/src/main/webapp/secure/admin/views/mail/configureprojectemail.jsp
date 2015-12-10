
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="aui" uri="webwork" %>

<html>
<head>
	<title><ww:text name="'admin.mailservers.configure'"/></title>
</head>

<body>

<div class="content intform">

    <page:applyDecorator name="auiform">

        <page:param name="action"><ww:url value="'/secure/ProjectEmail.jspa'"/></page:param>

        <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="submitButtonName">update</page:param>

        <page:param name="cancelLinkURI"><ww:url value="'/plugins/servlet/project-config/' + /projectKey" atltoken="false" /></page:param>

        <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
            <aui:param name="'title'"><ww:text name="'admin.mailservers.configure'"/></aui:param>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <ww:text name="'admin.mailservers.sender.address.description'"/>
            </aui:param>
        </aui:component>

        <aui:component theme="'aui'" template="hidden.jsp" name="'projectId'" value="/projectId"/>
        <page:applyDecorator name="auifieldgroup">
            <aui:textfield id="'fromAddress'" label="text('admin.mailservers.sender.address')" name="'fromAddress'" size="'long'" theme="'aui'" />
            <page:param name="description"><ww:text name="'admin.mailservers.sender.update'"/></page:param>
        </page:applyDecorator>

    </page:applyDecorator>

</div>

</body>
</html>
