from json import JSONDecoder
from utils.UrlUtils import UrlUtils
from utils.FileUtils import FileUtils


class ManifestoUtils():

    def __init__(self, url_utils: UrlUtils=None, fs: FileUtils=FileUtils()):

        self.url_utils = url_utils if url_utils is not None else UrlUtils(fs)

        zone_dev = 'jirastudio-dev'
        zone_dog = 'jirastudio-dog'
        zone_prod = 'jirastudio-prd'
        zone_prodv = 'jirastudio-prd-virtual'

        self.manifesto_zones = [zone_dev, zone_dog, zone_prod, zone_prodv]
        self.manifesto_zone_aliases = {
            'default': zone_dev,
            'dev': zone_dev,
            'dog': zone_dog,
            'prod': zone_prod,
            'prodv': zone_prodv,
            'preprod': zone_prodv
        }

        self.response_cache = {}

    def determine_hash(self, hash_zone_or_alias: str):
        decoded_zone = self.__decode_zone(hash_zone_or_alias.lower())
        if decoded_zone:
            response = self.__cached_response(self.__manifesto_url('/api/env/%s' % decoded_zone))
            return JSONDecoder().decode(response)['hash']
        else:
            return hash_zone_or_alias.lower()

    def __decode_zone(self, manifesto_zone_alias):
        """
         decodes the actual name of the manifesto zone from possible aliases. Returns None for
        """
        if manifesto_zone_alias in self.manifesto_zone_aliases:
            return self.manifesto_zone_aliases[manifesto_zone_alias]
        elif manifesto_zone_alias in self.manifesto_zones:
            return manifesto_zone_alias
        else:
            return None

    def get_plugins_maven_artifacts(self, manifesto_hash):
        response = self.__cached_response(self.__manifesto_url('/static/%s/psd' % manifesto_hash))
        return JSONDecoder().decode(response)['plugins']['jira']

    def get_od_jira(self, manifesto_hash):
        response = self.__cached_response(self.__manifesto_url('/static/%s/psd' % manifesto_hash))
        return JSONDecoder().decode(response)['products']['jira']

    def __manifesto_url(self, path):
        return 'https://manifesto.uc-inf.net' + path

    def __cached_response(self, url):
        if url not in self.response_cache:
            self.response_cache[url] = self.url_utils.read(url, None, None)
        return self.response_cache[url]

    def generate_all_zones_and_aliases(self):
        for zone in self.manifesto_zones:
            yield zone
        for alias in self.manifesto_zone_aliases.keys():
            yield alias




