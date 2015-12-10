import re
from unittest import TestCase
import unittest
from utils.Mocks import Mock, InvalidStateException

class XmlUtilsTest(TestCase):

    def test_members(self):

        m = Mock(member1 = 10, member2 = 'abc')
        m.another_member = []
        setattr(m, 'set_member', True)

        self.assertEqual(m.member1, 10)
        self.assertEqual(m.member2, 'abc')
        self.assertListEqual(m.another_member, [])
        self.assertEqual(m.set_member, True)

    def test_expected(self):

        m = Mock()
        m.expect_do_it(toReturn=0)
        self.assertEqual(0, m.do_it())

        m.expect_do_it(2, toReturn=4)
        self.assertEqual(4, m.do_it(2))

        m.expect_do_it(param=3, toReturn=9)
        self.assertIsNone(m.do_it(3))
        self.assertEqual(9, m.do_it(param=3))

        m.expect_do_it(4, withAnother='32', toReturn=16).expect_chained(toReturn='sno-flakes!')
        self.assertIsNone(m.do_it(4))
        self.assertIsNone(m.do_it(withAnother=32))
        self.assertEqual(16, m.do_it(4, withAnother='32'))
        self.assertEqual(0, m.do_it())

    def test_default(self):

        m = Mock().default_is_prime(False)
        m.expect_primes(toReturn=[2, 3, 5, 7, 11, 13, 17, 19])
        for num in m.primes():
            m.expect_is_prime(num, toReturn=True)

        for num in range(20):
            self.assertEqual(m.is_prime(num), num in m.primes())

    def test_verify(self):

        m = Mock()
        m.do_stuff()
        m.do_more_stuff()
        m.do_more_stuff(3)
        m.do_more_stuff('with a friend')

        self.assertTrue(m.verify_do_stuff())
        self.assertTrue(m.verify_do_more_stuff())
        self.assertTrue(m.verify_do_more_stuff(3))
        self.assertTrue(m.verify_do_more_stuff('with a friend'))

        self.assertFalse(m.verify_do_stuff(None))
        self.assertFalse(m.verify_do_more_stuff(0))
        self.assertFalse(m.verify_do_more_stuff(''))
        self.assertFalse(m.verify_do_more_stuff([]))

    def test_callcount(self):

        m = Mock()
        m.do_stuff()
        m.do_stuff('abcd')

        m.do_more_stuff()
        m.do_more_stuff(3)
        m.do_more_stuff('with a friend')

        self.assertEqual(m.callcount_do_stuff(), 2)
        self.assertEqual(m.callcount_do_more_stuff(), 3)

    def test_not_override_members(self):

        m = Mock()
        m.do_stuff = 2
        try:
            m.expect_do_stuff(toReturn=2)
            m.do_stuff(2)
        except AttributeError:
            self.assertEqual(m.do_stuff, 2)
            pass
        else:
            self.fail('field was replaced my a method, which is wrong!')
        self.assertEqual(m.do_stuff, 2)

    def test_subclassing(self):

        class Submock(Mock):
            def do_this(self):
                return 1000

        s = Submock()
        s.expect_ten(toReturn=10)
        self.assertEqual(s.do_this(), 1000)
        self.assertEqual(s.ten(), 10)

    def test_mock_ordered(self):
        m = Mock()
        m.ordered_exec('first', toReturn='first result')
        m.ordered_exec('second', toReturn='second result')

        self.assertEqual('first result', m.exec('first'))
        self.assertEqual('second result', m.exec('second'))

    def test_mock_ordered_when_expect_used_before(self):
        m = Mock()
        m.expect_exec('first', toReturn='first result')
        try:
            m.ordered_exec('second', toReturn='second result')
        except InvalidStateException as e:
            message = e.args[0]
            self.assertEqual('Cannot use ordered method with non-ordered!', message)

    def test_mock_expect_when_ordered_used_before(self):
        m = Mock()
        m.ordered_exec('first', toReturn='first result')
        try:
            m.expect_exec('second', toReturn='second result')
        except InvalidStateException as e:
            message = e.args[0]
            self.assertEqual('Cannot use ordered method with non-ordered!', message)

    def test_mock_ordered_with_wrong_order(self):
        m = Mock()
        m.ordered_exec('first', toReturn='first result')
        m.ordered_exec('second', toReturn='second result')

        try:
            m.exec('second')
        except AssertionError as e:
            message = e.args[0]
            self.assertRegex(message, re.compile('''Expected call of exec with:\n\targs=\\('first',\\)\n\tkwargs={}\n'''
                                                 +'''but found:\n\targs=\\('second',\\)\n\tkwargs={}\n.*'''))

    def test_mock_ordered_called_too_many_times(self):
        m = Mock()
        m.ordered_exec('first', toReturn='first result')

        self.assertEqual('first result', m.exec('first'))
        try:
            m.exec('should not be called')
        except AssertionError as e:
            message = e.args[0]
            self.assertEqual('Not expected more calls of exec. The method was called with:\n'
            +'''\targs=('should not be called',)\n\tkwargs={}''', message)

if __name__ == '__main__':
    unittest.main(verbosity=2)



