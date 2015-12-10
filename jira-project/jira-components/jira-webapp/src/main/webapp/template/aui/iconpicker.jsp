<%--

Required Parameters:
    * label                     - i18n text for the label
    * name                      - The name of the attribute to put and pull the result from.
                                    Equates to the NAME parameter of the HTML INPUT tag.
Optional Parameters:
    * id                        - ID attribute (inherits computed IDs of its parents)
    * maxlength                 - MAXLENGTH attribute
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
    * description               - descriptive text that will appear after the text

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>

<jsp:include page="/template/aui/formFieldLabel.jsp" />
<ww:if test="parameters['id']">
    <ww:property id="icon.picker.id" value="parameters['id']"/>
</ww:if>
<ww:else>
    <ww:property id="icon.picker.id" value="@jira.sitemesh.decorator.computed.id"/>
</ww:else>
<input <ww:property value="parameters['accesskey']"><ww:if test=".">accesskey="<ww:property value="."/>"</ww:if></ww:property>
    class="text
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
    id="<ww:property value="@icon.picker.id" />"
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
[<a class="jira-iconpicker-trigger" href="<ww:url page="/secure/popups/IconPicker.jspa" atltoken="false"><ww:param name="'fieldType'" value="parameters['fieldType']" /><ww:param name="'fieldId'" value="@icon.picker.id"/></ww:url>" id="<ww:property value="@icon.picker.id + '-trigger'"/>"><ww:text name="'admin.text.image.select.image'"/></a>]
<jsp:include page="/template/aui/formFieldError.jsp" />
