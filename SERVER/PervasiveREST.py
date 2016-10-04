from flask import Flask, session, escape, request, render_template
import json as JSON
import time
import util
import database
import Config

app = Flask(__name__)
app.secret_key = Config.SECRET

SESSIONS = {}


def getUser(token):
    if SESSIONS.has_key(token):
        user = SESSIONS[token]
        user.last_activity = time.time()
        return user
    else:
        return None


######################################################
# MILESTONE 2
@app.route("/login", methods=["POST"])
def login():
    json = util.getJson(request)
    user = database.login_user_object(json["username"], json["password"])
    if user:
        resp = util.makeResponseDict(data={"token": user.token, "username": user.username})
        SESSIONS[user.token] = user
    else:
        resp = util.makeResponseDict(403, "Bad credentials")

    return JSON.dumps(resp)


@app.route("/logout", methods=["POST"])
def logout():
    json = util.getJson(request)
    if SESSIONS.has_key(json["token"]):
        del SESSIONS[json["token"]]
    return JSON.dumps(util.makeResponseDict(200, "Logged out"))


@app.route("/signup", methods=["POST"])
def signup():
    json = util.getJson(request)
    if database.get_user(json["username"]):
        resp = util.makeResponseDict(403, "Username taken")
    else:
        database.add_user(json["username"], json["password"])
        user = database.login_user_object(json["username"], json["password"])
        resp = util.makeResponseDict(data={"token": user.token, "username": user.username})
        SESSIONS[user.token] = user

    return JSON.dumps(resp)


@app.route('/test', methods=["POST"])
def hello_world():
    json = util.getJson(request)
    user = database.get_user_object_from_token((json["token"]))
    if user:
        return "Hello " + user.username
    else:
        return 'Hello World!'


@app.route('/updatepos', methods=["POST"])
def updatepos():
    json = util.getJson(request)
    user = database.get_user_object_from_token(json["token"])
    if user:
        database.update_user_loc(user.ID, json["lat"], json["lng"])
        return JSON.dumps(util.makeResponseDict(200, "Position updated"))
    else:
        return JSON.dumps(util.makeResponseDict(403, "Bad credentials"))


@app.route('/userspos', methods=["GET", "POST"])
def userspos():
    if request.method == "POST":
        json = util.getJson(request)
        return JSON.dumps(database.get_other_users_pos(json["token"]))
    else:
        return JSON.dumps(database.get_all_users())


######################################################
# MILESTONE 3
@app.route('/adddevice', methods=["POST"])
def add_device():
    json = util.getJson(request)
    device = database.add_device(json["mac"])

    #Check if there's more in the request to add
    if json.has_key("owner"):
        user = database.get_user_object(json["owner"])
        if user:
            database.update_device_owner(json["mac"], user.ID)

    if json.has_key("name"):
        database.update_device_name(json["mac"], json["name"])

    if json.has_key("lat") and json.has_key("lng"):
        database.update_device_info(json["mac"], json["lat"], json["lng"])

    #TODO make real response
    return JSON.dumps(util.makeResponseDict())


@app.route('/claimdevice', methods=["POST"])
def claim_device():
    json = util.getJson(request)

    device = database.get_device_object(json["mac"])
    user = database.get_user_object_from_token(json["token"])

    if device and user:
        database.update_device_owner(json["mac"], user.username)

    # TODO make real response
    return JSON.dumps(util.makeResponseDict())


@app.route('/updatedevice', methods=["POST"])
def update_device():
    json = util.getJson(request)
    user = database.get_user_object_from_token(json["token"])

    if user:
        device = database.get_device_object(json["mac"])
        if device:
            if device.owner == user.username:
                if json.has_key("name") and json["name"] != device.name:
                    database.update_device_name(json["mac"], json["name"])

            if json.has_key("lat") and json.has_key("lng") and (json["lat"] != device.lat or json["lng"] != device.lng):
                database.update_device_info(json["mac"], json["lat"], json["lng"])
        else:
            device = database.add_device(json["mac"])


    #TODO make real response
    return JSON.dumps(util.makeResponseDict())


@app.route('/deviceinfo', methods=["POST"])
def device_info():
    json = util.getJson(request)

    device = database.get_device_object(json["mac"])

    if device:
        if device.owner == None:
            dOwner = "unknown"
        else:
            dOwner = device.owner.username

        data = {"name": device.name, "owner": dOwner, "lastseen": device.last_activity, "lat": device.lat, "lng": device.lng}
        return JSON.dumps(util.makeResponseDict(data=data))
    else:
        return JSON.dumps(util.makeResponseDict(404, "Device not known"))


@app.route('/alldevices', methods=["GET"])
def get_all_devices():
    return JSON.dumps(database.get_all_devices())


######################################################
# MILESTONE 4
@app.route('/addnote', methods=["POST"])
def set_note():
    json = util.getJson(request)

    user = database.get_user_object_from_token(json["token"])

    if user:
        noteid = database.add_note(user.ID, json["text"])
        if noteid:
            return JSON.dumps(util.makeResponseDict(200, "Note added", {"noteid": noteid}))
        else:
            return JSON.dumps(util.makeResponseDict(500, "Note not added"))
    else:
        return JSON.dumps(util.makeResponseDict(403, "Bad credentials!"))


@app.route('/updatenote', methods=["POST"])
def update_note():
    json = util.getJson(request)
    user = database.get_user_object_from_token(json["token"])
    note = database.get_note_object(json["id"])

    if note:
        if user and (user.ID == note.owner):
            userid = False
            if json.has_key("userid"):
                userid = json["userid"]

            mac = False
            if json.has_key("mac"):
                mac = json["mac"]

            lat = False
            lng = False
            if json.has_key("lat") and json.has_key("lng"):
                lat = json["lat"]
                lng = json["lng"]

            if userid or mac or lat:
                database.update_noteloc(note.ID, userid, mac, lat, lng)

            due = False
            if json.has_key("due"):
                due = json["due"]

            if due:
                database.update_notetime(note.ID, due)
            return JSON.dumps(util.makeResponseDict(200, "Note updated"))
        else:
            return JSON.dumps(util.makeResponseDict(403, "Bad credentials!"))
    else:
        return JSON.dumps(util.makeResponseDict(404, "Note not known"))


@app.route('/allnotes', methods=["GET", "POST"])
def get_note():
    if request.method == "POST":
        json = util.getJson(request)

        user = database.get_user_object_from_token(json["token"])
        if user:
            return JSON.dumps(database.get_all_notes(user.ID))
        else:
            return JSON.dumps(util.makeResponseDict(403, "Bad credentials"))
    else:
        return JSON.dumps(database.get_all_notes(False))


@app.route('deletenote', methods=["POST"])
def delete_note():
    json = util.getJson(request)
    user = database.get_user_object_from_token(json["token"])
    note = database.get_note_object(json["id"])

    if note:
        if user and (user.ID == note.owner):
            database.delete_note(note.ID)
            return JSON.dumps(200, "Note deleted")
        else:
            return JSON.dumps(util.makeResponseDict(403, "Bad credentials!"))
    else:
        return JSON.dumps(util.makeResponseDict(404, "Note not known"))


######################################################
# SETUP
if __name__ == '__main__':
    database.checkDB()
    app.run(host='', port=7777)


