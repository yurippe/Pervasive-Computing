from PIL import *


def composite_avatar(image):
    #Resize input image to 256x256
    res = image.resize((256,256), PIL.Image.ANTIALIAS)

    #Import mask
    mask = Image.open("nouser", "r")

    #Overlay mask and return result
    res.show()
    return res.paste(res, (0, 0), mask)


def set_avatar(username, image):
    if image.verify():
        try:
            final = composite_avatar(image)
            final.save("/avatars/%s.png" % username)
        except Exception:
            print("Avatar could not be saved for user %s" % username)
    else:
        print("Avatar from %s was not an image" % username)


def get_avatar(username):
    try:
        with open("/avatars/%s.png" % username, "rb") as f:
            img = f.read()
        return img
    except Exception:
        try:
            with open("nouser.png", "rb") as f:
                backup = f.read()
            return backup
        except Exception:
            print("%s attempted to get his avatar and fucked it up ... hard" % username)
            return None
