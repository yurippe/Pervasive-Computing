import time


class User(object):
    def __init__(self, user):
        self.username = user[2]
        self.token = user[1]
        self.ID = user[0]
        self.displayname = user[4]
        self.last_activity = time.time()