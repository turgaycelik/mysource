import json

class OutputEncoder(json.JSONEncoder):

    def default(self, o):
        return o.__dict__

class JsonWriter(object):

    def write(self, file, metrics):
        file.write(self.as_str(metrics))

    def as_str(self, metrics, indent=None):
        return OutputEncoder(sort_keys=True, indent=indent).encode(metrics)
