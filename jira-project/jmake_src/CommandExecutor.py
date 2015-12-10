from functools import partial
import os
import random
import subprocess
import signal
import time
import sys
import atexit

from Logger import Logger


class Callable:
    success = 0
    failure = 42
    do_not_proceed = 678

    def __init__(self, args):
        super().__init__()
        self.args = args

    def __call__(self, logger):
        return Callable.success


class SystemCallable(Callable):
    def __init__(self, args, command=None, cwd=None):
        super().__init__(args)
        self.__command = command
        self.__cwd = cwd
        self.with_logged_output = os.getenv('JMAKE_TRACE') is not None
        self.run_in_background = False
        self.log_file = None

    def __call__(self, logger):
        command = self.__command
        env = self.env if hasattr(self, 'env') else os.environ

        if not os.getenv('JMAKE_DRY', None) is None:
            command = "echo '%s'" % command
            logger.warn('Call will be suppressed because you have exported JMAKE_DRY env. variable.')

        executable = self.__execute(command, logger, env)
        logger.debug('Executed process pid=%s' % executable.pid)
        prev_signal_handler = signal.signal(signal.SIGINT, partial(self.__signal_handler, executable))

        try:
            self.sig_int = False
            raw_line = b''
            line_no = 0
            while not self.run_in_background:
                try:
                    line = None
                    byte = executable.stdout.read(1)
                    raw_line += byte
                    if byte == b'' or byte == b'\n':
                        line = raw_line.decode('utf-8')
                        if not line and executable.poll() is not None:
                            break
                        raw_line = b''
                    else:
                        if len(raw_line) > 6:
                            partial_line = raw_line.decode('utf-8')
                            if partial_line == 'maven2>' or partial_line == 'maven3>':
                                print(partial_line, end='')
                                sys.stdout.flush()
                                while not byte == b'\n':
                                    byte = executable.stdout.read(1)
                                    assert isinstance(byte, bytes)
                                    if byte:
                                        print(byte.decode('utf-8'), end='')
                                        sys.stdout.flush()
                            else:
                                rest_of_line = executable.stdout.readline()
                                assert isinstance(rest_of_line, bytes)
                                line = partial_line + rest_of_line.decode('utf-8')

                            raw_line = b''
                    if line is not None:
                        if self.process_output(logger, line.rstrip('\n'), line_no):
                            if self.with_logged_output:
                                logger.trace(line)
                            else:
                                print(self.__colorize_line(line, logger), end='')
                        line_no += 1
                except IOError:
                    if self.sig_int:
                        logger.debug('Caught SIGINT terminating jmake')
                    else:
                        logger.warn('The spawned process pid=%s was probably killed' % executable.pid)
                    return Callable.do_not_proceed
            if not self.run_in_background:
                executable.communicate()
            else:
                atexit.register(lambda: logger.info('Killing background process %s' % command) or executable.kill())
        finally:
            signal.signal(signal.SIGINT, prev_signal_handler)
            if not self.run_in_background:
                self.returncode = executable.returncode
            else:
                self.returncode = Callable.success

        return Callable.do_not_proceed if self.sig_int else self.returncode

    def command(self, new_command):
        self.__command = new_command

    def background(self):
        self.run_in_background = True
        return self

    def redirect_output(self, log_file):
        self.log_file = log_file
        return self

    def __signal_handler(self, executable, signal, frame):
        self.sig_int = True
        try:
            executable.kill()
        except OSError:
            pass

    def __execute(self, command, logger, env):
        logger.info(
            'Making OS call{0}: {1}'.format(
                ' (in directory: ' + str(self.__cwd) + ')' if self.__cwd is not None else '',
                command))
        if self.log_file is not None:
            logger.info('Redirected output to file %s' % self.log_file)
            out_file = open(self.log_file, mode='a')
        else:
            out_file = subprocess.PIPE

        return subprocess.Popen(command, stdout=out_file, stderr=subprocess.STDOUT, shell=True, cwd=self.__cwd,
            env=env)


    def process_output(self, logger: Logger, line: str, num: int):
        """
        Override to catch simple output lines and return True to display it on stdout or False to suppress it.
        """
        return True

    def __colorize_line(self, line: str, logger: Logger):
        if 'WARN' in line:
            return logger.color_text(line, Logger.L_WARN)
        if 'ERROR' in line:
            return logger.color_text(line, Logger.L_ERROR)
        if 'BUILD SUCCESS' in line:
            return logger.color_text(line, Logger.L_INFO)
        return line


class CommandExecutor:
    def __init__(self):
        self.commands = []
        self.post_commands = []
        self.perform_console_reset = True

    def append(self, command):
        if command is not None:
            self.commands.append(command)

    def append_post(self, command):
        if command is not None:
            self.post_commands.append(command)

    def set_logger(self, logger):
        self.log = logger
        return self


    def execute(self):
        log = getattr(self, 'log', None)
        if log is None: log = Logger().set_none()

        try:
            return_code = self.__execute_commands(self.commands, log)
            return return_code
        finally:

            self.__execute_commands(self.post_commands, log)
            if self.perform_console_reset:
                print('\x1b[0;0m')


    def __execute_commands(self, commands, log):
        try:
            for callable in commands:
                return_code = callable(log)
                if return_code == Callable.do_not_proceed:
                    return return_code
                if return_code != Callable.success:
                    log.error('Last executed command ' + callable.__class__.__name__ + ' returned ' + str(return_code))
                    return return_code
        except KeyboardInterrupt:
            log.info('\njmake interrupted by keyboard')
            return -1
        except IOError as er:
            log.error('I/O Error %s' % er)
            return -1
        return Callable.success


class ExecutionTimeCallable(Callable):
    def __init__(self):
        super().__init__({})
        self.start_time = time.time()

    def __call__(self, logger):
        self.time = str(round(time.time() - self.start_time, 3))
        msg = 'Finished in ' + self.time + 's'
        if not os.getenv('JMAKE_SKIP_COWS', None): cow_say(msg, logger)
        return Callable.success

COWSAY = './bin/cowsay'
COWSAY_DIR = './bin/cows'
LOLCAT = './bin/gems/bin/lolcat'
COWSAY_CMD = COWSAY + ' -f {0} {1} |' + LOLCAT


def cow_say(msg, logger):
    cow_to_print = COWSAY_DIR + os.sep + random.choice(os.listdir(COWSAY_DIR))
    command = COWSAY_CMD.format(cow_to_print, msg)
    return_code = subprocess.call(command, shell=True, stderr=subprocess.PIPE)
    if return_code != 0:
        logger.info(msg)
