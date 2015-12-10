<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<div id="captcha">
    <jsp:include page="/template/aui/formFieldLabel.jsp" />
    <input <ww:property value="parameters['accesskey']"><ww:if test=".">accesskey="<ww:property value="."/>"</ww:if></ww:property>
    class="text captcha-response
    <ww:property value="parameters['size']">
        <ww:if test=". == 'long'">long-field</ww:if>
        <ww:elseIf test=". == 'medium'">medium-field</ww:elseIf>
        <ww:elseIf test=". == 'short'">short-field</ww:elseIf>
        <ww:elseIf test=". == 'very-short'">very-short-field</ww:elseIf>
        <ww:elseIf test=". == 'full'">full-width-field</ww:elseIf>
    </ww:property>
    <ww:property value="parameters['cssClass']"><ww:if test="."><ww:property value="."/></ww:if></ww:property>"
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
    <ww:property value="parameters['nameValue']">
        <ww:if test=".">value="<ww:property value="."/>"</ww:if>
    </ww:property>
    />
    <div class="captcha-container">
        <img class="captcha-image" src="<ww:property value="parameters['captchaURI']"/>" alt=""/>
        <a class="captcha-trigger" href="<ww:property value="parameters['iconURI']"><ww:if test="."><ww:property value="."/></ww:if><ww:else>#</ww:else></ww:property>"<ww:property value="parameters['id']"><ww:if test="."> id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>-icon"</ww:if></ww:property><ww:property value="parameters['iconTitle']"><ww:if test="."> title="<ww:property value="."/>"</ww:if></ww:property>>
            <span class="aui-icon captcha-reload<ww:property value="parameters['iconCssClass']"><ww:if test="."> <ww:property value="."/></ww:if></ww:property>"><ww:property value="parameters['iconText']" /></span>
        </a>
    </div>
</div>