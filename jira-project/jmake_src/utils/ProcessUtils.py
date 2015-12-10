import subprocess

class ProcessUtils:
    def __init__(self):
        super().__init__()

    def check_output(self, *popenargs, **kwargs):
        return subprocess.check_output(*popenargs, **kwargs)