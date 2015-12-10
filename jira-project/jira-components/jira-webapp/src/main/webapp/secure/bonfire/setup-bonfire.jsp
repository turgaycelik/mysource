<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'setup.bonfire.title'" /></title>
    <meta content="setupbonfire" name="decorator"/>
</head>
<body>

<page:applyDecorator id="bonfire-setupwizard" name="auiform">
    <page:param name="action">SetupBonfire.jspa</page:param>
    <page:param name="submitButtonName">finish</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.finish'" /></page:param>
    <page:param name="cancelLinkURI"><ww:url value="'/secure/MyJiraHome.jspa'" atltoken="false"/></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.bonfire.step1'"/></aui:param>
    </aui:component>

    <ww:if test="/pluginInstalled == true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'setup.bonfire.info'"/></p>
                <p><ww:text name="'setup.bonfire.license.instruction'"/></p>
            </aui:param>
        </aui:component>

        <ww:text name="'setup.license.description.methods'"></ww:text>
        <ul>
            <li>
                <ww:text name="'setup.license.description.generate.eval'">
                    <ww:param name="'value0'"><a id="fetchLicense" data-url="Setup!fetchLicense.jspa" href="<ww:property value="/requestEvaluatorLicenseURL"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </li>
            <li>
                <ww:text name="'setup.license.description.retrieve'">
                    <ww:param name="'value0'"><a target="_blank" href="<ww:component name="'external.link.jira.licenses'" template="externallink.jsp"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </li>
        </ul>
        <br />
        <ww:text name="'setup.bonfire.proceed.instruction'">
            <ww:param name="'value0'"><a target="_blank" href="<ww:component name="'external.link.jira.licenses'" template="externallink.jsp"/>"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>

    </ww:if>

</page:applyDecorator>

<form class="upm-plugin-button-form" id="upm-plugin-button-form" method="post" name="bonfireplugin" action="<ww:property value="/requestEvaluatorLicenseURL"/>">
    <input type="hidden" value="<ww:property value="/callbackUrl"/>" name="callback">
    <input type="hidden" value="license" name="licensefieldname">
    <input type="hidden" value="<ww:property value="/organisation"/>" name="organisation_name">
    <input type="hidden" value="setupbonfire" name="referrer">
</form>

</body>
</html>
