<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title>
        <ww:if test="/request == true">
            <ww:text name="'admin.trustedapps.add.trusted.application'"/>
        </ww:if>
        <ww:else>
            <ww:text name="'admin.trustedapps.edit.trusted.application'"/>
        </ww:else>
    </title>
</head>
<body>
	<page:applyDecorator name="jiraform">
        <page:param name="title">
            <ww:if test="/request == true">
                <ww:text name="'admin.trustedapps.add.trusted.application'"/>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.trustedapps.edit.trusted.application'"/>
            </ww:else>
        </page:param>
        <page:param name="helpURL">trustedapps.edit</page:param>

		<page:param name="action">EditTrustedApplication.jspa</page:param>
		<page:param name="description"><p><ww:text name="'admin.trustedapps.edit.description'"/></p>
            <p class="warningText"><ww:text name="'admin.trustedapps.edit.warning'">
                <ww:param name="value0"><strong></ww:param>
                <ww:param name="value1"></strong></ww:param>
            </ww:text></p></page:param>
		<page:param name="width">100%</page:param>
		<page:param name="cancelURI">ViewTrustedApplications.jspa</page:param>

        <ww:if test="/editable == true">
            <%-- only put submit in if we have an editable form --%>
            <ww:if test="/request == true">
                <page:param name="submitId">add_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
            </ww:if>
            <ww:else>
                <page:param name="submitId">update_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
            </ww:else>

            <ui:textfield label="text('admin.trustedapps.field.application.name')" name="'name'" size="100" />

            <ui:textfield label="text('admin.trustedapps.field.application.id')" name="'applicationId'" size="100" readonly="true" />

            <ui:component name="'publicKey'" template="hidden.jsp" theme="'single'"  />

            <ui:textfield label="text('admin.trustedapps.field.timeout')" name="'timeout'">
                <ui:param name="'description'">
                    <ww:text name="'admin.trustedapps.field.timeout.description'" />
                </ui:param>
            </ui:textfield>

            <ui:textarea label="text('admin.trustedapps.field.ip.matches')" name="'ipMatch'" rows="5" cols="40">
                <ui:param name="'description'">
                    <ww:text name="'admin.trustedapps.field.ip.matches.description'" />
                </ui:param>
            </ui:textarea>

            <ui:textarea label="text('admin.trustedapps.field.url.matches')" name="'urlMatch'" rows="5" cols="40">
                <ui:param name="'description'">
                    <ww:text name="'admin.trustedapps.field.url.matches.description'" />
                </ui:param>
            </ui:textarea>
        </ww:if>

        <ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
        <%-- record what page to redirect after success --%>
        <ui:component name="'redirectURI'" template="hidden.jsp" theme="'single'"  />
	</page:applyDecorator>

</body>
</html>
