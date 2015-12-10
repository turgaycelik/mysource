<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- textlabel.jsp
  --
  -- Required Parameters:
  --   * label      - The description that will be used to identfy the control.
  --   * name       - The name of the attribute to put and pull the result from.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ include file="/template/standard/controlheader.jsp" %>

      <ww:property value="parameters['nameValue']">
         <ww:if test="."><span id="<ww:property value="parameters['name']" />"><ww:property value="."/></span></ww:if>
      </ww:property>

	  <ww:property value="parameters['texthtml']">
         <ww:if test="."><ww:property value="." escape="false"/></ww:if>
      </ww:property>

<%@ include file="/template/standard/controlfooter.jsp" %>
