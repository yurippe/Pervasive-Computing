import database

class Note(object):
    def __init__(self, id):
        note = database.get_note(id)
        loc = database.get_noteloc(id)
        dt = database.get_notetime(id)

        self.ID = id
        self.owner = note[0]
        self.text = note[1]
        self.addtime = note[2]

        self.userid = loc[0]
        self.mac = loc[1]
        self.lat = loc[2]
        self.lng = loc[3]

        self.due = dt[0]

