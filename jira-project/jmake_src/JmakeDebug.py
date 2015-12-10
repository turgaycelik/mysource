from Diagnostics import LocalPortInspector
from JmakeRun import Run


DEFAULT_DEBUG_PORT = 5005

class Debug(Run):
    def __init__(self):
        super().__init__()
        self.command = 'debug'
        self.description = 'Runs JIRA using your code. This will download a tomcat for you, configure it and '\
                           'start JIRA on your local machine. To debug it, use the default "Remote" configuration '\
                           'from your IDEA (port 5005). Once connected this will hot-swap code changes into your '\
                           'running JIRA (for core, not for plugins).'

    def define_parser(self, parser):
        super().define_parser(parser)
        parser.add_argument('-dp', '--debug-port', default=DEFAULT_DEBUG_PORT,
                            help='Debugger port. Default is %s. Set to "auto" to scan for a free port.' % DEFAULT_DEBUG_PORT,
                            dest='debug_port')
        parser.add_argument('-susp', '--suspend', action="store_true",
                            help='Suspend the JVM on startup', dest='suspend')

    def debug(self):
        return True

    def check_ports(self, args, executor):
        super().check_ports(args, executor)

        if args.debug_port == "auto":
            def set_debug_port(port):
                args.sh_port = port
            executor.append(LocalPortInspector(DEFAULT_DEBUG_PORT, port_name='debug port').probe(set_debug_port))
        else:
            executor.append(LocalPortInspector(args.debug_port, port_name='debug port'))
