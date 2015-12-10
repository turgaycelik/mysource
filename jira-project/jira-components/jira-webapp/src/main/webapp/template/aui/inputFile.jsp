<%--

Required Parameters:
    * label                     - i18n text for the label
    * name                      - The name of the attribute to put and pull the result from.
                                    Equates to the NAME parameter of the HTML INPUT tag.
Optional Parameters:
    * id                        - ID attribute (inherits computed IDs of its parents)
    * disabled (bool)           - sets disabled="disabled"
    * readonly (bool)           - sets readonly="readonly"
    * tabindex                  - TABINDEX attribute (try not to use this)
    * data                      - DATA attribute
    * accesskey                 - ACCESSKEY attribute
    * size                      - "very-short" || "short" || "medium" || "long" (leaving blank uses the default field width specified by AUI forms)
    * cssClass                  - set additional CSS classes
    * title                     - TITLE attribute
    * style                     - set inline styles (use sparingly)
    * labelAfter                - cause the <label> to be displayed after the <input>

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<ww:if test="parameters['labelAfter'] != true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<input <ww:property value="parameters['accesskey']"><ww:if test=".">accesskey="<ww:property value="."/>"</ww:if></ww:property>
    class="upfile
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
    type="file"
    <ww:property value="parameters['title']">
        <ww:if test=".">title="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['nameValue']">
        <ww:if test=".">value="<ww:property value="."/>"</ww:if>
    </ww:property>
/>
<ww:if test="parameters['labelAfter'] == true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<jsp:include page="/template/aui/formFieldIcon.jsp" />
<jsp:include page="/template/aui/formFieldError.jsp" />