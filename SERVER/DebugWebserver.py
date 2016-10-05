import socket

responses = {302:"Found"}
def createResponse(body, code=302):
    try:
        codestr = responses[code]
    except Exception as e:
        codestr = ""
        print e
        
    data = "HTTP/1.1 " + str(code) + codestr + "\n"
    data += "Content-Type: text/html; charset UTF-8\n"
    data += "Content-Length: " + str(len(body)) + "\n"

    data += "\n" #end of headers
    data += body
    return data

class WebClientSocket():

    def __init__(self, socket):
        self.socket = socket
        self.socket.setblocking(0)

    def receive(self, buffersize=8192):
        total_data = []
        try:
            data = self.socket.recv(buffersize)
            while data:
                total_data.append(data)
                data = self.socket.recv(buffersize)
        except: pass
        return "".join(total_data)

    def send(self, data):
        self.socket.send(data)

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind(('', 81))
s.listen(5)

while 1:
    try:
        c, addr = s.accept()
        print "----CONNECTION FROM: " + str(addr) + "-----"
        client = WebClientSocket(c)
        print client.receive()
        print "--------------------------------------------"
        client.send(createResponse("302 OK", 302))
    except KeyboardInterupt:
        s.close()
