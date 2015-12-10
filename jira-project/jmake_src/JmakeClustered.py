from argparse import ArgumentParser
import getpass
import hashlib
from json import JSONEncoder, JSONDecoder
import os
import re
import socket
import subprocess
import uuid
import time
import json
from CommandExecutor import Callable
from Diagnostics import LocalPortInspector, AllPortsChecker
from JmakeDebug import Debug, DEFAULT_DEBUG_PORT
from JmakeRun import Run, DEFAULT_HTTP_PORT, DEFAULT_SHUTDOWN_PORT
from Logger import Logger
from catalina.WorkspaceLayout import WorkspaceLayout
from module import JmakeModule
from urllib.request import HTTPBasicAuthHandler, Request, urlopen
from utils.FileUtils import FileUtils
from utils.XmlUtils import XmlUtils

USAGE = 'In order to run JIRA in clustered mode, follow the guide:'

USAGE_COMMANDS = [
    '# requires postgres installed:',
    'sudo ./jmake postgres init',
    './jmake cleanall',
    './jmake run --postgres',
    '# complete installation steps and kill the instance',
    './jmake clustered monitor',
    '# then run as many instances as you want in additional shells:',
    './jmake clustered run quickstart --postgres',
    './jmake clustered debug quickstart --postgres',
    '# You may replace postgres with mysql in the instructions above.'
]

DEFAULT_CACHE_PORT = 40001


class ClusterContext(Callable):

    CLUSTERED_INFO_DIR = os.sep.join(['.', 'target', 'clustered-info'])

    @staticmethod
    def generate_cluster_name():
        cluster_name = '@'.join([getpass.getuser(), socket.gethostname()])
        return cluster_name

    def __init__(self, args, fs: FileUtils= FileUtils()):
        super().__init__(args)
        self.fs = fs
        self.cluster_name = args.cluster_name if args.cluster_name is not None else ClusterContext.generate_cluster_name()
        cluster_name_hash = hashlib.sha224(self.cluster_name.encode('utf-8')).hexdigest()
        self.cluster_hash = cluster_name_hash[:6]
        self.multicast_port = int(self.cluster_hash[:8], 16) % 30000 + 10000
        self.instance_hash = args.instance_hash if args.instance_hash is not None else hashlib.sha224(str(uuid.uuid4()).encode('utf-8')).hexdigest()[:6]
        self.peer_discovery = args.peer_discovery if args.peer_discovery is not None else 'default'
        self.clustered_info_dir = self.fs.existing_dir(self.CLUSTERED_INFO_DIR)
        cluster_info_file = 'instance-' + self.instance_hash
        self.info_file = os.sep.join([self.clustered_info_dir, cluster_info_file])

        args.instance_name = self.instance_hash
        args.clustered = True
        if not (args.postgres or args.mysql):
            # choose postgres as the original blogpost says so...
            args.postgres = True
        args.clustered_instance_name = str(self.cluster_hash)

    def configure_jira_home(self, args):
        self.jira_home = self.fs.existing_dir(os.sep.join([WorkspaceLayout.JIRA_CLUSTERED_HOME_ROOT, self.instance_hash]))
        self.shared_jira_home = self.fs.existing_dir(os.sep.join([WorkspaceLayout.JIRA_SHARED_HOME_ROOT, self.cluster_hash]))
        args.layout.jira_home_dir = self.jira_home

    def instance_info_writer(self):
        def instance_info_writer_closure(log):

            instance_info = {
                'start-time': str(int(time.time())),
                'http-port': self.args.port,
                'shutdown-port': self.args.sh_port,
                'cluster-hash': self.cluster_hash,
                'instance-hash': self.instance_hash,
                'cluster-peer-cache-port': self.args.cache_port,
                'cluster-name': self.cluster_name,
                'cluster-multicast-port': self.multicast_port,
                'jira-home': self.jira_home,
                'shared-home': self.shared_jira_home,
                'peer-discovery': self.peer_discovery,
                'url': '%s://localhost:%s%s' % ('https' if self.args.ssl else 'http', str(self.args.port), self.args.jira_context)
            }

            if 'debug_port' in self.args:
                instance_info['debug-port'] = self.args.debug_port

            with open(self.info_file, 'w') as f:
                log.debug('Writing cluster info file: ' + self.info_file)
                f.write(JSONEncoder(indent=2).encode(instance_info))
                f.write('\n')

            return Callable.success
        return instance_info_writer_closure

    def instance_info_cleaner(self):
        def instance_info_cleaner_closure(log):
            if self.fs.file_exists(self.info_file):
                log.debug('Removing cluster info file: ' + self.info_file)
                self.fs.remove(self.info_file)
            else:
                log.warn('Cluster info file does not exist: ' + self.info_file)
            return Callable.success
        return instance_info_cleaner_closure

    def __call__(self, logger, xml: XmlUtils=XmlUtils()):

        logger.info('Preparing cluster configuration...')
        cluster_props_file = {
            'jira.node.id': self.instance_hash,
            'jira.shared.home': self.shared_jira_home,
            'ehcache.multicast.port': self.multicast_port,
            'ehcache.listener.port': self.args.cache_port,
            'ehcache.peer.discovery': self.peer_discovery
        }
        for k, v in cluster_props_file.items():
            logger.debug('Using %s: %s' % (k, v))

        with open(self.args.layout.cluster_properties(), 'w') as f:
            f.writelines('%s = %s\n' % (k, v) for k, v in cluster_props_file.items())

        needed_dirs = ['data', 'plugins', 'logos', 'import', 'export']

        for s in needed_dirs:
            subdir = os.sep.join([self.shared_jira_home, s])
            if not self.fs.dir_exists(subdir):
                src = os.sep.join(['.', 'jirahome', s])
                if self.fs.dir_exists(src):
                    logger.info('Missing directory: ' + subdir + ' populating from ' + src)
                    self.fs.copy_tree(src, subdir)
                else:
                    logger.warn('Cannot populate ' + subdir + ' from ' + src + ' (does not exist). '
                                                                               'Creating an empty directory instead.')
                    self.fs.existing_dir(subdir)

        return super().__call__(logger)


class ClusteredRun(Run):

    def __init__(self):
        super().__init__()
        self.description = 'Runs JIRA in clustered mode. This will generate an instance and cluster hash ' \
                           'based on given --cluster-name. Default cluster name is generated from logged ' \
                           'in user and local machine name, so should not clash against other nodes in local ' \
                           'network. In order to link several physical machines into one cluster, use the same' \
                           'cluster name. This command looks for open ports and will never fail because ' \
                           'network ports are unavailable. Use "./jmake clustered info" for a list of ' \
                           'running instances. Using quickstart will make instances set up quickly on one ' \
                           'built JIRA.'

    def __call__(self, args, executor):
        self.cluster = ClusterContext(args)
        executor.append(lambda log: log.info('Forcing instance name to: ' + args.instance_name))
        executor.append_post(self.cluster.instance_info_cleaner())
        super().__call__(args, executor)

    def build_layouts(self, args):
        super().build_layouts(args)
        self.cluster.configure_jira_home(args)

    def define_parser(self, parser):
        super().define_parser(parser)
        parser.add_argument('--cluster-name', action='store', default=None, dest='cluster_name',
                            help='Cluster name.')
        parser.add_argument('--instance-hash', action='store', default=None, dest='instance_hash',
                            help='Instance hash.')
        parser.add_argument('--peer-discovery', action='store', default=None, dest='peer_discovery',
                            choices=['default', 'automatic', 'manual rmiUrls=RMI_URLS'],
                            help='Peer Discovery.')

        self.sample_usage(parser, USAGE, USAGE_COMMANDS)

        self.sample_usage(parser, 'By default jmake tries to build one local cluster. In order to link '
                                  'instances on local network in one cluster use --cluster-name param like this:',
                          [
                              '# run this on each instance',
                              './jmake clustered run quickstart --cluster-name 3rd-floor'
                          ])

        self.sample_usage(parser, 'List locally running instances and their settings: ',
                          [
                              './jmake clustered info'
                          ])

        self.sample_usage(parser, 'Monitor locally running instances and their settings: ',
                          [
                              './jmake clustered monitor'
                          ])



    def get_tasks_post_build_jira(self, args, executor):
        super().get_tasks_post_build_jira(args, executor)
        executor.append(self.cluster.instance_info_writer())
        executor.append(self.cluster)


    def find_port(self, executor, initial_port, default_port, port_name, port_setter):
        port = default_port
        if initial_port != "auto":
            port = initial_port

        executor.append(LocalPortInspector(port, port_name=port_name).probe(port_setter))

    def check_ports(self, args, executor):

        def set_http_port(port):
            args.port = port
        self.find_port(executor, args.port, DEFAULT_HTTP_PORT, "http port", set_http_port)

        def set_sh_port(port):
            args.sh_port = port
        self.find_port(executor, args.sh_port, DEFAULT_SHUTDOWN_PORT, "shutdown port", set_sh_port)

        def set_cache_port(port):
            args.cache_port = port
        self.find_port(executor, "auto", DEFAULT_CACHE_PORT, "clustered cache port", set_cache_port)


class ClusteredDebug(Debug, ClusteredRun):

    def check_ports(self, args, executor):

        def set_http_port(port):
            args.port = port
        self.find_port(executor, args.port, DEFAULT_HTTP_PORT, "http port", set_http_port)

        def set_sh_port(port):
            args.sh_port = port
        self.find_port(executor, args.sh_port, DEFAULT_SHUTDOWN_PORT, "shutdown port", set_sh_port)

        def set_cache_port(port):
            args.cache_port = port
        self.find_port(executor, "auto", DEFAULT_CACHE_PORT, "clustered cache port", set_cache_port)

        def set_debug_port(port):
            args.debug_port = port
        self.find_port(executor, args.debug_port, DEFAULT_DEBUG_PORT, "debug port", set_debug_port)


class ClusteredMonitor (JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'monitor'
        self.description = 'Monitors running clusters.'
        self.prevent_console_reset = True
        self.prevent_post_commands = True
        self.check_branch = False

    def define_parser(self, parser: ArgumentParser):
        self.sample_usage(parser, 'Runs a monitor of running clustered instances. This will block your console.',
                          [
                              './jmake clustered monitor'
                          ])

    def __call__(self, _, executor):
        executor.append_post(lambda logger: subprocess.call('tput cnorm'.split(' ')))
        executor.append(lambda logger: subprocess.call('tput civis'.split(' ')))
        executor.append(lambda logger: subprocess.call(['sh', '-c', 'export IFS=''; SLEEP_TIME=0; while sleep $SLEEP_TIME; do SLEEP_TIME=1; TEXT=`./jmake clustered info`; clear; echo $TEXT; done']))



class ClusteredInfo (JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'info'
        self.description = 'Prints information on running clustered instances.'
        self.prevent_console_reset = True
        self.prevent_post_commands = True
        self.check_branch = False

    def __call__(self, _, executor):
        def clustered_info(_, fs: FileUtils=FileUtils()):
            files = fs.listdir(fs.existing_dir(ClusterContext.CLUSTERED_INFO_DIR))
            clusters = {}

            for file in files:
                if file.startswith('instance-'):

                    with open(os.sep.join([ClusterContext.CLUSTERED_INFO_DIR, file]), 'r') as f:
                        status = JSONDecoder().decode(f.read())

                    key = (status['cluster-name'], status['cluster-hash'])

                    if not key in clusters:
                        clusters[key] = [status]
                    else:
                        clusters[key].append(status)

            if clusters:

                checker = AllPortsChecker()
                checker(Logger().set_none())

                red_ports =  []

                def portprint(port):
                    try:
                        p = int(port)
                        if p in checker.get_open_ports():
                            return Logger.color_text(port, Logger.L_ONLINE)
                        else:
                            red_ports.append(p)
                            return Logger.color_text(port, Logger.L_OFFLINE)
                    except Exception as _:
                        return port

                def homeprint(home:str):
                    return home.replace(fs.abs_path('.'), '.')

                def serverstatus(url:str):
                    try:
                        response = urlopen(Request(url + '/status', headers = {
                            'Accept': 'application/json'
                        }))
                        jsonResponse = json.loads(response.read().decode())
                        return jsonResponse['state']
                    except Exception as _:
                        return 'NOT RUNNING'


                for cluster_info in sorted(clusters.keys(), key=lambda tpl: tpl[0]):
                    cluster_name, cluster_hash = cluster_info
                    print('Cluster %s "%s" (%d instances):' % (cluster_hash, cluster_name, len(clusters[cluster_info])))

                    for instance in sorted(clusters[cluster_info], key=lambda instance: instance['start-time']):
                        print(' -> %s %s ' % (instance['instance-hash'], instance['url']))
                        print('     http: %s ctrl: %s debug: %s mcast: %s peer: %s' %
                              (portprint(instance['http-port']), portprint(instance['shutdown-port']), portprint(instance['debug-port'] if 'debug-port' in instance else 'n/a'),
                               instance['cluster-multicast-port'], portprint(instance['cluster-peer-cache-port'])))
                        print('     home: %s shared: %s' % (homeprint(instance['jira-home']), homeprint(instance['shared-home'])))
                        print('     shutdown: ./jmake run shutdown --instance-name %s' % instance['instance-hash'])
                        print('     status %s' % (serverstatus(instance['url'])))

                    print('')

                if red_ports:
                    print('Wait until all ports are %s before running more instances to avoid port clashes.' % Logger.color_text('green', Logger.L_ONLINE))
                else:
                    print('You can now run more instances with %s' % Logger.color_text('./jmake clustered run', Logger.L_INFO))

            else:
                print('There are no clustered instances running. Run some with %s' % Logger.color_text('./jmake clustered run', Logger.L_INFO))

            return Callable.success

        executor.append(clustered_info)


class Clustered(JmakeModule):
    def __init__(self):
        super().__init__()
        self.command = 'clustered'
        self.description = 'Aids in building, running, DoTing and deploying JIRA in clustered mode. ' \
                           'See ./jmake clustered run --help'

    def get_submodules(self):
        return [ClusteredRun(), ClusteredInfo(), ClusteredMonitor(), ClusteredDebug()]

    def append_format_usage(self):
        return USAGE + '\n ' + '\n '.join(USAGE_COMMANDS) + "\n\nSee also: \n * ./jmake clustered run --help \n" \
               " * https://extranet.atlassian.com/x/FIt3h"

