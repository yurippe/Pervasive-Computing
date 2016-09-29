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
                if json["name"] and json["name"] != device.name:
                    database.update_device_name(json["mac"], json["name"])

            if json["lat"] and json["lng"] and (json["lat"] != device.lat or json["lng"] != device.lng):
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
# SETUP
if __name__ == '__main__':
    database.checkDB()
    app.run(host='', port=7777)


