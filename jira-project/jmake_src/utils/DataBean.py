

class DataBean(object):

    """
    All that trouble just because you cannot do:

    o = object()
    o.data = 9000

    The above will not work, o is immutable! So instead classes pop up here and there, use DataBean:

    o = DataBean()
    o.data = 9000

    or, better yet, user the funky constructor:

    o = DataBean(data = 9000)
    o.data     # <- proper member access

    """

    def __init__(self, **kwargs):
        for k, v in kwargs.items():
            setattr(self, str(k), v)


    @staticmethod
    def from_dict(d: dict):
        bean = DataBean()
        for k, v in d.items():
            setattr(bean, k, v)
        return bean

    def __str__(self):
        return self.__dict__.__str__()

    def __repr__(self):
        return self.__str__()