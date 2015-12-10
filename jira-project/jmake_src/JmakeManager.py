from maven.Maven import MavenCallable
from module import JmakeModule


class BogusModule(JmakeModule):
    def __call__(self, args, executor):
        executor.append(lambda logger: logger.info('Just messing with your heads a bit...') or 0)


class Excel(BogusModule):
    def __init__(self):
        super().__init__()
        self.command = 'excel'
        self.description = 'Installs MS Excel on Windows, Unix or Mac. Yes, we finally cracked it. '
        self.check_branch = False

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)
        parser.add_argument('-pc', '--pretty-charts', action='store_true',
            help='installs those pretty charts your boss likes.')


class ConfRoomBooking(BogusModule):
    def __init__(self):
        super().__init__()
        self.command = 'conf-room'
        self.description = 'Books conf room, installs reminder. '
        self.check_branch = False

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)

        parser.add_argument('room', help='the room to book.')
        parser.add_argument('from', help='start time.')
        parser.add_argument('till', help='finish time. obviously it will take longer so this will add extra 20 minutes')
        parser.add_argument('-f', '--force', action='store_true',
            help='cancels existing appointments in the conf room selected.')


class Presentation(BogusModule):
    def __init__(self):
        super().__init__()
        self.command = 'presentation'
        self.description = 'Prepares a presentation from a template.'
        self.check_branch = False

    def define_parser(self, parser):
        MavenCallable.add_maven_switches(parser)
        parser.add_argument('template', help='template you need to pick.', choices=['great-success', 'morale-booster',
                                                                                    'five-years-from-now-plan',
                                                                                    'health-and-safety'])


class Manager(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'manager'
        self.description = 'Helps in other tasks that are not worthy of engineers. '

    def get_submodules(self):
        return [Excel(), ConfRoomBooking(), Presentation()]







