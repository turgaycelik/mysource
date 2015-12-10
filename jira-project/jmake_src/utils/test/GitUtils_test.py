from unittest import TestCase
import unittest
from utils.GitUtils import GitUtils, GitResult, GitExecutor

class MockedGitException(Exception):
    pass

class MockGitExecutor(GitExecutor):
    def __init__(self, expected_command=None, output=None, returncode=0):
        super().__init__()
        self.__expected_calls = []
        self.__expected_call_num = 0
        if expected_command is not None:
            self.expect_call(expected_command, output, returncode)

    def expect_call(self, expected_command, output, returncode = 0):
        self.__expected_calls.append({
            'expected_command': expected_command,
            'output': output,
            'returncode': returncode,
            'was_called': False,
            'was_called_with': None
        })
        return self

    def execute(self, command, exception_on_error: bool=True):
        call_num = self.__expected_call_num
        self.__expected_call_num += 1
        if call_num >= self.__expected_calls.__len__():
            raise AssertionError('Not expected git call with command: %s' % command)

        call = self.__expected_calls[call_num]
        call['was_called'] = True
        call['was_called_with'] = [command]
        if command != call['expected_command']:
            raise AssertionError('Expected to be called with different parameters\n\texpected: %s\n\tfound: %s'
                                 % (call['expected_command'], command))

        if call['returncode'] != 0 and exception_on_error:
            # Throw MockedGitException to be 100% that we're catching this exception in tests
            raise MockedGitException('Return code expected to be 0 but was %s.' % call['returncode'])

        return GitResult(call['returncode'], call['output'])


class TestGitUtils(TestCase):

    def test_get_notes_without_commit(self):
        test_note = 'test note'

        git_executor = MockGitExecutor()\
            .expect_call('notes --ref=test-ref', ['commit1 notes_commit1'])\
            .expect_call('notes --ref=test-ref show notes_commit1', [test_note])

        git_utils = GitUtils(git_executor)
        retrieved_note = git_utils.get_notes('test-ref')

        self.assertEqual(test_note, retrieved_note)

    def test_get_notes_with_commit(self):
        test_note = 'test note'

        git_executor = MockGitExecutor('notes --ref=test-ref show notes_commit1',[test_note])

        git_utils = GitUtils(git_executor)
        retrieved_note = git_utils.get_notes('test-ref', 'notes_commit1')

        self.assertEqual(test_note, retrieved_note)


    def test_get_notes_with_commit_when_git_fails(self):
        git_executor = MockGitExecutor('notes --ref=test-ref show notes_commit1',
            ['error: No note found for object notes_commit1.'], 1)

        git_utils = GitUtils(git_executor)

        self.assertRaisesRegex(MockedGitException, 'Return code expected to be 0 but was 1.',
            git_utils.get_notes,'test-ref', 'notes_commit1')

    def test_generate_annotated_commits_with_details(self):
        git_log_cmd = 'log --show-notes=test-ref -n {0} --pretty=format:"hash,%H,shorthash,%h,commiter,%an <%ae>,date,%at|__NOTE__|%N"'

        git_executor = MockGitExecutor()\
            .expect_call(git_log_cmd.format(100), ['key01,value01,key02,value02|__NOTE__|note01', 'key11,value11,key12,value12|__NOTE__|note02'])

        git_utils = GitUtils(git_executor)
        commits_generator = git_utils.generate_annotated_commits_with_details('test-ref', 100)

        self.assertDictEqual({'note': 'note01', 'key01':'value01', 'key02': 'value02'}, next(commits_generator))
        self.assertDictEqual({'note': 'note02', 'key11':'value11', 'key12': 'value12'}, next(commits_generator))
        self.assertIteratorFinished(commits_generator)

    def test_generate_annotated_commits_with_details_when_there_are_commits_without_notes(self):
        git_log_cmd = 'log --show-notes=test-ref -n {0} --pretty=format:"hash,%H,shorthash,%h,commiter,%an <%ae>,date,%at|__NOTE__|%N"'

        git_executor = MockGitExecutor()\
        .expect_call(git_log_cmd.format(100), ['key01,value01,key02,value02|__NOTE__|note01', '', 'ignore me',
                                               'no,note,here,dude|__NOTE__|', 'key11,value11,key12,value12|__NOTE__|note02'])

        git_utils = GitUtils(git_executor)
        commits_generator = git_utils.generate_annotated_commits_with_details('test-ref', 100)

        self.assertDictEqual({'note': 'note01', 'key01': 'value01', 'key02': 'value02'}, next(commits_generator))
        self.assertDictEqual({'note': 'note02', 'key11': 'value11', 'key12': 'value12'}, next(commits_generator))
        self.assertIteratorFinished(commits_generator)

    def test_get_commit_details_for_latest_commit(self):
        git_executor = MockGitExecutor('''log --show-notes=test-ref -n 1 --pretty=format:"hash,%H,shorthash,%h,commiter,%an <%ae>,date,%at|__NOTE__|%N" ''',
            ['hash,1111,shorthash,2222,commiter,Unit Test <unit@test.none>,date,1371047821|__NOTE__|test note'])
        git_utils = GitUtils(git_executor)
        commit_details = git_utils.get_commit_details('test-ref')
        expected_details = {'date': '1371047821', 'note': 'test note', 'commiter': 'Unit Test <unit@test.none>', 'shorthash': '2222', 'hash': '1111'};
        self.assertDictEqual(expected_details, commit_details)

    def test_get_commit_details(self):
        git_executor = MockGitExecutor('''log --show-notes=test-ref -n 1 --pretty=format:"hash,%H,shorthash,%h,commiter,%an <%ae>,date,%at|__NOTE__|%N" 1111''',
            ['hash,1111,shorthash,2222,commiter,Unit Test <unit@test.none>,date,1371047821|__NOTE__|test note'])
        git_utils = GitUtils(git_executor)
        commit_details = git_utils.get_commit_details('test-ref', '1111')
        expected_details = {'date': '1371047821', 'note': 'test note', 'commiter': 'Unit Test <unit@test.none>', 'shorthash': '2222', 'hash': '1111'};
        self.assertDictEqual(expected_details, commit_details)

    def test_get_latest_annotated_commit(self):
        git_log_cmd = 'log --show-notes=test-ref -n {0} --pretty=format:"hash,%H,shorthash,%h,commiter,%an <%ae>,date,%at|__NOTE__|%N"'

        git_executor = MockGitExecutor() \
            .expect_call(git_log_cmd.format(30000), ['', 'key01,value01,key02,value02|__NOTE__|note01', 'ignore me',
                                                   'no,note,here,dude|__NOTE__|', 'key11,value11,key12,value12|__NOTE__|note02'])

        git_utils = GitUtils(git_executor)
        commit = git_utils.latest_annotated_commit_with_details('test-ref')

        self.assertDictEqual({'note': 'note01', 'key01':'value01', 'key02': 'value02'}, commit)

    def test_get_config_by_key(self):
        git_log_cmd = 'config --get {}'

        key = 'just.a.key'
        git_executor = MockGitExecutor() \
            .expect_call(git_log_cmd.format(key), ['a value'])

        git_utils = GitUtils(git_executor)
        data = git_utils.get_config_by_key(key)

        self.assertEqual('a value', data)

    def assertIteratorFinished(self, it):
        iterator_finished_marker = object()
        self.assertEqual(iterator_finished_marker, next(it, iterator_finished_marker), "Iterator has more elements!")

if __name__ == '__main__':
    unittest.main(verbosity=2)

