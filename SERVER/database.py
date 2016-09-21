import hashlib
import time
import Config
from User import User
#import the SQL lite database
import sqlite3


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
    conn = sqlite3.connect('dPerv.db')
    return conn


# Check if the schema is implemented in the database.
def checkDB():
    conn = connectDB()
    c = conn.cursor()

    #CREATE TABLE IF NOT EXISTS foo (int x, ...)
    #Milestone 2
    c.execute("CREATE TABLE IF NOT EXISTS users(userid INTEGER PRIMARY KEY AUTOINCREMENT, token VARCHAR(32), user VARCHAR(30) UNIQUE NOT NULL, hpass VARCHAR(32) NOT NULL, displayname VARCHAR(64));")
    c.execute("CREATE TABLE IF NOT EXISTS userinfo(userid INTEGER, online INTEGER DEFAULT 0, lastseen INTEGER DEFAULT 0, lat REAL DEFAULT 0, lng REAL DEFAULT 0, FOREIGN KEY(userid) REFERENCES user(userid));")
    c.execute("CREATE TABLE IF NOT EXISTS friends(user1 INTEGER, user2 INTEGER, FOREIGN KEY(user1) REFERENCES user(userid), FOREIGN KEY(user2) REFERENCES user(userid));")
    c.execute("CREATE TABLE IF NOT EXISTS blocked(user1 INTEGER, user2 INTEGER, FOREIGN KEY(user1) REFERENCES user(userid), FOREIGN KEY(user2) REFERENCES user(userid));")
    conn.commit()

    #Check if test user is in database
    if not get_user("Steffan"):
        add_user("Steffan", "123")
    if not get_user("Kristian"):
        add_user("Kristian", "123")
    if not get_user("Nicolai"):
        add_user("Nicolai", "123")
    if not get_user("Matus"):
        add_user("Matus", "123")

    #Milestone 3

    #Milestone 4

    #Finished!
    #conn.commit()
    conn.close()


# Add the user to the database
def add_user(username, password):
    # Check for username being taken
    if get_user(username):
        return None
    else:
        # Hash password
        hpass = hash_pass(username, password)

        # Create new entry in the database
        conn = connectDB()
        c = conn.cursor()

        c.execute("INSERT INTO users(user, hpass, displayname) VALUES ('%s', '%s', '%s')" %(username, hpass, username))
        conn.commit()

        user = get_user(username)
        c.execute("INSERT INTO userinfo(userid) VALUES ('%s')" %(user[0]))
        conn.commit()
        conn.close()


def get_user(username):
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT * FROM users WHERE user = '%s'" % (username))
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

    c.execute("SELECT * FROM users WHERE token = '%s'" % (token))
    user = c.fetchone()

    conn.close()
    return user


def get_user_object_from_token(token):
    user = get_user_from_token(token)
    if user:
        return User(user)
    else:
        return None


def login_user(username, password):
    conn = connectDB()
    c = conn.cursor()

    # Hash password
    hpass = hash_pass(username, password)

    user = get_user(username)

    if user:
        if user[3] == hpass:
            c.execute("UPDATE userinfo SET online = 1, lastseen = %s WHERE userid = %s" %(int(time.time()), user[0]))
            token = generate_token(user[2])
            c.execute("UPDATE users SET token = '%s' WHERE userid = '%s'" %(token, user[0]))
            conn.commit()
            c.execute("SELECT * FROM users WHERE userid = '%s'" % user[0])
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

    c.execute("UPDATE users SET token = NULL WHERE userid = '%s'" %(userID))
    c.execute("UPDATE userinfo SET online = 0, lastseen = '%s' WHERE userid = '%s'" %(int(time.time()), userID))
    conn.commit()
    conn.close()


def update_user_loc(userID, lat, lng):
    conn = connectDB()
    c = conn.cursor()

    c.execute("UPDATE userinfo SET lastseen = '%s', lat = '%s', lng = '%s' WHERE userid = '%s'" %(int(time.time()), lat, lng, userID))
    conn.commit()
    conn.close()


def update_user_displayname(userID, newname):
    conn = connectDB()
    c = conn.cursor()

    c.execute("UPDATE users SET displayname = '%s' WHERE userid = '%s'" %(newname, userID))
    conn.commit()
    conn.close()


def update_user_password(username, password):
    conn = connectDB()
    c = conn.cursor()

    c.execute("UPDATE users SET hpass = '%s' WHERE user = '%s'" %(hash_pass(username, password), username))
    conn.commit()
    conn.close()


def logout_inactive():
    conn = connectDB()
    c = conn.cursor()

    c.execute("UPDATE userinfo SET online = 0 WHERE lastseen = '%s'" %(int(time.time())-1800))
    conn.commit()
    conn.close()


def get_other_users_pos(token):
    conn = connectDB()
    c = conn.cursor()

    c.execute("SELECT userid, displayname, online, lastseen, lat, lng FROM users NATURAL JOIN userinfo WHERE token <> '%s' OR token IS NULL" % token)
    result = c.fetchall()
    users = [{"userid": user[0], "displayname": user[1], "online": user[2], "lastseen": user[3], "lat": user[4], "lng": user[5]} for user in result]

    conn.close()
    return users