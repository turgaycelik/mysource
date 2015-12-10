<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.trustedapps.view.trusted.applications'"/></title>
</head>
<body>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.trustedapps.view.trusted.applications'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">trustedapps.list</page:param>
        <p>
             <ww:text name="'admin.trustedapps.view.trusted.applications.description'"/>
        </p>
    </page:applyDecorator>

    <p/>
        <table align=center bgcolor="#bbbbbb" border="0" cellpadding="0" cellspacing="0" width="60%">
            <tr>
                <td>
                    <table border="0" cellpadding="3" cellspacing="1" width=100%>
                        <tr class="rowHeader">
                            <td class="colHeaderLink">
                                <b><ww:text name="'common.words.name'"/></b>
                            </td>
                            <td class="colHeaderLink">
                                <b><ww:text name="'common.words.operations'"/></b>
                            </td>
                        </tr>

                        <ww:iterator value="/trustedApplications" status="'status'">
                            <tr class="<ww:if test="@status/modulus(2) == 1">rowNormal</ww:if><ww:else>rowAlternate</ww:else>">
                                <td valign="top">
                                    <b><ww:property value="./name"/></b>
                                    <ww:if test="./validKey == false">
                                        <br>
                                        <img src="<ww:url page="/images/icons/emoticons/error.png"/>" alt="<ww:property value="./publicKey" />"/>
                                        <font size="-2"><ww:property value="./publicKey" /></font>
                                    </ww:if>
                                </td>
                                <td width="1%" valign="top" nowrap="true">
                                    <ww:if test="./validKey == true"><%-- only show if the key is valid --%>
                                        <a id="edit-<ww:property value="./numericId"/>" href="<ww:url page="/secure/admin/trustedapps/EditTrustedApplication!default.jspa"><ww:param name="'id'" value="./numericId"/></ww:url>" title="<ww:text name="'admin.trustedapps.edit.trusted.app'">
                                            <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                                        </ww:text>"><ww:text name="'common.words.edit'"/></a>
                                    |</ww:if>
                                     <a id="delete-<ww:property value="./numericId"/>" href="<ww:url page="/secure/admin/trustedapps/DeleteTrustedApplication!default.jspa"><ww:param name="'id'" value="./numericId"/></ww:url>" title="<ww:text name="'admin.trustedapps.delete.trusted.app'">
                                        <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                                    </ww:text>"><ww:text name="'common.words.delete'"/></a>
                                </td>
                            </tr>
                        </ww:iterator>
                        <ww:if test="trustedApplications/size == 0">
                        <tr class="rowNormal">
                            <td colspan="2"><ww:text name="'admin.trustedapps.no.apps.configured'"/></td>
                        </tr>
                        </ww:if>
                    </table>
                </td>
            </tr>
        </table>
    <p/>

    <p>
    <page:applyDecorator name="jiraform">
        <page:param name="action">ViewTrustedApplications!request.jspa</page:param>
        <page:param name="submitId">request_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.send.request'"/></page:param>
        <page:param name="title"><ww:text name="'admin.trustedapps.request.new.trusted.app'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <ww:text name="'admin.trustedapps.request.new.app.instructions'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
            </ww:text>
        </page:param>

        <ui:textfield label="text('admin.trustedapps.words.base.url')" name="'trustedAppBaseUrl'" size="'60'">
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>
    </page:applyDecorator>
    </p>
</body>
</html>
