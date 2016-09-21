from PIL import *


def composite_avatar(image):
    #Resize input image to 256x256
    res = image.resize((256,256), PIL.Image.ANTIALIAS)

    #Import mask
    mask = Image.open("/avatars/nouser", "r")

    #Overlay mask and return result
    res.show()
    return res.paste(res, (0, 0), mask)


def set_avatar(userid, image):
    if image.verify():
        try:
            final = composite_avatar(image)
            final.save("/avatars/%s.png" % userid)
        except Exception:
            print("Avatar could not be saved for user %s" % userid)
    else:
        print("Avatar from %s was not an image" % userid)


def get_avatar(userid):
    try:
        with open("/avatars/%s.png" % userid, "rb") as f:
            img = f.read()
        return img
    except Exception:
        try:
            with open("/avatars/nouser.png", "rb") as f:
                backup = f.read()
            return backup
        except Exception:
            print("%s attempted to get his avatar and fucked it up ... hard" % username)
            return None
