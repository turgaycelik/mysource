package com.atlassian.jira.issue.index.analyzer;


import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.IOException;

/**
 *
 * This Filter adds wildcards to the tokens returned by standard filters.
 *
 * <p/>
 * It can be difficult to do  wild card search over a stemmed index - for example 'feature' will be stemmed to
 * feature and if you search for 'feature*' you will not match 'feature'.
 * To search for 'feature*' then stem first and then wildcard.
 *
 * The need for this was highlighted by (JRA-19918)
 *
 * This filter stems and then appends the wildcard to each existing token, so it makes no sense for it not to
 *  be the final filter in the chain. note that the query returned will actually not work (it will look for the
 *  litral * as it is a term query - use the query to get the text and then search)
 *
 * @since v4.3
 */
public class WildcardFilter extends TokenFilter
{
    private TermAttribute termAtt;
    
   /**
   *  Wildcrad character: U+002A (ASTERISK)
   */
  public static final char WILDCARD_OPERATOR = '\u002A';

    public WildcardFilter(TokenStream in) {
    super(in);
    termAtt = (TermAttribute) addAttribute(TermAttribute.class);
  }

  public final boolean incrementToken() throws IOException
  {
    if (input.incrementToken()) {
      int len = termAtt.termLength();
      len++;
      termAtt.resizeTermBuffer(len);
      termAtt.termBuffer()[len - 1] = WILDCARD_OPERATOR;
      termAtt.setTermLength(len);
      return true;
    } else {
      return false;
    }
  }

}
