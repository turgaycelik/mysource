from unittest import TestCase
from eh.metrics.GrepCount import GrepCount, GrepCountWithActivator
from utils.Mocks import Mock

ANY_FILE_FILTER = lambda x: True

#noinspection PyTypeChecker
class TestGrepCount(TestCase):

    def test_grep_file_filter(self):
        grep = GrepCount('test', lambda x: x.startswith('want'), grep_str='', metrics_logger=Mock()).configure(Mock(), False)
        self.assertTrue(grep.wants_file('want-a-lot'))
        self.assertTrue(grep.wants_file('want!!!'))
        self.assertTrue(grep.wants_file('wants'))
        self.assertFalse(grep.wants_file('do-not-want'))
        self.assertFalse(grep.wants_file('how-about-no'))

    def test_matches_with_string(self):

        grep = GrepCount(None, ANY_FILE_FILTER, None, 'ism', metrics_logger=Mock())
        grep.pre_files_scan('test-module')
        grep.wants_file('test.log')
        self.assertTrue(grep.on_read_line('buddhism'))
        self.assertTrue(grep.on_read_line('ismypizzaready?'))
        self.assertTrue(grep.on_read_line('kissme'))
        self.assertTrue(grep.on_read_line('mismatch'))
        self.assertEqual(3, grep.value, 'should have 3 hits')

    def test_stop_condition(self):

        grep = GrepCount(None, ANY_FILE_FILTER, None, 'cat', stop_object='lolcat', metrics_logger=Mock())
        grep.pre_files_scan('test-module')
        grep.wants_file('test.log')
        self.assertTrue(grep.on_read_line('catalogue'))
        self.assertFalse(grep.on_read_line('lolcat'))
        self.assertEqual(2, grep.value, 'should have 2 hits')

    def test_grep_with_regex_not_matches(self):
        #having
        grep_count_dates = GrepCount(None, ANY_FILE_FILTER, None, '1[1-9]{3]', use_regex=True, metrics_logger=Mock())
        grep_count_dates.pre_files_scan('test-module')
        grep_count_dates.wants_file('test.log')

        #when
        should_continue = grep_count_dates.on_read_line('2345')
        self.assertTrue(should_continue)
        self.assertEqual(grep_count_dates.value, 0, 'should not have any hits ')

    def test_grep_with_regex_matches(self):
        #having
        grep_count_dates = GrepCount(None, ANY_FILE_FILTER, None, '1[1-9]{3}', use_regex=True, metrics_logger=Mock())
        grep_count_dates.pre_files_scan('test-module')
        grep_count_dates.wants_file('test.log')

        #when
        self.assertTrue(grep_count_dates.on_read_line('sad 1345 sad '))
        #then
        self.assertEqual(grep_count_dates.value, 1, 'should have one hit ')

    def test_grep_regex_matches_with_list_of_regex_expressions(self):
        #having
        grep_count_dates = GrepCount(None, ANY_FILE_FILTER, None, ['1[1-9]{3}', '2+'], use_regex=True, metrics_logger=Mock())
        grep_count_dates.pre_files_scan('test-module')
        grep_count_dates.wants_file('test.log')

        #when
        self.assertTrue(grep_count_dates.on_read_line('sad 1345 sad '))
        self.assertTrue(grep_count_dates.on_read_line('sad 2345 sad '))
        self.assertTrue(grep_count_dates.on_read_line('sad 12345 sad '))

        #then
        self.assertEqual(grep_count_dates.value, 3, 'should have three hits')

    def test_grep_matches_with_list_of_strings(self):
        grep_count_dates = GrepCount(None, ANY_FILE_FILTER, None, ['13', '2'], metrics_logger=Mock())
        grep_count_dates.pre_files_scan('test-module')
        grep_count_dates.wants_file('test.log')

        #when
        self.assertTrue(grep_count_dates.on_read_line('sad 1345 sad '))
        self.assertTrue(grep_count_dates.on_read_line('sad 434 sad '))
        self.assertTrue(grep_count_dates.on_read_line('sad 12345 sad '))

        #then
        self.assertEqual(grep_count_dates.value, 2, 'should have two hits')

    def test_grep_matches_with_function(self):
        grep_count_dates = GrepCount(None, ANY_FILE_FILTER, None, lambda line: '45' in line, metrics_logger=Mock())
        grep_count_dates.pre_files_scan('test-module')
        grep_count_dates.wants_file('test.log')

        #when
        self.assertTrue(grep_count_dates.on_read_line('sad 1345 sad '))
        self.assertTrue(grep_count_dates.on_read_line('sad 43445 sad '))
        self.assertTrue(grep_count_dates.on_read_line('sad 12335 sad '))

        #then
        self.assertEqual(grep_count_dates.value, 2, 'should have two hits')


#noinspection PyTypeChecker
class TestGrepCountWithActivator(TestCase):
    def test_count_works_only_in_specified_scope(self):
        #having
        grep_count = GrepCountWithActivator(None, ANY_FILE_FILTER, None, '1', start_count='<context>', stop_count='</context>', metrics_logger=Mock())
        grep_count.pre_files_scan('test-module')
        grep_count.wants_file('test.log')

        #when
        self.assertTrue(grep_count.on_read_line('sad 1 sad '))
        self.assertTrue(grep_count.on_read_line('sad 1 sad <context>'))
        self.assertTrue(grep_count.on_read_line('sad 1 sad '))
        self.assertTrue(grep_count.on_read_line('sad 2 sad '))
        self.assertTrue(grep_count.on_read_line('sad 1 sad <context> sad 2 sad '))
        self.assertTrue(grep_count.on_read_line('sad 1 sad </context> sad 1 sad'))
        #then
        self.assertEqual(grep_count.value, 3, 'should have two hits')

