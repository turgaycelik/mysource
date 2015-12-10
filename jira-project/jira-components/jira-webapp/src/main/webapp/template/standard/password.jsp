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
  --   * label  - The description that will be used to identfy the control.
  --   * name   - The name of the attribute to put and pull the result from.
  --              Equates to the NAME parameter of the HTML INPUT tag.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --   * size            - SIZE parameter of the HTML INPUT tag.
  --   * maxlength       - MAXLENGTH parameter of the HTML INPUT tag.
  --   * disabled        - DISABLED parameter of the HTML INPUT tag.
  --   * readonly        - READONLY parameter of the HTML INPUT tag.
  --   * onkeyup         - onkeyup parameter of the HTML INPUT tag.
  --   * tabindex        - tabindex parameter of the HTML INPUT tag.
  --   * onchange        - onkeyup parameter of the HTML INPUT tag.
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/template/standard/controlheader.jsp" %>

<input type="password"
       name="<ww:property value="parameters['name']"/>"
      <ww:property value="parameters['size']">
         <ww:if test=".">size="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['maxlength']">
         <ww:if test=".">maxlength="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['disabled']">
         <ww:if test="parameters['disabled'] == true">DISABLED</ww:if>
      </ww:property>
      <ww:property value="parameters['readonly']">
         <ww:if test="{parameters['readonly']}">READONLY</ww:if>
      </ww:property>
      <ww:property value="parameters['onkeyup']">
         <ww:if test=".">onkeyup="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['tabindex']">
         <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['onchange']">
         <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
      </ww:property>
      <ww:property value="parameters['autocomplete']">
         <ww:if test=".">autocomplete="<ww:property value="."/>"</ww:if>
      </ww:property>
>
<%@ include file="/template/standard/controlfooter.jsp" %>
