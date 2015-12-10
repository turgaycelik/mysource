import os
import re
from functools import reduce
from CommandExecutor import SystemCallable, Callable
from Logger import Logger, LOG

class Git(SystemCallable):

    def __init__(self, command):
        super().__init__(None, 'git %s' % command)
        self.output = []

    def process_output(self, logger : Logger, line : str, num : int):
        # I'd love a yield here, but that would make more processes open at one time, which is not fine for nested loops.
        if line: self.output.append(line)

    def call(self):
        super().__call__(LOG)
        for line in self.output:
            if self.returncode:
                if line: LOG.error(line)
            else:
                if line: LOG.trace(line)
        return self.output

class GitException(Exception):
    pass

class GitResult():
    def __init__(self, returncode, output):
        super().__init__()
        self.returncode = returncode
        self.output = output

class GitExecutor():
    def execute(self, command, exception_on_error: bool = True):
        git = Git(command)
        call_result = git.call()
        if git.returncode != 0 and exception_on_error:
            raise GitException('Return code expected to be 0 but was %s.' % git.returncode)
        return GitResult(git.returncode, call_result)

class GitUtils():

    def __init__(self, git_executor: GitExecutor = GitExecutor()):
        super().__init__()
        self.git_executor = git_executor

    def execute_git(self, command, exception_on_error = True):
        return self.git_executor.execute(command, exception_on_error)

    def current_branch(self):
        return self.execute_git('rev-parse --abbrev-ref HEAD').output[0]

    def current_commit(self):
        return self.execute_git('''log -1 --pretty=format:'%H' ''').output[0]

    def get_notes(self, notes_ref: str, commit: str = None):
        if commit is None:
            # get latest commit
            notes_output = self.execute_git('notes --ref=%s' % notes_ref).output[0]
            commit = notes_output[notes_output.find(' ')+1:]

        return reduce(lambda r, e: r + e, self.execute_git('notes --ref=%s show %s' % (notes_ref, commit)).output)

    def latest_annotated_commit_with_details(self, notes_ref: str):
        return next(self.generate_annotated_commits_with_details(notes_ref), None)

    def generate_annotated_commits_with_details(self, notes_ref: str, max_log_count: int = 30000, commit_range: str = ''):
        git_command = '''log --show-notes={0} -n {1} --pretty=format:"hash,%H,shorthash,%h,commiter,%an <%ae>,date,%at|__NOTE__|%N" {2}'''
        git_result = self.execute_git(git_command.format(notes_ref, max_log_count, commit_range).strip())

        for line in git_result.output:
            if re.match(r"^.*\|__NOTE__\|.*$", line):
               if not re.match(r"^.*\|__NOTE__\|\s*$", line):
                    parsed = self._parse_git_log_line(line)
                    yield parsed

    def generate_annotated_commits(self, notes_ref: str):
        for line in self.execute_git('notes --ref=%s' % notes_ref).output:
            yield line[line.find(' ')+1:]

    def put_notes(self, notes:str, ref:str, commit_hash:str, rewriteExisting:bool = True):
        additional_opts = []
        if rewriteExisting:
            additional_opts += ['-f']
        filename = '.note'
        with open(filename, 'w', encoding='utf-8') as f:
            f.write(notes)
        rc = self.execute_git('''notes --ref=%s add %s -F .note %s''' %
                                (ref, ' '.join(additional_opts), commit_hash)).returncode
        os.remove(filename)
        return rc

    def is_clean_workspace(self):
        return len(self.execute_git('status --short -uno').output) == 0

    def _parse_git_log_line(self, log_line):
        log_rest, log_note = log_line.split('|__NOTE__|', 1)
        details = log_rest.split(',')
        it = iter(details)
        res = dict(zip(it, it))
        res['note'] = log_note
        return res

    def get_commit_details(self, notes_ref: str, commit: str = ""):
        git_command = '''log --show-notes=%s -n 1 --pretty=format:"hash,%%H,shorthash,%%h,commiter,%%an <%%ae>,date,%%at|__NOTE__|%%N" %s'''
        result = self.execute_git(git_command % (notes_ref, commit), False)
        if result.returncode:
            return None
        res = self._parse_git_log_line(result.output[0])
        return res

    def fetch_notes(self, ref:str):
        return self.execute_git('fetch origin refs/notes/{0}:refs/notes/{0}'.format(ref), False).returncode

    def push_notes(self, ref:str):
        return self.execute_git('push origin refs/notes/%s' % ref, False).returncode

    def set_remote(self, remote:str, url:str):
        return self.execute_git('remote add "%s" "%s"' % (remote, url), False).returncode

    def get_remotes(self):
        return self.execute_git('remote -vv').output

    def set_user(self, user: str, email: str):
        for res in [self.execute_git('config user.name "%s"' % user, False), self.execute_git('config user.email "%s"' % email, False)]:
            if res.returncode != 0:
                return Callable.do_not_proceed
        return 0

    def get_config_by_key(self, key: str):
        out = self.execute_git('config --get %s' % key).output
        return out[0] if len(out) == 1 else None
