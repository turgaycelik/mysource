import os
import subprocess
import sys

_cygpath_process = None


def abspath(path):
    """
    Returns os.path.abspath or if run under cygwin uses cygpath to convert POSIX path to native Windows filename.
    """
    if sys.platform != "cygwin":
        return os.path.abspath(path)
    global _cygpath_process
    if not _cygpath_process:
        _cygpath_process = subprocess.Popen(["cygpath.exe", "-a", "-m", "-f", "-"], stdin=subprocess.PIPE,
                                         stdout=subprocess.PIPE)
    _cygpath_process.stdin.write(path.encode() + b'\n')
    return _cygpath_process.stdout.readline().rstrip().decode()
