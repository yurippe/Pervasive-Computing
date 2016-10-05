import hashlib
import time
import Config
from User import User
from Device import Device
#import the SQL lite database
import sqlite3
import os


# Hash the password to the database. The username is also used for security
def hash_pass(username, password):
    sha256 = hashlib.sha256()
    sha256.update(Config.SECRET)
    sha256.update(username)
    sha256.update(password)
    return sha256.hexdigest()


# Generate a hashed token
def generate_token(username):
    sha256 = hashlib.sha256()
    sha256.update(Config.SECRET)
    sha256.update(username)
    sha256.update(str(time.time()))
    return sha256.hexdigest()


def connectDB():
    dbpath = os.path.join(os.path.realpath(__file__), 'dPerv.db')
    conn = sqlite3.connect(dbpath)
    return conn


# Check if the schema is implemented in the database.
def checkDB():
    conn = connectDB()
    c = conn.cursor()

    #drop_the_tables()

    #CREATE TABLE IF NOT EXISTS foo (int x, ...)
    #Milestone 2
    c.execute("CREATE TABLE IF NOT EXISTS users(userid INTEGER PRIMARY KEY AUTOINCREMENT, token VARCHAR(32), user VARCHAR(30) UNIQUE NOT NULL, hpass VARCHAR(32) NOT NULL, displayname VARCHAR(64));")
    c.execute("CREATE TABLE IF NOT EXISTS userinfo(userid INTEGER, online INTEGER DEFAULT 0, lastseen INTEGER DEFAULT 0, lat REAL DEFAULT 0, lng REAL DEFAULT 0, FOREIGN KEY(userid) REFERENCES user(userid));")
    c.execute("CREATE TABLE IF NOT EXISTS friends(user1 INTEGER, user2 INTEGER, FOREIGN KEY(user1) REFERENCES users(userid), FOREIGN KEY(user2) REFERENCES users(userid));")
    #Milestone 3
    c.execute("CREATE TABLE IF NOT EXISTS devices(mac VARCHAR(32) PRIMARY KEY, name VARCHAR(128), owner INTEGER, FOREIGN KEY(owner) REFERENCES users(userid));")
    c.execute("CREATE TABLE IF NOT EXISTS deviceinfo(mac VARCHAR(32), lastseen INTEGER DEFAULT 0, lat REAL DEFAULT 0, lng REAL DEFAULT 0, FOREIGN KEY(mac) REFERENCES devices(mac));")
    #Milestone 4
    c.execute("CREATE TABLE IF NOT EXISTS notes(noteid INTEGER PRIMARY KEY AUTOINCREMENT, notetype INTEGER NOT NULL, owner INTEGER REFERENCES users(userid) NOT NULL, note TEXT);")
    c.execute("CREATE TABLE IF NOT EXISTS notepos(noteid INTEGER PRIMARY KEY REFERENCES notes(noteid) ON DELETE CASCADE ON UPDATE CASCADE, radius REAL DEFAULT 10, lat REAL NOT NULL, lng REAL NOT NULL);")
    c.execute("CREATE TABLE IF NOT EXISTS notebluewhy(noteid INTEGER PRIMARY KEY REFERENCES notes(noteid) ON DELETE CASCADE ON UPDATE CASCADE, filtertype INTEGER DEFAULT 0, filter VARCHAR(256));")
    conn.commit()

    #Check if test users are in database
    if not get_user("Steffan"):
        add_user("Steffan", "123")
    if not get_user("Kristian"):
        add_user("Kristian", "123")
    if not get_user("Nicolai"):
        add_user("Nicolai", "123")
    if not get_user("a"):
        add_user("a", "a")

    conn.close()


def drop_the_tables():
    conn = connectDB()
    c = conn.cursor()

    # Milestone 2
    c.execute("DROP TABLE IF EXISTS userinfo;")
    c.execute("DROP TABLE IF EXISTS friends;")
    c.execute("DROP TABLE IF EXISTS users;")
    # Milestone 3
    c.execute("DROP TABLE IF EXISTS deviceinfo;")
    c.execute("DROP TABLE IF EXISTS devices;")
    # Milestone 4
    c.execute("DROP TABLE IF EXISTS notepos;")
    c.execute("DROP TABLE IF EXISTS notebluewhy;")
    c.execute("DROP TABLE IF EXISTS notes;")
    conn.commit()


######################################################
# MILESTONE 2
# Add the user to the database
def add_user(username, password):
    # Check for username being taken
    if get_user(username):
        return False
    else:
        # Hash password
        hpass = hash_pass(username, password)

        # Create new entry in the database
        conn = connectDB()
        c = conn.cursor()

        c.execute("INSERT INTO users(user, hpass, displayname) VALUES ('%s', '%s', '%s');"
                  % (username, hpass, username))
        conn.commit()

        user = get_user(username)
        c.execute("INSERT INTO userinfo(userid) VALUES ('%s');" %(user[0]))
        conn.commit()
        conn.close()

        return True


def get_user(username):
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT * FROM users WHERE user = '%s';" % (username))
    user = c.fetchone()

    conn.close()
    return user


def get_user_object(username):
    user = get_user(username)
    if user:
        return User(user)
    else:
        return None


def get_user_from_token(token):
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT * FROM users WHERE token = '%s';" % (token))
    user = c.fetchone()

    conn.close()
    return user


def get_user_object_from_token(token):
    user = get_user_from_token(token)
    if user:
        return User(user)
    else:
        return None


def get_user_from_id(userid):
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT * FROM users WHERE userid = '%s" % (userid))
    user = c.fetchone()

    conn.close()
    return user


def get_other_users_pos(token):
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT userid, displayname, online, lastseen, lat, lng FROM users NATURAL JOIN userinfo WHERE token <> '%s' OR token IS NULL;" % token)
    result = c.fetchall()
    users = [{"userid": user[0], "displayname": user[1], "online": user[2],
              "lastseen": user[3], "lat": user[4], "lng": user[5]} for user in result]

    conn.close()
    return users


def get_all_users():
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT displayname, online, lastseen, lat, lng FROM users NATURAL JOIN userinfo;")
    result = c.fetchall()
    users = [{"displayname": user[0], "online": user[1], "lastseen": user[2], "lat": user[3], "lng": user[4]} for user in result]

    conn.close()
    return users


def login_user(username, password):
    conn = connectDB()
    c = conn.cursor()

    # Hash password
    hpass = hash_pass(username, password)

    user = get_user(username)

    if user:
        if user[3] == hpass:
            c.execute("UPDATE userinfo SET online = 1, lastseen = '%s' WHERE userid = '%s';"
                      % (int(time.time()), user[0]))
            token = generate_token(user[2])
            c.execute("UPDATE users SET token = '%s' WHERE userid = '%s';" % (token, user[0]))
            conn.commit()
            c.execute("SELECT * FROM users WHERE userid = '%s';" % user[0])
            user = c.fetchone()
            conn.close()
            return user

    conn.close()
    return None


def login_user_object(username, password):
    user = login_user(username, password)
    if user:
        return User(user)
    else:
        return None


def logout_user(userID):
    conn = connectDB()
    c = conn.cursor()

    c.execute("UPDATE users SET token = NULL WHERE userid = '%s';" % (userID))
    c.execute("UPDATE userinfo SET online = 0, lastseen = '%s' WHERE userid = '%s';"
              % (int(time.time()), userID))
    conn.commit()
    conn.close()


def logout_inactive():
    conn = connectDB()
    c = conn.cursor()

    c.execute("UPDATE userinfo SET online = 0 WHERE lastseen = '%s';" % (int(time.time())-1800))
    conn.commit()
    conn.close()


def update_user_loc(userID, lat, lng):
    conn = connectDB()
    c = conn.cursor()

    c.execute("UPDATE userinfo SET lastseen = '%s', lat = '%s', lng = '%s' WHERE userid = '%s';"
              % (int(time.time()), lat, lng, userID))
    conn.commit()
    conn.close()


def update_user_displayname(userID, newname):
    conn = connectDB()
    c = conn.cursor()

    c.execute("UPDATE users SET displayname = '%s' WHERE userid = '%s';" % (newname, userID))
    conn.commit()
    conn.close()


def update_user_password(username, password):
    conn = connectDB()
    c = conn.cursor()

    c.execute("UPDATE users SET hpass = '%s' WHERE user = '%s';" % (hash_pass(username, password), username))
    conn.commit()
    conn.close()


######################################################
# MILESTONE 3
def get_device(mac):
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT name, user FROM devices LEFT JOIN users ON userid = owner WHERE mac = '%s';" % (mac))
    device = c.fetchone()

    conn.close()
    return device


def get_device_object(mac):
    device = get_device(mac)
    if device:
        return Device(mac, device)
    else:
        return None


def get_device_info(mac):
    if get_device(mac):
        conn = connectDB()
        c = conn.cursor()

        c.execute("SELECT lastseen, lat, lng FROM deviceinfo WHERE mac = '%s';" % (mac))
        info = c.fetchone()

        conn.close()
        return info


def get_all_devices():
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT mac, name, user, lastseen, lat, lng FROM users INNER JOIN (SELECT mac, name, owner, lastseen, lat, lng FROM devices NATURAL JOIN deviceinfo) ON userid = owner;")
    result = c.fetchall()
    conn.close()

    devices = [{"mac": device[0], "name": device[1], "owner": device[2], "lastseen": device[3],
                "lat": device[4], "lng": device[5]} for device in result]

    return devices


def add_device(mac):
    if not get_device(mac):
        conn = connectDB()
        c = conn.cursor()

        c.execute("INSERT INTO devices(mac) VALUES ('%s');" % (mac))
        c.execute("INSERT INTO deviceinfo(mac, lastseen) VALUES ('%s', '%s');" % (mac, int(time.time())))
        conn.commit()

        conn.close()
        return True
    else:
        return False


def update_device_name(mac, name):
    if get_device_object(mac):
        conn = connectDB()
        c = conn.cursor()

        c.execute("UPDATE devices SET name = '%s' WHERE mac = '%s';" % (name, mac))
        conn.commit()

        conn.close()


def update_device_owner(mac, username):
    user = get_user_object(username)
    device = get_device_object(mac)

    if user and device:
        conn = connectDB()
        c = conn.cursor()
        c.execute("UPDATE devices SET owner = '%s' WHERE mac = '%s';" % (user.ID, mac))
        conn.commit()
        conn.close()


def update_device_info(mac, lat, lng):
    if get_device_object(mac):
        conn = connectDB()
        c = conn.cursor()

        c.execute("UPDATE deviceinfo SET lastseen = '%s', lat = '%s', lng = '%s' WHERE mac = '%s';"
                  % (int(time.time()), lat, lng, mac))
        conn.commit()

        conn.close()


######################################################
# MILESTONE 4
def get_notes(owner):
    conn = connectDB()
    c = conn.cursor()

    #Type 7
    c.execute("SELECT noteid, notetype, note FROM notes WHERE owner = '%s' AND notetype = 7;" % (owner))
    result = c.fetchall()

    notes = [{"noteid": note[0], "type": note[1], "note": note[2]} for note in result]

    #Type 5-6
    c.execute("SELECT noteid, notetype, note, radius, lat, lng FROM notes NATURAL JOIN noteloc WHERE owner = '%s';" % (owner))
    result = c.fetchall()

    notes += [{"noteid": note[0], "type": note[1], "note": note[2],
              "radius": note[3], "lat": note[4], "lng": note[5]} for note in result]

    #Type 0-4
    c.execute("SELECT noteid, notetype, note, filtertype, filter FROM notes NATURAL JOIN notebluewhy WHERE owner = '%s';" % (owner))
    result = c.fetchall()

    notes += [{"noteid": note[0], "type": note[1], "note": note[2],
               "filtertype": note[3], "filter": note[4]} for note in result]

    conn.close()
    return notes


def get_all_notes():
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT noteid, notetype, note FROM notes WHERE notetype = 7;")
    result = c.fetchall()

    notes = [{"noteid": note[0], "type": note[1], "note": note[2]} for note in result]

    c.execute("SELECT noteid, owner, notetype, note, radius, lat, lng FROM notes NATURAL JOIN noteloc;")
    result = c.fetchall()

    notes = [{"noteid": note[0], "owner": note[1], "type": note[2], "note": note[3],
              "radius": note[4], "lat": note[5], "lng": note[6]} for note in result]

    c.execute("SELECT noteid, owner, notetype, note, filtertype, filter FROM notes NATURAL JOIN notebluewhy;")
    result = c.fetchall()

    notes += [{"noteid": note[0], "owner": note[1], "type": note[2], "note": note[3],
               "filtertype": note[4], "filter": note[5]} for note in result]

    conn.close()
    return notes


def add_note(type, owner, note):
    conn = connectDB()
    c = conn.cursor()

    c.execute("INSERT INTO notes(notetype, owner, note) VALUES ('%s', '%s', '%s');" % (type, owner, note))
    conn.commit()

    noteid = c.lastrowid

    conn.close()
    return noteid


def add_noteloc(id, radius, lat, lng):
    conn = connectDB()
    c = conn.cursor()

    c.execute("INSERT INTO noteloc(noteid, radius, lat, lng) VALUES ('%s', '%s', '%s', '%s');" % (id, radius, lat, lng))
    conn.commit()

    conn.close()
    return True


def add_notefilter(id, filtertype, filter):
    conn = connectDB()
    c = conn.cursor()

    c.execute("INSERT INTO notebluewhy(noteid, filtertype, filter) VALUES ('%s', '%s', '%s');" % (id, filtertype, filter))
    conn.commit()

    conn.close()
    return True


def delete_note(id, owner):
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT * FROM nots WHERE noteid = '%s' AND owner '%s';" % (id, owner))
    owned = c.fetchone()

    if owned:
        c.execute("DELETE FROM notes WHERE noteid = '%s' AND owner = '%s';" % (id, owner))
        conn.commit()

        conn.close()
        return True
    else:
        conn.close()
        return False
