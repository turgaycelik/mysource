<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- selectgroupmap.jsp
  --
  -- Required Parameters:
  --   * label     - The description that will be used to identfy the control.
  --   * name      - The name of the attribute to put and pull the result from.
  --                 Equates to the NAME parameter of the HTML tag SELECT.
  --   * list      - Iterator that will provide the options for the control.
  --                 Equates to the HTML OPTION tags in the SELECT.
  --   * groupKey  - Where to get the values for the OPTGROUP tag.  Equates to
  --                 the LABEL parameter of the OPTION tag.
  --   * groupList - Iterator that will provide the options for the group.
  --                 Must refer to an attribute of the "list" parameter elements
  --   * listKey   - Where to get the values for the OPTION tag.  Equates to
  --                 the VALUE parameter of the OPTION tag.
  --   * listValue - The value displayed by the control.  Equates to the body
  --                 of the HTML OPTION tag.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --   * disabled        - DISABLED parameter of the HTML SELECT tag.
  --   * tabindex        - tabindex parameter of the HTML SELECT tag.
  --   * onchange        - onkeyup parameter of the HTML TEXTAREA tag.
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
>
   <ww:iterator value="parameters['list']">
      <optgroup label="<ww:property value="{parameters['groupKey']}"/>">
         <ww:iterator value="{parameters['groupList']}">
            <option value="<ww:property value="{parameters['listKey']}"/>" <ww:if test="{parameters['listKey']} == parameters['nameValue']">SELECTED</ww:if>>
               <ww:property value="{parameters['listValue']}"/>
            </option>
         </ww:iterator>
      </optgroup>
   </ww:iterator>
</select>

<%@ include file="/template/standard/controlfooter.jsp" %>
