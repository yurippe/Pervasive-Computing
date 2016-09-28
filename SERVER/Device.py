import time
import database


class Device(object):
    def __init__(self, mac, device):
        info = database.get_device_info(mac)

        self.name = device[0]
        self.owner = database.get_user_object(device[1])
        self.last_activity = time.time()
        self.lat = info[1]
        self.lng = info[2]