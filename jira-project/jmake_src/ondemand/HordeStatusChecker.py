from urllib.error import URLError
import urllib.request
import time

from CommandExecutor import Callable
from ondemand.HordeLayout import HordeLayout


class HordeStatusChecker:
    def __init__(self, horde_layout: HordeLayout):
        self.horde_layout = horde_layout

    def is_running(self):
        try:
            urllib.request.urlopen(self.horde_layout.horde_status_url())
            return True
        except URLError:
            return False


class HordeStatusEnforcer(Callable):
    def __init__(self, args, max_tries=25, timeout=5):
        super().__init__(args)
        self.horde_layout = args.horde_layout
        self.horde_status_checker = HordeStatusChecker(self.horde_layout)
        self.max_tries = max_tries
        self.timeout = timeout

    def __call__(self, logger):
        tries = 1
        while not self.horde_status_checker.is_running():
            if tries >= self.max_tries:
                logger.error(
                    'Horde status at %s returned error - stopping build' % self.horde_layout.horde_status_url())
                return Callable.do_not_proceed
            logger.info('Horde is not ready: Attempt %d of %d failed. Retrying in %d seconds. Check the logs at %s' % (
            tries, self.max_tries, self.timeout, self.horde_layout.horde_log_file()))
            tries += 1
            time.sleep(self.timeout)

        return Callable.success
