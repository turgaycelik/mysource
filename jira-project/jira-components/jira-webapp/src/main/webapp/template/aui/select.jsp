<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<ww:if test="parameters['labelAfter'] != true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<select <ww:property value="parameters['accesskey']"><ww:if test=".">accesskey="<ww:property value="."/>"</ww:if></ww:property>
    class="select
    <ww:property value="parameters['size']">
        <ww:if test=". == 'long'">long-field</ww:if>
        <ww:elseIf test=". == 'medium'">medium-field</ww:elseIf>
        <ww:elseIf test=". == 'short'">short-field</ww:elseIf>
        <ww:elseIf test=". == 'very-short'">very-short-field</ww:elseIf>
        <ww:elseIf test=". == 'full'">full-width-field</ww:elseIf>
    </ww:property>
    <ww:property value="parameters['cssClass']"><ww:if test="."><ww:property value="."/></ww:if></ww:property>"
    <ww:property value="parameters['data']">
        <ww:if test=".">data="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:if test="parameters['disabled'] == true">
        disabled="disabled"
    </ww:if>
    <ww:property value="parameters['id']">
        <ww:if test=".">id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['maxlength']">
        <ww:if test=".">maxlength="<ww:property value="."/>"</ww:if>
    </ww:property>
    name="<ww:property value="parameters['name']"/>"
    <ww:if test="parameters['readonly'] == true">
        readonly="readonly"
    </ww:if>
    <ww:property value="parameters['style']">
        <ww:if test=".">style="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['tabindex']">
        <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
    </ww:property>
    type="text"
    <ww:property value="parameters['title']">
        <ww:if test=".">title="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['rows']">
        <ww:if test=".">multiple size="<ww:property value="."/>"</ww:if>
    </ww:property>>
    <ww:property value="parameters['defaultOptionText']">
        <ww:if test=". && . != ''">
            <option<ww:property value="parameters['defaultOptionValue']">
                <ww:if test=".">value="<ww:property value="parameters['defaultOptionValue']" />"</ww:if></ww:property>
                <ww:if test="../parameters['defaultOptionValue'] == ../parameters['nameValue']">
                    selected="selected"
                </ww:if>><ww:property value="." /></option>
        </ww:if>
    </ww:property>
    <ww:iterator value="parameters['list']">
        <option value="<ww:property value="."/>" <ww:if test="parameters['nameValue'] == .">selected="selected"</ww:if>><ww:property value="."/></option>
    </ww:iterator>
</select>
<ww:if test="parameters['labelAfter'] == true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<jsp:include page="/template/aui/formFieldIcon.jsp" />
<jsp:include page="/template/aui/formFieldError.jsp" />
