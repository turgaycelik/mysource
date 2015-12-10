import os
from unittest import TestCase
import unittest
from Logger import Logger
from CommandExecutor import Callable, SystemCallable
from utils.Mocks import Mock

class CallableTest(TestCase):
    def test_callable_stores_args(self):
        args = {'yes': 'it', 'does': 'store them'}
        callable = Callable(args)
        self.assertDictEqual(args, callable.args)


class MockedSubProcess(Mock):
    def __init__(self, trace, returncode):
        super().__init__(pid = 1234,
            trace = trace,
            trace_ptr = 0,
            trace_line_ptr = 0,
            stdout = self,
            returncode = returncode)

    def read(self, numbytes : int):
        if not numbytes == 1:
            raise ValueError('Cannot use this mock with reading differently than 1 byte at a time')
        if self.trace_ptr == len(self.trace):
            return b''
        if self.trace_line_ptr == len(self.trace[self.trace_ptr]):
            self.trace_line_ptr = 0
            self.trace_ptr += 1
            return b'\n'

        text = self.trace[self.trace_ptr][self.trace_line_ptr:self.trace_line_ptr + numbytes]
        self.trace_line_ptr += numbytes
        return bytes(text, encoding='utf-8')

    def readline(self):
        if self.trace_ptr == len(self.trace):
            return b''
        line = self.trace[self.trace_ptr][self.trace_line_ptr:] + '\n'
        self.trace_ptr += 1
        self.trace_line_ptr = 0
        return bytes(line, encoding='utf-8')

    def poll(self):
        return None if self.trace_ptr < len(self.trace) else self.trace_ptr


class MockedSystemCallable(SystemCallable):
    def __init__(self, command, subprocess):
        super().__init__({}, command)
        self.subprocess = subprocess
        self.output_notifications = []

    def _SystemCallable__execute(self, command, logger, env=os.environ):
        self.called_with_env = env
        self.called_command = command
        return self.subprocess

    def is_killed(self):
        return self.subprocess.verify_kill()

    def process_output(self, logger: Logger, line : str, num : int):
        self.output_notifications.append(line)


class SystemCallableTest(TestCase):

    def test_catching_callable_line(self):
        trace = ['line1', 'line2', 'line3 which is very long to test maven:cli hackery', '4', 'end']
        subprocess = MockedSubProcess(trace, 0)
        call = MockedSystemCallable('some', subprocess)

        rc = call(Mock())

        self.assertListEqual(trace, call.output_notifications)

    def test_calling_command_forwards_system_return_code(self):
        command = 'my command'
        call = MockedSystemCallable(command, MockedSubProcess([], 6667))
        rc = call(Mock())

        self.assertEqual(rc, 6667)
        self.assertEqual(command, call.called_command)

    def test_using_env_member(self):
        env = os.environ.copy()
        env.clear()
        env['MY_ENV'] = 987
        call = MockedSystemCallable('some', MockedSubProcess([], 0))
        call.env = env

        call(Mock())
        self.assertDictEqual(call.called_with_env, env)

    def test_catching_sigint_causes_killing_spawned_process_and_halts_jmake(self):
        subprocess = MockedSubProcess(['some output', 'some more output'], 0)
        call = MockedSystemCallable('some', subprocess)

        def read(num_bytes):
            if not subprocess.verify_kill():
                #noinspection PyUnresolvedReferences
                call._SystemCallable__signal_handler(subprocess, None, None)
            raise IOError

        subprocess.read = read

        rc = call(Mock())
        self.assertEqual(True, call.is_killed())
        self.assertEqual(Callable.do_not_proceed, rc)

    def test_dry_runs(self):
        key = 'JMAKE_DRY'
        command = 'ls / | just_testing -rf ;something else'
        call = MockedSystemCallable(command, MockedSubProcess([], 0))
        try:
            os.environ[key] = '1'
            call(Mock())
        finally:
            os.unsetenv(key)

        self.assertEqual(call.called_command, "echo '%s'" % command)

    def test_that_returncode_field_exists(self):

        call = MockedSystemCallable('a-command', MockedSubProcess([], 1234))
        call(Mock())
        self.assertTrue(hasattr(call, 'returncode'))
        self.assertEqual(call.returncode, 1234)

if __name__ == '__main__':
    unittest.main(verbosity=2)