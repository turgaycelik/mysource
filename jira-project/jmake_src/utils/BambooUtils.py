import re

class BambooUtils(object):

    def decode_build_number(self, build_number:str, default_job:str = 'JOB1'):
        if re.match(r'.*-JOB\d+-\d+', build_number):
            b, job, n = build_number.rsplit('-', 2) # NAME-JOB-NUM
            return job, '-'.join([b, n])
        else:
            return default_job, build_number