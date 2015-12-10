<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- textarea.jsp
  --
  -- Required Parameters:
  --   * label  - The description that will be used to identfy the control.
  --   * name   - The name of the attribute to put and pull the result from.
  --              Equates to the NAME parameter of the HTML INPUT tag.
  --   * cols   - Width of the textarea.  Equates to the COLS parameter of
  --              HTML tag TEXTAREA.
  --   * rows   - Height of the textarea.  Equates to the ROWS parameter of
  --              HTML tag TEXTAREA.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --   * disabled  - DISABLED parameter of the HTML TEXTAREA tag.
  --   * readonly  - READONLY parameter of the HTML TEXTAREA tag.
  --   * onkeyup   - onkeyup parameter of the HTML TEXTAREA tag.
  --   * tabindex  - tabindex parameter of the HTML TEXTAREA tag.
  --   * onchange  - onkeyup parameter of the HTML TEXTAREA tag.
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ include file="/template/single/controlheader.jsp" %>

<textarea name="<ww:property value="parameters['name']"/>"
          cols="<ww:property value="parameters['cols']"/>"
          rows="<ww:property value="parameters['rows']"/>"
          wrap="virtual"
         <ww:property value="parameters['disabled']">
            <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
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
         <ww:property value="parameters['onfocus']">
            <ww:if test=".">onfocus="<ww:property value="."/>"</ww:if>
         </ww:property>
         <ww:property value="parameters['class']">
            <ww:if test=".">class="<ww:property value="."/>"</ww:if>
         </ww:property>
><ww:property value="parameters['nameValue']" escape="true"/></textarea>
<%@ include file="/template/single/controlfooter.jsp" %>
