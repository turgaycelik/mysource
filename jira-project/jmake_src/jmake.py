#!/usr/bin/env python3
import sys
from CommandDispatcher import CommandDispatcher
from CommandExecutor import CommandExecutor
from Logger import LOG


dispatcher = CommandDispatcher(CommandExecutor().set_logger(LOG.set_debug()))
sys.exit(dispatcher.dispatch_from_params(sys.argv))






