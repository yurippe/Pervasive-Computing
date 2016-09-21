from flask import Flask, session, escape, request, render_template
import json as JSON
import time
import util
import database
import Config
from User import User

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


@app.route("/login", methods=["POST"])
def login():
    if request.method == "POST":
        json = util.getJson(request)
        user = util.loginUser(json["username"], json["password"])
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
    if request.method == "POST":
        json = util.getJson(request)
        if database.get_user(json["username"]):
            resp = util.makeResponseDict(403, "Username taken")
        else:
            database.add_user(json["username"], json["password"])
            user = util.loginUser(json["username"], json["password"])
            if user:
                resp = util.makeResponseDict(data={"token": user.token, "username": user.username})
                SESSIONS[user.token] = user
            else:
                #this will never happen...
                resp = util.makeResponseDict(403, "Server shit the bed")

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
        data = database.update_user_loc(user.ID, json["lat"], json["lng"])
        resp = util.makeResponseDict(data)
    else:
        resp = util.makeResponseDict(403, "Bad credentials")

    return JSON.dumps(resp)


@app.route('/userspos', methods=["POST"])
def uesrspos():
    json = util.getJson(request)
    user = database.get_user_object_from_token(json["token"])
    if user:
        resp = database.get_other_users_pos(user.token)
    else:
        resp = util.makeResponseDict(403, "Bad credentials")

    return JSON.dumps(resp)

if __name__ == '__main__':
    database.checkDB()
    app.run(host='', port=7777)
