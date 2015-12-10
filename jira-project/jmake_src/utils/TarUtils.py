import tarfile

class TarUtils(object):

    def unpack_file(self, file, path = "."):
        tar = tarfile.open(name = file)
        try:
            tar.extractall(path = path)
        finally:
            tar.close()
