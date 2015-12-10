<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<ww:property value="parameters['label']">
    <ww:if test=". && ./equals('') == false"><%-- Need the following all on one line as IE7 doesn't ignore the white space --%>
        <label<ww:property value="parameters['id']"><ww:if test="."> for="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if></ww:property>><ww:property value="."/><ww:if test="parameters['mandatory'] == true"><span class="aui-icon icon-required"> <ww:text name="'AUI.form.label.text.required'"/></span></ww:if></label>
    </ww:if>
</ww:property>