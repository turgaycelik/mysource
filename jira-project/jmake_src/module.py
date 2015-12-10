from argparse import ArgumentParser
import argparse
import getpass

class JmakeModule:
    def __init__(self):
        super().__init__()
        # you may set this flag to true if your module does not need finalization tasks executed (time,cows etc)
        self.prevent_post_commands = False
        # you may set this flag to prevent console reset (when output matters, like auto-complete).
        self.prevent_console_reset = False
        # you may unset this flag to disable branch checking.
        self.check_branch = True
        # you may unset this flag to prevent feature discovery in modules.
        self.prevent_post_diagnostics = False

        self.command = 'define-me'
        self.description = 'no description'
        self.sample_usage_text = ''


    def get_submodules(self):
        return None

    def define_parser(self, parser: ArgumentParser):
        pass

    def is_manager(self):
        return getpass.getuser() in ['jpendleton', 'nmenere', 'justus', 'wseliga', 'elmadir']

    def sample_usage(self, parser: ArgumentParser, intent: str, commands: list):
        parser.formatter_class = argparse.RawDescriptionHelpFormatter
        if not self.sample_usage_text:
            self.epilog_text = parser.epilog
            self.sample_usage_text = 'sample usages:\n\n'

        self.sample_usage_text += intent + '\n  '
        self.sample_usage_text += '\n  '.join(commands) + '\n\n'

        parser.epilog = self.sample_usage_text + self.epilog_text

    def append_format_usage(self):
        """
         Override to add text to usage information when parameters are incorrectly supplied.
        """
        return None
