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
# Other
@app.route('/test', methods=["POST"])
def hello_world():
    json = util.getJson(request)
    user = database.get_user_object_from_token((json["token"]))
    if user:
        return "Hello " + user.username
    else:
        return 'Hello World!'


######################################################
# MILESTONE 2
@app.route("/login", methods=["POST"])
def login():
    json = util.getJson(request)
    user = database.login_user_object(json["username"], json["password"])
    if user:
        resp = util.makeResponseDict(data={"token": user.token, "username": user.username, "displayname": user.displayname})
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


@app.route('/updatedisplayname', methods=["POST"])
def update_display_name():
    json = util.getJson(request)
    user = database.get_user_object_from_token(json["token"])
    if user:
        database.update_user_displayname(user.ID, json["displayname"])
        return JSON.dumps(util.makeResponseDict(200, "Display name updated"))
    else:
        return JSON.dumps(util.makeResponseDict(403, "Bad credentials"))


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
@app.route('/getnotes', methods=["GET", "POST"])
def get_notes():
    if request.method == "POST":
        json = util.getJson(request)
        user = database.get_user_object_from_token(json["token"])

        if user:
            return JSON.dumps(util.makeResponseDict(data=database.get_notes(user.ID)))
        else:
            return JSON.dumps(util.makeResponseDict(403, "Bad credentials!"))
    else:
        return JSON.dumps(database.get_all_notes())


@app.route('/addnote', methods=["POST"])
def add_note():
    json = util.getJson(request)

    user = database.get_user_object_from_token(json["token"])

    if user:
        #What type of note is it?
        if json["type"] <= 6:
            #Pure note
            id = database.add_note(json["type"], user.ID, json["note"])

            if json["type"] in [0, 1, 2, 3]:
                #Bluetooth / Wifi based
                database.add_notefilter(id, json["filtertype"], json["filter"])

            elif json["type"] in [4, 5]:
                # Locationsbased
                database.add_noteloc(id, json["radius"], json["lat"], json["lng"])

            return JSON.dumps(util.makeResponseDict(200, "Note created", data={"noteid": id}))
        else:
            return JSON.dumps(util.makeResponseDict(400, "Bad notetype"))
    else:
        return JSON.dumps(util.makeResponseDict(403, "Bad credentials!"))


@app.route('/deletenote', methods=["POST"])
def delete_note():
    json = util.getJson(request)

    user = database.get_user_object_from_token(json["token"])

    if user:
        if database.delete_note(json["noteid"], user.ID):
            return JSON.dumps(util.makeResponseDict(200, "Note deleted"))
        else:
            return JSON.dumps(util.makeResponseDict(403, "Not your note!"))
    else:
        return JSON.dumps(util.makeResponseDict(403, "Bad credentials!"))


######################################################
# <3 Friends <3
@app.route('/getfriends', methods=["POST"])
def get_friends():
    json = util.getJson(request)

    user = database.get_user_object_from_token(json["token"])

    if user:
        return JSON.dumps(util.makeResponseDict(200, data=database.get_friends(user.ID)))
    else:
        return JSON.dumps(util.makeResponseDict(403, "Bad credentials!"))


@app.route('/addfriend', methods=["POST"])
def add_friend():
    json = util.getJson(request)

    user = database.get_user_object_from_token(json["token"])

    if user:
        #if user.ID == json["friendid"]:
            #return JSON.dumps(300, "Cannot friend yourself")
        if database.add_friend(user.ID, json["friendid"]):
            return JSON.dumps(util.makeResponseDict(200, "Friend added!"))
        else:
            return JSON.dumps(util.makeResponseDict(400, "ID of other user not existing"))
    else:
        return JSON.dumps(util.makeResponseDict(403, "Bad credentials!"))


@app.route('/deletefriend', methods=["POST"])
def remove_friend():
    json = util.getJson(request)

    user = database.get_user_object_from_token(json["token"])

    if user:
        q = database.delete_friend(user.ID, json["friendid"])
        return JSON.dumps(util.makeResponseDict(200, q))
    else:
        return JSON.dumps(util.makeResponseDict(403, "Bad credentials!"))


######################################################
# Code
@app.route('/code', methods=["GET", "POST"])
def get_code():
    return JSON.dumps(util.makeResponseDict(200, data=database.get_code()))


######################################################
# SETUP
if __name__ == '__main__':
    database.checkDB()
    app.run(host='', port=7777)


