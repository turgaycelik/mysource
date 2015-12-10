<%--
  --
  -- Required Parameters:
  --   * label     - The description that will be used to identfy the control.
  --   * name      - The name of the attribute to put and pull the result from.
  --                 Equates to the NAME parameter of the HTML tag INPUT.
  --   * trueLabel
  --   * falseLabel
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --   * disabledTrue        - DISABLED parameter of the HTML INPUT tag.
  --   * tabindexTrue        - tabindex parameter of the HTML INPUT tag.
  --   * onchangeTrue        - onkeyup parameter of the HTML INPUT tag.
  --   * checkRadio      - determine which radio option is checked by default
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/template/standard/controlheader.jsp" %>
  <input type="radio"
         name="<ww:property value="parameters['name']"/>"
         value="true"
         id="<ww:property value="parameters['name']" />_true"
     <ww:property value="parameters['disabledTrue']">
        <ww:if test=".">disabled="disabled"</ww:if>
     </ww:property>
     <ww:property value="parameters['tabindexTrue']">
        <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
     </ww:property>
     <ww:property value="parameters['onclickTrue']">
        <ww:if test=".">onclick="<ww:property value="."/>"</ww:if>
     </ww:property>
     <ww:if test="parameters['checkRadio'] == true || parameters['nameValue'] == true">
         checked="checked"
     </ww:if>
  />
   <label for="<ww:property value="parameters['name']" />_true">
      <ww:property value="parameters['trueLabel']">
      <ww:if test=".">
        <ww:property value="." />
      </ww:if>
      <ww:else>
        On
      </ww:else>
      </ww:property>
   </label>
  <input type="radio"
         name="<ww:property value="parameters['name']"/>"
         value="false"
         id="<ww:property value="parameters['name']" />_false"
     <ww:property value="parameters['disabledFalse']">
        <ww:if test=".">disabled="disabled"</ww:if>
     </ww:property>
     <ww:property value="parameters['tabindexFalse']">
        <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
     </ww:property>
     <ww:property value="parameters['onclickFalse']">
        <ww:if test=".">onclick="<ww:property value="."/>"</ww:if>
     </ww:property>
     <ww:if test="parameters['checkRadio'] == false || parameters['nameValue'] == false">
         checked="checked"
     </ww:if>
  />
   <label for="<ww:property value="parameters['name']" />_false">
      <ww:property value="parameters['falseLabel']">
      <ww:if test=".">
        <ww:property value="." />
      </ww:if>
      <ww:else>
        Off
      </ww:else>
      </ww:property>
   </label>

<ww:property value="parameters['fieldBody']">
    <ww:if test="."><ww:property value="." escape="false"/></ww:if>
</ww:property>

<%@ include file="/template/standard/controlfooter.jsp" %>
