<%--
  -- Required Parameters:
  --   * label      - The description that will be used to identfy the control.
  --   * name       - The name of the attribute to put and pull the result from.
  --                  Equates to the NAME parameter of the HTML INPUT tag.
  --
  -- Optional Parameters:
+  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --   * size       - SIZE parameter of the HTML INPUT tag.
  --   * maxlength  - MAXLENGTH parameter of the HTML INPUT tag.
  --   * cssClass   - set additional CSS classes
  --   * disabled   - DISABLED parameter of the HTML INPUT tag.
  --   * readonly   - READONLY parameter of the HTML INPUT tag.
  --   * tabindex  - tabindex parameter of the HTML INPUT tag.
  --
 --%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<ww:if test="parameters['labelAfter'] != true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<input <ww:property value="parameters['autocomplete']"><ww:if test="."> autocomplete="<ww:property value="."/>"</ww:if></ww:property>
    <ww:if test="parameters['disabled'] == true">
        disabled="disabled"
    </ww:if>
    class="text
    <ww:property value="parameters['size']">
        <ww:if test=". == 'long'"> long-field</ww:if>
        <ww:elseIf test=". == 'medium'"> medium-field</ww:elseIf>
        <ww:elseIf test=". == 'short'"> short-field</ww:elseIf>
    </ww:property>
    <ww:property value="parameters['cssClass']"><ww:if test="."> <ww:property value="."/></ww:if></ww:property>"
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
    type="password"
    <ww:property value="parameters['style']">
        <ww:if test=".">style="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['tabindex']">
        <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['fieldTitle']">
        <ww:if test=".">title="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['nameValue']">
        <ww:if test=".">value="<ww:property value="."/>"</ww:if>
    </ww:property>
/>
<ww:if test="parameters['labelAfter'] == true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<jsp:include page="/template/aui/formFieldIcon.jsp" />
<jsp:include page="/template/aui/formFieldError.jsp" />
