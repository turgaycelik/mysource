<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<ww:if test="parameters['labelBefore'] == true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<input <ww:if test="parameters['nameValue'] == true || parameters['checked'] == true">checked="checked"</ww:if>
    class="checkbox<ww:property value="parameters['cssClass']"><ww:if test="."> <ww:property value="." /></ww:if></ww:property>"
    <ww:if test="parameters['disabled'] == true">
        disabled="disabled"
    </ww:if>
    <ww:property value="parameters['id']">
        <ww:if test=".">id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if>
    </ww:property>
    name="<ww:property value="parameters['name']"/>"
    <ww:property value="parameters['style']">
        <ww:if test=".">style="<ww:property value="." />"</ww:if>
    </ww:property>
    type="checkbox"
    <ww:property value="parameters['tabindex']">
        <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['title']">
        <ww:if test=".">title="<ww:property value="."/>"</ww:if>
    </ww:property>
    value="<ww:property value="parameters['fieldValue']"/>"
/>
<ww:if test="parameters['labelBefore'] != true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<jsp:include page="/template/aui/formFieldError.jsp" />