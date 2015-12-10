from argparse import ArgumentParser
from unittest import TestCase
from JmakeAutoComplete import AutoComplete
from utils.Mocks import Mock

class AutoCompleteTest(TestCase):
    def test_simple_parser(self):
        parser = ArgumentParser()
        self.do_test(parser, ['-'], ['-h', '--help'], ['clean'])

    def test_choices_mutually_exclusive(self):
        parser = ArgumentParser()
        parser.add_argument('myFirstChoice', choices=['a', 'b', 'c', 'd', 'e'])
        parser.add_argument('mySecondChoice', choices=['q', 'w', 'e', 'r', 't'])
        self.do_test(parser, [], ['a', 'b', 'c', 'd', 'e', 'q', 'w', 'r', 't'], [])
        self.do_test(parser, ['a'], ['q', 'w', 'e', 'r', 't'], ['a', 'b', 'c', 'd'])
        self.do_test(parser, ['a', 'r'], [], ['a', 'b', 'c', 'd', 'e', 'q', 'w', 'r', 't'])

    def test_partial_completion(self):
        parser = ArgumentParser()

        parser.add_argument('--abcd')
        parser.add_argument('--abef')
        parser.add_argument('--bder')
        parser.add_argument('--abbd')
        parser.add_argument('--acce')

        self.do_test(parser, ['--'], ['--abcd', '--abef', '--bder', '--abbd', '--acce'], [])
        self.do_test(parser, ['--a'], ['--abcd', '--abef', '--abbd', '--acce'], ['--bder'])
        self.do_test(parser, ['--ab'], ['--abcd', '--abef', '--abbd'], ['--bder', '--acce'])
        self.do_test(parser, ['--abcd'], ['--bder', '--acce', '--abef', '--abbd'], [])
        self.do_test(parser, ['--abcd', 'e'], [], ['--bder', '--acce', '--abef', '--abbd'])

    def test_sub_parsers(self):
        parser = ArgumentParser()

        parser.add_argument('--main_level')
        subparser = parser.add_subparsers().add_parser('sub')
        subparser.add_argument('--sub_level')
        subparser = subparser.add_subparsers().add_parser('sub2')
        subparser.add_argument('--sub_level2')

        self.do_test(parser, [], ['--main_level', 'sub'], ['sub2', '--sub_level', '--sub_level2'])
        self.do_test(parser, ['sub'], ['--sub_level', 'sub2'], ['--sub_level2', '--main_level'])
        self.do_test(parser, ['sub', 'sub2'], ['--sub_level2'], ['--sub_level', '--main_level'])

    def test_sub_parsers_are_mutually_exclusive(self):
        parser = ArgumentParser()

        parser.add_argument('-f', '--force')
        subparsers = parser.add_subparsers()
        subparsers.add_parser('save')
        subparsers.add_parser('delete')
        subparsers.add_parser('copy')
        subparsers.add_parser('move')
        subparsers.add_parser('touch')

        self.do_test(parser, [], ['save', 'delete', 'copy', 'move', 'touch', '-f', '--force'], [])
        self.do_test(parser, ['-f'], ['save', 'delete', 'copy', 'move', 'touch'], ['-f', '--force'])
        self.do_test(parser, ['save'], [], ['save', 'delete', 'copy', 'move', 'touch', '-f', '--force'])

    def test_clean_is_considered_as_an_option_always(self):
        parser = ArgumentParser()

        parser.add_argument('--main_level')
        subparser = parser.add_subparsers().add_parser('sub')
        subparser.add_subparsers().add_parser('sub2')

        self.do_test(parser, [], ['clean'], [])
        self.do_test(parser, ['sub'], ['clean'], [])
        self.do_test(parser, ['sub', 'sub2'], ['clean'], [])
        self.do_test(parser, ['sub', 'sub2', 'clean'], [], ['clean'])

    def do_test(self, parser, input, wanted, unwanted):
        module = AutoComplete(parser)
        args = Mock(input = ['jmake'])
        args.input.extend(input)
        #noinspection PyUnresolvedReferences
        res = [x for x in module._AutoComplete__determine_autocomplete(args, None)]
        for item in res:
            self.assertNotIn(item, unwanted)
        for item in wanted:
            self.assertIn(item, res)
        pass
