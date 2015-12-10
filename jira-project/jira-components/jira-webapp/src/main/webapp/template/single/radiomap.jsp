<%--
  -- WebWork, Web Application Framework
  --
  -- Distributable under LGPL license.
  -- See terms of license at opensource.org
  --
  --
  -- radiomap.jsp
  --
  -- Required Parameters:
  --   * label     - The description that will be used to identfy the control.
  --   * name      - The name of the attribute to put and pull the result from.
  --                 Equates to the NAME parameter of the HTML tag INPUT.
  --   * list      - Iterator that will provide the options for the control.
  --                 Equates to the HTML LABEL tags.
  --   * listKey   - Where to get the values for the INPUT tag.  Equates to
  --                 the VALUE parameter of the INPUT tag.
  --   * listValue - The value displayed next to the radio control.  Equates to the body
  --                 of the enclosing HTML LABEL tag.
  --
  -- Optional Parameters:
  --   * labelposition   - determines were the label will be place in relation
  --                       to the control.  Default is to the left of the control.
  --   * disabled        - DISABLED parameter of the HTML INPUT tag.
  --   * tabindex        - tabindex parameter of the HTML INPUT tag.
  --   * onchange        - onkeyup parameter of the HTML INPUT tag.
  --   * checkRadio      - determine which radio option is checked by default
  --
  --%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ include file="/template/single/controlheader.jsp" %>
<%@ include file="/template/common/radiomap.jsp"%>
<%@ include file="/template/single/controlfooter.jsp" %>
