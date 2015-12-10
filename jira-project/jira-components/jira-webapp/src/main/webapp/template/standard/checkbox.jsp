<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- checkbox.jsp
  --
  -- Required Parameters:
  --   * label       - The description that will be used to identfy the control.
  --   * name        - The name of the attribute to put and pull the result from.
  --                   Equates to the NAME parameter of the HTML INPUT tag.
  --   * fieldValue  - The value displayed by the control.  Equates to the value
  --                   of the HTML INPUT tag.
  --
  -- Optional Parameters:
  --   * id              - sets the id of the HTML INPUT tag.
  --   * disabled        - DISABLED parameter of the HTML INPUT tag.
  --   * tabindex        - tabindex parameter of the HTML INPUT tag.
  --   * onchange        - onkeyup parameter of the HTML INPUT tag.
  --%>


<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/template/standard/controlheader.jsp" %>
<input type="checkbox" <ww:if test="parameters['nameValue'] == true">checked="checked"</ww:if>
       name="<ww:property value="parameters['name']"/>"
       value="<ww:property value="parameters['fieldValue']"/>"
     <ww:property value="parameters['checked']">
        <ww:if test=".">checked="checked"</ww:if>
     </ww:property>
     <ww:property value="parameters['id']">
        <ww:if test=".">id="<ww:property value="." />"</ww:if>
     </ww:property>
     <ww:property value="parameters['class']">
        <ww:if test=".">class="<ww:property value="." />"</ww:if>
     </ww:property>
     <ww:property value="parameters['style']">
        <ww:if test=".">style="<ww:property value="." />"</ww:if>
     </ww:property>
     <ww:property value="parameters['disabled']">
        <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
     </ww:property>
     <ww:property value="parameters['tabindex']">
        <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
     </ww:property>
     <ww:property value="parameters['onchange']">
        <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
     </ww:property>
/>

<%@ include file="/template/standard/controlfooter.jsp" %>
