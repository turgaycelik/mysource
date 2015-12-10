from argparse import _SubParsersAction
import textwrap

def subparser_choices(parser):
    result = {}

    for action in parser._actions:
        if isinstance(action, _SubParsersAction):
            for (choice, target) in action.choices.items(): result[choice] = target

    return result

def wrap_text(text):
    return '\n'.join(textwrap.wrap(text, 90))