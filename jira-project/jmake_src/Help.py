import argparse
from argparse import _SubParsersAction
from module import JmakeModule


class Help(JmakeModule):
    def __init__(self, parser):
        super().__init__()
        self.command = 'help'
        self.description = 'Prints the main help'
        self.parser = parser
        self.prevent_post_commands = True
        self.check_branch = False

    def __call__(self, args, executor):
        if args.full:
            for title, subparser in self.__subparser_walker('jmake', self.parser):
                print(self.__decorate_title(args, title))
                print(self.__decorate_section(args, self.__print_full_help(args, subparser)))
        else:
            executor.append(lambda logger: self.parser.print_help() or 0)

    def __print_full_help(self, args, parser: argparse.ArgumentParser):
        return parser.format_help()

    def __subparser_walker(self, command: str, parser: argparse.ArgumentParser):
        for action in parser._get_positional_actions():
            if isinstance(action, _SubParsersAction):
                for (keyword, subparser) in action.choices.items():
                    subcommand = command + ' ' + keyword
                    yield subcommand, subparser
                    for s, p in self.__subparser_walker(subcommand, subparser):
                        yield s, p

    def define_parser(self, parser: argparse.ArgumentParser):
        parser.add_argument('--full', action='store_true', help='Prints full help for all options.')
        parser.add_argument('--html', action='store_true', help='When full help is printed, use html decorations.')

    def __decorate_title(self, args, title: str):
        if args.html:
            return self.__html(self.__html(title, 'b'), 'h2')
        else:
            return ' => %s \n' % title

    def __decorate_section(self, args, section: str):
        if args.html:
            return self.__html(section, 'pre')
        else:
            return section

    def __html(self, text, tag_name):
        return '<{1}>{0}</{1}>'.format(text, tag_name)