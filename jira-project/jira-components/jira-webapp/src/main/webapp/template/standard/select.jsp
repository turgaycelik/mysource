<%@ page import="webwork.util.ValueStack"%>
 <%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- select.jsp
  --
  -- Required Parameters:
  --   * label  - The description that will be used to identfy the control.
  --   * name   - The name of the attribute to put and pull the result from.
  --              Equates to the NAME parameter of the HTML SELECT tag.
  --   * list   - Iterator that will provide the options for the control.
  --              Equates to the HTML OPTION tags in the SELECT and supplies
  --              both the NAME and VALUE parameters of the OPTION tag.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --   * disabled        - DISABLED parameter of the HTML SELECT tag.
  --   * tabindex        - tabindex parameter of the HTML SELECT tag.
  --   * onchange        - onkeyup parameter of the HTML SELECT tag.
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/template/standard/controlheader.jsp" %>
<select name="<ww:property value="parameters['name']"/>"
      <ww:property value="parameters['disabled']">
         <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
      </ww:property>
      <ww:property value="parameters['tabindex']">
         <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['onchange']">
         <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['style']">
         <ww:if test=".">style="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['class']">
         <ww:if test=".">class="<ww:property value="."/>"</ww:if>
      </ww:property>
>

   <ww:property value="parameters['headerrow']">
      <ww:if test=". && . != ''"><option
		value="<ww:property value="parameters['headervalue']" />"
		<ww:if test="../parameters['headerrow'] == ../parameters['nameValue']">selected</ww:if>
		><ww:property value="parameters['headerrow']" /></option></ww:if>
   </ww:property>
   <ww:iterator value="parameters['list']">
      <option value="<ww:property value="."/>" <ww:if test="parameters['nameValue'] == .">SELECTED</ww:if>>
            <ww:property value="."/>
      </option>
   </ww:iterator>
</select>

<%@ include file="/template/standard/controlfooter.jsp" %>
