import difflib
import pprint

class MockedMethod(object):
    MODE_EXPECT = 1,
    MODE_ORDERED = 2

    def __init__(self, name: str):
        self.expected_calls = {}
        self.ordered_calls = []
        self.made_calls = []
        self.default = None
        self.mock_method_name = name
        self.mode = None

    def __as_key(self, args: tuple, kwargs: dict):
        key = [str(a) for a in args]
        key.append('(positionals::%s)' % str(len(args)))
        for k, v in kwargs.items():
            key.append('%s:%s' % (k, str(v)))
        key.append('(keyword::%s)' % str(len(kwargs)))
        return ''.join(key)

    def __can_run_in_mode(self, mode):
        if self.mode is None:
            self.mode = mode
        elif self.mode != mode:
            raise InvalidStateException('Cannot use ordered method with non-ordered!')

    def XX_mock_ordered(self, *args, **kwargs):
        self.__can_run_in_mode(self.MODE_ORDERED)
        if 'toReturn' not in kwargs:
            raise KeyError('Mock: ordered must have toReturn parameter defined!')
        expected_return = kwargs['toReturn']
        del(kwargs['toReturn'])
        self.ordered_calls += [{'args': args, 'kwargs': kwargs, 'result': expected_return}]

    def XX_mock_verify_ordered(self, *args, **kwargs):
        if len(self.ordered_calls) > 0:
            raise AssertionError('Not all ordered invocations of method >> %s << were called! Remaining: %s' % (
                self.mock_method_name, self.ordered_calls))

    def XX_mock_expect(self, *args, **kwargs):
        self.__can_run_in_mode(self.MODE_EXPECT)
        if 'toReturn' not in kwargs:
            raise KeyError('Mock: expect must have toReturn parameter defined!')
        expected_return = kwargs['toReturn']
        del(kwargs['toReturn'])
        key = self.__as_key(args, kwargs)
#        if key in self.expected_calls:
#            raise KeyError('Mock: %s(): already mocked for these parameters: "%s"!' % (self.mock_method_name, key))
#            print('Mock: %s(): already mocked for these parameters: "%s"!' % (self.mock_method_name, key))
        self.expected_calls[key] = expected_return

    def XX_mock_verify(self, *args, **kwargs):
        return self.__as_key(args, kwargs) in self.made_calls

    def XX_mock_default(self, value):
        self.default = value

    def __prepare_diff(self, expected, found):
        diff = ('\n'.join(difflib.ndiff(
            pprint.pformat(expected).splitlines(),
            pprint.pformat(found).splitlines())))
        return diff

    def __call__(self, *args, **kwargs):
        if self.mode == self.MODE_ORDERED:
            if len(self.ordered_calls) == 0:
                raise AssertionError('Not expected more calls of %s. The method was called with:\n\targs=%s\n\tkwargs=%s'
                                     % (self.mock_method_name, args, kwargs))

            expected_call = self.ordered_calls.pop(0)
            if expected_call['args'] == args and expected_call['kwargs'] == kwargs:
                return expected_call['result']
            else:
                expected = [expected_call['args'], expected_call['kwargs']]
                found = [args, kwargs]
                diff = self.__prepare_diff(expected, found)
                raise AssertionError('Expected call of %s with:\n\targs=%s\n\tkwargs=%s\nbut found:\n\targs=%s\n\tkwargs=%s\n\n%s\n'
                                     % (self.mock_method_name, expected_call['args'], expected_call['kwargs'], args, kwargs, diff))
        else:
            key = self.__as_key(args, kwargs)
            self.made_calls.append(key)
            try:
                return self.expected_calls[key]
            except KeyError:
                return self.default

class MockUtils:

    MOCK_SPECIALS = ['verify_ordered_', 'expect_', 'verify_', 'default_', 'callcount_', 'ordered_']

    @staticmethod
    def __get_matched_special(item):
        matched = [match for match in MockUtils.MOCK_SPECIALS if item.startswith(match)]
        return None if len(matched) == 0 else matched[0]

    @staticmethod
    def known_special(item: str):
        return MockUtils.__get_matched_special(item) is not None

    @staticmethod
    def split_name(method_name: str):
        matched = MockUtils.__get_matched_special(method_name)
        split_pos = len(matched)-1
        return method_name[:split_pos], method_name[split_pos+1:]

    @staticmethod
    def handle(mock, item):
        utility, method_name = MockUtils.split_name(item)
        return getattr(MockUtils, utility)(mock, getattr(mock, method_name))

    @staticmethod
    def expect(mock, method):
        def expect_closure(*args, **kwargs):
            method.XX_mock_expect(*args, **kwargs)
            return mock
        return expect_closure

    @staticmethod
    def ordered(mock, method):
        def ordered_closure(*args, **kwargs):
            method.XX_mock_ordered(*args, **kwargs)
            return mock
        return ordered_closure

    @staticmethod
    def verify(mock, method):
        def verify_closure(*args, **kwargs):
            return method.XX_mock_verify(*args, **kwargs)
        return verify_closure

    @staticmethod
    def verify_ordered(mock, method):
        def verify_ordered_closure(*args, **kwargs):
            return method.XX_mock_verify_ordered(*args, **kwargs)
        return verify_ordered_closure

    @staticmethod
    def default(mock, method):
        return lambda arg: method.XX_mock_default(arg) or mock

    @staticmethod
    def callcount(mock, method):
        return lambda: len(method.made_calls)

class InvalidStateException(Exception):
    pass

class Mock(object):

    """
     Usage:
      1. for members, use constructor:
       m = Mock(m1 = v1, m2 = v2, m3 = 'someString', m4 = 9)
       m.m1;                           # <- this will be a genuine member.
      2.1. for methods, use either expect...:
       m = Mock()
       m.expect_method(3, toReturn=9)
       m.method(3)                     # <- shall return 9
      2.2. ... or verify:
       m = Mock()
       m.doIt('someString')
       m.verify_doIt()               # <- False
       m.verify_doIt(3)              # <- False
       m.verify_doIt('someString')   # <- True
    """

    def __init__(self, **kwargs):
        for k, v in kwargs.items():
            setattr(self, str(k), v)

    def verify_all_ordered(self):
        #noinspection PyTypeChecker
        # dir returns list of strings, IDEA doesn't know that ;<
        methods = (getattr(self, a) for a in dir(self) if not a.startswith('__'))
        mocked_methods = (m for m in methods if isinstance(m, MockedMethod))
        for m in mocked_methods:
            m.XX_mock_verify_ordered()

    def __getattr__(self, item: str):
        if MockUtils.known_special(item):
            return MockUtils.handle(self, item)
        else:
            try:
                return getattr(super(), item)
            except AttributeError:
                method = MockedMethod(item)
                setattr(self, item, method)
                return method

