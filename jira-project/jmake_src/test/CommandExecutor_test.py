from unittest import TestCase
import unittest
from CommandExecutor import CommandExecutor, Callable
from Logger import Logger


class CommandExecutorTest(TestCase):
    def void(self, ret_code): return lambda logger: ret_code

    def push(self, i, ret=0): return lambda logger: self.execs.append(i) or ret


    def setUp(self):
        self.executor = CommandExecutor().set_logger(Logger().set_none())
        self.executor.perform_console_reset = False
        self.execs = []


    def test_empty_executables(self):
        self.executor.execute()

    def test_executables_should_be_executed_in_order(self):
        self.executor.append(self.push(1))
        self.executor.append(self.push(2))
        self.executor.append(self.push(3))
        self.executor.append(self.push(4))
        self.executor.append(self.push(5))
        execution_ret = self.executor.execute()
        self.assertEqual(Callable.success, execution_ret)
        self.assertListEqual([1, 2, 3, 4, 5], self.execs)

    def post_execution_test_with_return_code(self, ret_code):
        self.executor.append(self.void(Callable.success))
        self.executor.append(self.push(1, ret_code))
        self.executor.append_post(self.push(7, Callable.success))
        execution_ret = self.executor.execute()
        self.assertEqual(ret_code, execution_ret)
        self.assertListEqual([1, 7], self.execs)

    def test_post_execution_should_happen_when_zero_return_code(self):
        self.post_execution_test_with_return_code(Callable.success)

    def test_post_execution_should_happen_when_non_zero_return_code(self):
        self.post_execution_test_with_return_code(1)

    def test_post_execution_should_happen_always_do_not_proceed_return_code(self):
        self.post_execution_test_with_return_code(Callable.do_not_proceed)

    def test_execution_should_stop_on_error(self):
        self.executor.append(self.push(1, Callable.success))
        self.executor.append(self.push(2, 1))
        self.executor.append(self.push(3, Callable.success))
        self.executor.append(self.push(4, 1))
        self.executor.append(self.push(5, Callable.success))
        self.executor.append_post(self.push(6, Callable.success))
        self.executor.append_post(self.push(8, Callable.success))
        self.executor.append_post(self.push(10, 1))
        self.executor.append_post(self.push(12, Callable.success))

        execution_ret = self.executor.execute()
        self.assertEqual(1, execution_ret)
        self.assertListEqual([1, 2, 6, 8, 10], self.execs)

    def test_execution_should_stop_on_do_not_proceed(self):
        self.executor.append_post(self.push(4, Callable.success))
        self.executor.append_post(self.push(5, Callable.do_not_proceed))
        self.executor.append_post(self.push(6, Callable.success))
        self.executor.append(self.push(1, Callable.success))
        self.executor.append(self.push(2, Callable.do_not_proceed))
        self.executor.append(self.push(3, Callable.success))

        execution_ret = self.executor.execute()
        self.assertEqual(Callable.do_not_proceed, execution_ret)
        self.assertListEqual([1, 2, 4, 5], self.execs)

if __name__ == '__main__':
    unittest.main(verbosity=2)