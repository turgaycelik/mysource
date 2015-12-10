<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%--
If you just want to display a single radio input please provide the 'list' attribute with value of 'null':
<aui:radio id="'radio'" label="'radio'" list="null" name="'radio'" theme="'aui'" value="'somevalue'"/>
--%>
<ww:if test="parameters['list'] == null">
    <input <ww:property value="parameters['accesskey']"><ww:if test=".">accesskey="<ww:property value="."/>"</ww:if></ww:property>
        class="radio<ww:property value="parameters['cssClass']"><ww:if test="."> <ww:property value="."/></ww:if></ww:property>"
        <ww:if test="parameters['disabled'] == true">
            disabled="disabled"
        </ww:if>
        <ww:if test="parameters['checked'] == true">
            checked="checked"
        </ww:if>
        <ww:property value="parameters['id']">
            <ww:if test=".">id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if>
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
        type="radio"
        <ww:property value="parameters['title']">
            <ww:if test=".">title="<ww:property value="."/>"</ww:if>
        </ww:property>
        <ww:property value="parameters['customValue']">
            <ww:if test=".">value="<ww:property value="."/>"</ww:if>
            <ww:elseIf test="parameters['nameValue']">value="<ww:property value="parameters['nameValue']"/>"</ww:elseIf>
        </ww:property>
    />
    <jsp:include page="/template/aui/formFieldLabel.jsp" />
    <jsp:include page="/template/aui/formFieldIcon.jsp" />
</ww:if>
<ww:else>
    <ww:iterator value="parameters['list']" status="'status'">
        <div class="radio">
            <input
                <ww:if test="{parameters['listKey']} == '' && !parameters['nameValue']"> checked="checked"</ww:if>
                <ww:if test="{parameters['listKey']} == parameters['nameValue']"> checked="checked"</ww:if>
                <ww:if test="{parameters['listKey']} == parameters['checkRadio']"> checked="checked"</ww:if>
                class="radio"
                <ww:if test="parameters['disabled'] == true">
                    disabled="disabled"
                </ww:if>
                <ww:property value="{parameters['listKey']}">
                    <ww:if test=".">id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="parameters['id']"/><ww:property value="."/>"</ww:if>
                </ww:property>
                name="<ww:property value="parameters['name']"/>"
                <ww:property value="parameters['tabindex']">
                    <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
                </ww:property>
                type="radio"
                <ww:property value="{parameters['listKey']}">
                    <ww:if test=".">value="<ww:property value="."/>"</ww:if>
                </ww:property>
            />
            <label <ww:property value="parameters['labelAccesskey']"><ww:if test=".">accesskey="<ww:property value="."/>"</ww:if></ww:property>
                <ww:property value="{parameters['listKey']}">
                    <ww:if test="."> for="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="parameters['id']"/><ww:property value="."/>"</ww:if>
                </ww:property>>
                <ww:property value="{parameters['listValue']}" escape="false" />
                <ww:if test="parameters['mandatory'] == true">
                    <span class="aui-icon icon-required"><ww:text name="'AUI.form.label.text.required'"/></span>
                </ww:if>
            </label>
        </div>
    </ww:iterator>
</ww:else>
<jsp:include page="/template/aui/formFieldError.jsp" />
