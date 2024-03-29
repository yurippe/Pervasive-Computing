import hashlib
import time
import Config
import database
from User import User


def getJson(request):
    if request.is_json:
        json = request.get_json(cache=False)
    else:
        json = request.form
    return json


def makeResponseDict(status=200, msg="OK", data={}):
    return {"status" : status, "message" : msg, "data" : data}

