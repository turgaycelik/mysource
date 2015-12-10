AJS.test.require("jira.webresources:jqlautocomplete");

(function() {

    function _testParse(query, expectedResult) {


        test("test query: " + query, function() {
            // parser
            var parser = JIRA.JQLAutoComplete.MyParser(ALL_RESERVED_WORDS);

            var parseResult = parser.parse(query);

            if (expectedResult === PARSE_FAIL) {
                ok(parseResult.parseError, "Expected parsing to fail");
            }
            else {
                ok(!parseResult.parseError, "Expected parsing to be successful");
            }
        });
    }

    // constants
    var PARSE_FAIL = new String("PARSE_FAIL"),
        SUCCESS = new String("SUCCESS"),
        ILLEGAL_CHARS_STRING = "{}*/%+$#@?;",
        SOME_RESERVED_WORDS = ["abort", "access", "add", "after", "alias", "all", "alter", "and", "any", "as", "asc"],
        ALL_RESERVED_WORDS = ["greater", "for", "privileges", "float", "validate", "distinct", "of", "break", "defaults", "byte"
            , "initial", "file", "noaudit", "empty", "on", "false", "boolean", "right", "option", "decrement", "limit", "else"
            , "increment", "fetch", "equals", "or", "number", "table", "like", "create", "row", "declare", "not", "trans", "asc"
            , "start", "session", "then", "view", "strict", "explain", "go", "unique", "desc", "raise", "exclusive", "before"
            , "next", "inout", "goto", "date", "nowait", "escape", "mode", "character", "rownum", "union", "encoding", "delete"
            , "current", "whenever", "left", "do", "null", "end", "min", "trigger", "intersection", "define", "max", "previous"
            , "integer", "sqrt", "return", "true", "checkpoint", "divide", "join", "access", "alter", "field", "delimiter"
            , "string", "exists", "modulo", "having", "public", "insert", "abort", "uid", "to", "last", "grant", "count"
            , "transaction", "synonym", "inner", "char", "drop", "rename", "collate", "by", "where", "long", "identified"
            , "prior", "function", "revoke", "after", "remainder", "values", "more", "commit", "when", "any", "power", "notin"
            , "returns", "avg", "index", "execute", "minus", "select", "int", "double", "size", "rows", "and", "difference", "input"
            , "default", "isempty", "intersect", "column", "exec", "output", "cf", "update", "raw", "connect", "set", "catch", "sum"
            , "object", "from", "add", "collation", "while", "share", "order", "isnull", "if", "less", "between", "all", "with", "is"
            , "check", "alias", "resource", "lock", "into", "modify", "audit", "as", "multiply", "in", "decimal", "begin", "subtract"
            , "immediate", "outer", "continue", "group", "user", "rowid", "first"];


    // locals
    var i,
        fieldName,
        currentChar;

    _testParse("priority = \"qwerty\"", SUCCESS);
    _testParse("priority = \"qwerty\"", SUCCESS);
    _testParse("priority=\"qwerty\"", SUCCESS);
    _testParse("priority=qwerty", SUCCESS);
    _testParse("  priority=qwerty  ", SUCCESS);
    _testParse("priority=     qwerty order      by priority, other", SUCCESS);

    //Some tests for the other operators.
    _testParse("coolness >= awesome", SUCCESS);
    _testParse("coolness > awesome", SUCCESS);
    _testParse("coolness < awesome", SUCCESS);
    _testParse("coolness <= awesome", SUCCESS);
    _testParse("coolness        !=       awesome order     by     coolness desc", SUCCESS);

    //Some tests for the in operator.
    _testParse("language in (java, c, \"python2\")", SUCCESS);
    _testParse("languagein   IN    (   java, c     , \"python2\")", SUCCESS);
    _testParse("inlanguage in (java, c, \"python2\")", SUCCESS);
    _testParse("pri in (java,c,\"python2\")", SUCCESS);
    _testParse("pri in(java)", SUCCESS);
    _testParse("pri In(java)", SUCCESS);
    _testParse("pri iN(java)", SUCCESS);

    //Some tests for the NOT in operator.
    _testParse("language not in (java, c, \"python2\")", SUCCESS);
    _testParse("languagein  NOT   IN    (   java, c     , \"python2\")", SUCCESS);
    _testParse("inlanguage not in (java, c, \"python2\")", SUCCESS);
    _testParse("pri NOT in (java,c,\"python2\")", SUCCESS);
    _testParse("pri not in(java)", SUCCESS);
    _testParse("pri NoT In(java)", SUCCESS);
    _testParse("pri nOT iN(java)", SUCCESS);

    // Some tests for the LIKE operator.
    _testParse("pri ~ stuff", SUCCESS);
    _testParse("pri~stuff", SUCCESS);
    _testParse("pri ~ 12", SUCCESS);
    _testParse("pri~12", SUCCESS);
    _testParse("pri ~ (\"stuff\", 12)", SUCCESS);

    // Some tests for the NOT_LIKE operator.
    _testParse("pri !~ stuff", SUCCESS);
    _testParse("pri!~stuff", SUCCESS);
    _testParse("pri !~ 12", SUCCESS);
    _testParse("pri!~12", SUCCESS);
    _testParse("pri !~ (\"stuff\", 12)", SUCCESS);

    // Some tests for the IS operator
    _testParse("pri IS stuff", SUCCESS);
    _testParse("pri is stuff", SUCCESS);
    _testParse("pri IS EMPTY", SUCCESS);

    // Some tests for the IS_NOT operator
    _testParse("pri IS NOT stuff", SUCCESS);
    _testParse("pri IS not stuff", SUCCESS);
    _testParse("pri is Not stuff", SUCCESS);
    _testParse("pri is not stuff", SUCCESS);


    //Test for the nested behaviour of in clause.
    _testParse("pri iN((java), duke)", SUCCESS);

    //Test to make sure that numbers are returned correctly.
    _testParse("priority = 12345", SUCCESS);
    _testParse("priority = -12345", SUCCESS);
    _testParse("priority = \"12a345\"", SUCCESS);
    _testParse("priority = 12345a", SUCCESS);

    //Test custom field labels
    _testParse("cf[12345] = 12345a", SUCCESS);
    _testParse("Cf  [ 0005 ] = x", SUCCESS);

    //Make sure that a quoted number is actually returned as a string.
    _testParse("priority = \"12345\"", SUCCESS);

    //An invalid number should be returned as a string.
    _testParse("priority=\"12a345\"", SUCCESS);

    //Some tests to check the empty operand
    _testParse("testfield = EMPTY", SUCCESS);
    _testParse("testfield = empty", SUCCESS);
    _testParse("testfield = NULL", SUCCESS);
    _testParse("testfield = null", SUCCESS);
    _testParse("testfield = \"null\"", SUCCESS);
    _testParse("testfield = \"NULL\"", SUCCESS);
    _testParse("testfield = \"EMPTY\"", SUCCESS);
    _testParse("testfield = \"empty\"", SUCCESS);

    // tests for quoted strings with characters that must be quoted
    _testParse("priority = \"a big string ~ != foo and priority = haha \"", SUCCESS);
    _testParse("priority = \"\"", SUCCESS);

    //test for strange field names.
    _testParse("prior\\'ty = testvalue", SUCCESS);
    _testParse("priority\\ ty=testvalue", SUCCESS);
    _testParse("priority\u2ee5 > 6", SUCCESS);
    _testParse("priori\\nty\\u2ee5 > 6", SUCCESS);
    _testParse("\\     < 38", SUCCESS);
    _testParse("\"this is a strange field \" = google", SUCCESS);
    _testParse("\"don't\" = 'true'", SUCCESS);
    _testParse("\"don\\\"t\" = 'false'", SUCCESS);
    _testParse("'don\"t' = 'false'", SUCCESS);
    _testParse("'cf[1220]' = abc", SUCCESS);
    _testParse("'cf' = abc", SUCCESS);
    _testParse("10245948 = abc      order          by 10245948", SUCCESS);
    _testParse("-10245948 = abc", SUCCESS);
    _testParse("new\\nline = abc", SUCCESS);
    _testParse("some\\u0082control = abc  order by some\\u0082control", SUCCESS);

    //test for strange field values.
    _testParse("b = ''", SUCCESS);
    _testParse("b = \\ ", SUCCESS);
    _testParse("b = don\\'t\\ stop\\ me\\ now", SUCCESS);
    _testParse("b = \u2ee5", SUCCESS);
    _testParse("b = \\u2EE5jkdfskjfd", SUCCESS);
    _testParse("b not in 'jack says, \"Hello World!\"'", SUCCESS);
    _testParse("b not in 'jack says, \\'Hello World!\\''", SUCCESS);
    _testParse("b not in \"jack says, 'Hello World!'\"", SUCCESS);
    _testParse("b not in \"jack says, \\\"Hello World!'\\\"\"", SUCCESS);
    _testParse("b not in \"jack says, \\tnothing\"", SUCCESS);
    _testParse("bad ~ wt\\u007f", SUCCESS);

    //tests for escaping.
    _testParse("priority = \"a \\n new \\r line\"", SUCCESS);
    _testParse("priority = \"Tab:\\t NewLine:\\n Carrage Return:\\r\"", SUCCESS);
    _testParse("priority = \"Quote:\\\" Single:\\' Back Slash:\\\\ Space:\\ \"", SUCCESS);
    _testParse("priority = \"Unicode: \\ufeeF1 Unicode2: \\u6EEF\"", SUCCESS);
    _testParse("priority = 'Escape\" don\\'t'", SUCCESS);
    _testParse("priority = \"Escape' don\\\"t\"", SUCCESS);

    //Some tests for function calls.
    _testParse("priority = higherThan(Major)", SUCCESS);
    _testParse("priority In     randomName(Major, Minor,      \"cool\", -65784)", SUCCESS);
    _testParse("priority    >=    randomName()", SUCCESS);
    _testParse("pri not in fun(name\\u0082e)", SUCCESS);

    //test for strange function names.
    _testParse("a = func\\'  ()", SUCCESS);
    _testParse("a = fu\\\"nc\\'()", SUCCESS);
    _testParse("a=function\\ name(  )", SUCCESS);
    _testParse("a = \u2ee5()", SUCCESS);
    _testParse("a = somereallystrangestring\\u2ee5()", SUCCESS);
    _testParse("version <= \"affected\\ versions\"(   )", SUCCESS);
    _testParse("version <= \"affected\\ versio'ns\"(   )", SUCCESS);
    _testParse("version <= \"affected versio\\\"ns\"(   )", SUCCESS);
    _testParse("version <= 'my messed up versio\\'ns'     (   )", SUCCESS);
    _testParse("version <= 'my m\\nessed up\\ versio\"ns'     (   )", SUCCESS);
    _testParse("version <= 4759879855`(   )", SUCCESS);
    _testParse("version <= 4759879(   )", SUCCESS);
    _testParse("version = badname\\u0091", SUCCESS);

    //test some of the string breaks
    _testParse("a=b&c=d", SUCCESS);
    _testParse("a=b&&c=d", SUCCESS);
    _testParse("a=b|c=d", SUCCESS);
    _testParse("a=b||c=d", SUCCESS);
    _testParse("a<b", SUCCESS);
    _testParse("a>b", SUCCESS);
    _testParse("a~b", SUCCESS);

    //Check the and operator.
    _testParse("priority = major and foo > bar()", SUCCESS);
    _testParse("priority = majorand and foo>bar()", SUCCESS);
    _testParse("priority = major and foo > bar()", SUCCESS);
    _testParse("priority != major    and      foo >      bar()", SUCCESS);
    _testParse("priority != major    &&      foo >      bar()", SUCCESS);
    _testParse("priority != andmajor    &      foo >      bar()", SUCCESS);
    _testParse("priority != andmajor    and      foo >      bar() order by priority     DESC,      foo", SUCCESS);

    // Some tests for valid operator parsing
    _testParse("a =b", SUCCESS);
    _testParse("a !=b", SUCCESS);
    _testParse("a >=b", SUCCESS);
    _testParse("a <=b", SUCCESS);
    _testParse("a !~b", SUCCESS);
    _testParse("a ~b", SUCCESS);

    //Check the or operator.
    _testParse("priority = major or foo > bar()", SUCCESS);
    _testParse("priority = major or foo > bar()", SUCCESS);
    _testParse("priority = major or foo > bar() or priority = minor", SUCCESS);
    _testParse("priority = major || foo > bar() | priority = minor", SUCCESS);
    _testParse("priority = major or foo > bar() || priority = minor", SUCCESS);

    //Checks for operator precedence for and and or.
    _testParse("priority = major and foo > bar(1,2,3) or priority = minor and baz != 1234", SUCCESS);
    _testParse("priority =     major AND foo > bar(1,2,3) oR priority = minor and baz != 1234", SUCCESS);
    _testParse("priority=major and foo>bar(1,2,3)|| priority=minor  and  baz!=1234", SUCCESS);
    _testParse("priority = major AND foo > bar(1,2,3) Or priority = minor AND baz != 1234", SUCCESS);

    //Another test for the and operator.
    _testParse("priority = major && foo > bar(1,2,3) AND priority = minor and baz != 1234", SUCCESS);

    // use parentheses to overthrow precedence
    _testParse("priority = major and (foo > bar(1,2,3) OR priority = minor) and baz != 1234", SUCCESS);

    //make sure the precedence still works with the brackets.
    _testParse("priority = major or (foo > bar(1,2,3) or priority = minor) and baz != 1234", SUCCESS);

    //test for the not operator.
    _testParse("not priority = major or foo > bar() or priority = minor", SUCCESS);
    _testParse("not priority = major or foo > bar() AnD priority=\"minor\"", SUCCESS);
    _testParse("not priority = major or not foo > bar() AnD priority=\"minor\"", SUCCESS);
    _testParse("not (priority = major or not foo > bar()) AnD priority=\"minor\"", SUCCESS);

    //check the '!' operator.
    _testParse("! (priority = major or ! foo > bar()) AnD priority=\"minor\"", SUCCESS);

    //check th changed operator
    _testParse("! (priority changed from major or ! foo > bar()) AnD priority changed to minor", SUCCESS);

    //test that a double not also works.
    _testParse("not not (not priority = major or     foo >bar()) and         priority=\"minor\"", SUCCESS);

    //Tests to make sure illegal characters can be escaped.
    for (i = 0; i < ILLEGAL_CHARS_STRING.length; i++) {
        currentChar = ILLEGAL_CHARS_STRING.charAt(i);
        fieldName = "test" + currentChar + "dfjd";
        _testParse("'" + fieldName + "' = 'good'", SUCCESS);
    }

    //We want to be able to add sort when there is no where clause.
    _testParse("order by crap", SUCCESS);
    _testParse("order by crap  DESC", SUCCESS);
    _testParse("order by crap  ASC", SUCCESS);

    _testParse("", SUCCESS);
    _testParse("affectedVersion = \"New Version 1\" order by affectedVersion         ASC,         affectedVersion       DESC, cf[1234]  DESC    ", SUCCESS);

    // JRA-18391 Test that new lines work
    _testParse("(not (resolutiondate <= \"2008-01-01\") and (type in (Improvement)) or \"Priority for beta phase\" = 3) and\nassignee = currentUser() order by status", SUCCESS);
    _testParse("(not (resolutiondate <= \"2008-01-01\") and (type in (Improvement)) or \"Priority for beta phase\" = 3) or\nassignee = currentUser() order by status", SUCCESS);
    _testParse("(not (resolutiondate <= \"2008-01-01\") and (type in (Improvement)) or \"Priority for beta phase\" = 3) and not\nassignee = currentUser() order by status", SUCCESS);

    //JDEV-25614
    _testParse("issue.property[x]=y", SUCCESS);
    _testParse("issue.property[x.y].test.path = y", SUCCESS);
    _testParse("issue.property[x.y].test.path = y.a", SUCCESS);
    _testParse("issue.property = y", PARSE_FAIL);
    _testParse("issue.property", PARSE_FAIL);
    _testParse("issue.property[.x] = y", PARSE_FAIL);
    _testParse("issue.property[x] = x", SUCCESS);
    _testParse("issue.property[issue.status] = resolved", SUCCESS);
    _testParse("ISSUE.property[\"issue.status\"] = resolved", SUCCESS);
    _testParse("issue.property['issue.status'] = resolved", SUCCESS);
    _testParse("issue.property     ['issue.status'] = resolved", SUCCESS);
    _testParse("issue.property[\'1@4s\'] = resolved", SUCCESS);
    _testParse("issue.property[1234] = resolved", SUCCESS);
    _testParse("issue.property[-1234] = resolved", SUCCESS);
    _testParse("issue.property.x = y", PARSE_FAIL);
    _testParse("issue.ProPeRty[\'-@,@\'] = resolved", SUCCESS);
    _testParse("comment.prop[author]= filip", PARSE_FAIL);
    _testParse("comment.prop[author]..a= filip", PARSE_FAIL);
    _testParse("version = 1.2.3", SUCCESS);


    // Some tests for invalid parsing input.
    _testParse("       ", PARSE_FAIL);
    _testParse("foo", PARSE_FAIL);
    _testParse("foo=", PARSE_FAIL);
    _testParse("=", PARSE_FAIL);
    _testParse("!=", PARSE_FAIL);
    _testParse("foo bar and 78foo = bar", PARSE_FAIL);
    _testParse("and and", PARSE_FAIL);
    _testParse("a=b a=b", PARSE_FAIL);
    _testParse("foo=bar and", PARSE_FAIL);
    _testParse("foo=bar and and", PARSE_FAIL);
    _testParse("foo=bar and 78foo", PARSE_FAIL);
    _testParse("foo=bar and 78foo=", PARSE_FAIL);
    _testParse("foo=bar and \n78foo", PARSE_FAIL);
    _testParse("foo=bar and 78foo brenden and a=b", PARSE_FAIL);
    _testParse("foo=bar and \n78foo brenden and a=b", PARSE_FAIL);
    _testParse("foo=bar and not", PARSE_FAIL);
    _testParse("foo=bar and not foo =", PARSE_FAIL);
    _testParse("not", PARSE_FAIL);
    _testParse("not not", PARSE_FAIL);
    _testParse("a=b and not not=b", PARSE_FAIL);
    _testParse("a=b not a=b", PARSE_FAIL);
    _testParse("(", PARSE_FAIL);
    _testParse("abc = ()", PARSE_FAIL);
    _testParse("abc ~ ()", PARSE_FAIL);
    _testParse("abc = )foo(", PARSE_FAIL);
    _testParse("abc in (foo", PARSE_FAIL);
    _testParse("abc in (foo(", PARSE_FAIL);
    _testParse("abc IN ((foo)", PARSE_FAIL);
    _testParse("abc in (foo))", PARSE_FAIL);
    _testParse("abc in ((fee, fie, foe, fum), 787, (34, (45))", PARSE_FAIL);
    _testParse("priority = 12345=== not p jfkff fjfjfj", PARSE_FAIL);
    _testParse("priority = 12345 jfkff fjfjfj", PARSE_FAIL);
    _testParse("priority=a jfkff=fjfjfj", PARSE_FAIL);
    _testParse("priority=12345 jfkff=fjfjfj", PARSE_FAIL);
    _testParse("a=b ,b", PARSE_FAIL);
    _testParse("a=b,b", PARSE_FAIL);

    // Some tests for invalid operator parsing
    _testParse("a inb", PARSE_FAIL);
    _testParse("a isb", PARSE_FAIL);
    _testParse("a is notb", PARSE_FAIL);
    _testParse("a not inb", PARSE_FAIL);

    // JRA-18392 Some tests for reserved words in fieldNames and in values


    for (i = 0; i < SOME_RESERVED_WORDS.length; i++) {
        _testParse(SOME_RESERVED_WORDS[i] + " = stuff", PARSE_FAIL);
        _testParse("stuff = " + SOME_RESERVED_WORDS[i], PARSE_FAIL);
    }

    _testParse("cf[1234 = x", PARSE_FAIL);
    _testParse("cf1234] = x", PARSE_FAIL);
    _testParse("cf[z123] = x", PARSE_FAIL);
    _testParse("cf[-123] = x", PARSE_FAIL);
    _testParse("cf[] = x", PARSE_FAIL);
    _testParse("[54] = x", PARSE_FAIL);

    _testParse("pri notin (x)", PARSE_FAIL);
    _testParse("pri isnot empty", PARSE_FAIL);
    _testParse("pri ^ empty", PARSE_FAIL);
    _testParse("pri ! in (test)", PARSE_FAIL);
    _testParse("pri is ! empty", PARSE_FAIL);

    _testParse("priority = \"\"\"", PARSE_FAIL);
    _testParse("priority = \"", PARSE_FAIL);
    _testParse("priority = '''", PARSE_FAIL);
    _testParse("priority = '", PARSE_FAIL);

    _testParse("what = hrejw'ewjrhejkw", PARSE_FAIL);
    _testParse("wh\"at = hrejwewjrhejkw", PARSE_FAIL);

    _testParse("'' = bad", PARSE_FAIL);
    _testParse("\"\" = bad", PARSE_FAIL);
    _testParse("a = ''()", PARSE_FAIL);
    _testParse("b = \"\"()", PARSE_FAIL);

    _testParse("test = case\\", PARSE_FAIL);
    _testParse("test = case\\k", PARSE_FAIL);
    _testParse("test = case\\u", PARSE_FAIL);
    _testParse("test = case\\u278q", PARSE_FAIL);
    _testParse("test = case\\u27", PARSE_FAIL);
    _testParse("test = case\\u-998", PARSE_FAIL);
    _testParse("test = case order by \\u-998", PARSE_FAIL);
    _testParse("test = case\\uzzzz", PARSE_FAIL);
    _testParse("test = case\\u278qzzzz", PARSE_FAIL);
    _testParse("test = case\\u27zzzzz", PARSE_FAIL);
    _testParse("tecase\\u-998zzzzz", PARSE_FAIL);
    _testParse("case order by \\u-998zzzzz", PARSE_FAIL);

    _testParse("cf = brenden", PARSE_FAIL);
    _testParse("cf = broken", PARSE_FAIL);

    _testParse("\u0000iuyiuyiu", PARSE_FAIL);
    _testParse("aa = order by \u0001", PARSE_FAIL);

    _testParse("cont\nrol = ''", PARSE_FAIL);
    _testParse("control = f\run()", PARSE_FAIL);

    _testParse("a = b order by", PARSE_FAIL);
    _testParse("a = b order bya", PARSE_FAIL);
    _testParse("a = b order", PARSE_FAIL);
    _testParse("a = b order BY abc desc, ", PARSE_FAIL);
    _testParse("a = b order BY abc desc,qq,", PARSE_FAIL);
    _testParse("a = b order BY abc qq asc,", PARSE_FAIL);

    // Check some spacing stuff
    _testParse("a = b ANDaffectedVersion=hello", PARSE_FAIL);
    _testParse("a = b ORaffectedVersion=hello", PARSE_FAIL);
    _testParse("a = b ORNOTaffectedVersion=hello", PARSE_FAIL);

    //Tests to make sure illegal characters cause an error.
    for (i = 0; i < ILLEGAL_CHARS_STRING.length; i++) {
        currentChar = ILLEGAL_CHARS_STRING.charAt(i);
        _testParse("test" + currentChar + "dfjd = 'bad'", PARSE_FAIL);
    }

    //Some lexical errors
    //This test may no longer be valid when we expand the queries.
    _testParse("f\n = \n \n abc *", PARSE_FAIL);
    _testParse("f *= abc", PARSE_FAIL);
})();



