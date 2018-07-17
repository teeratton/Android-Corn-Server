import socket
import sys
import base64
import time
HOST = ''
PORT = 8889
 
s = socket.socket()
print ('socket created')

try:
    s.bind((HOST, PORT))
except socket.error as err:
    print ('Bind Failed, Error Code: ' + str(err[0]) + ', Message: ' + err[1])
    sys.exit()
 
print ('Socket Bind Success!')

s.listen(5)
print ('Socket is now listening')
 
 
while 1:
    bytesReceived = 0
    image = ""
    conn, addr = s.accept()
    print ('Connect with ' + addr[0] + ':' + str(addr[1]))
    imageSize = conn.recv(1024).decode('utf-8')
    print (imageSize)
    
    encoded = str(imageSize).encode('utf-8')
    msg_size = len(encoded) 

    conn.send(encoded)
    total = imageSize
    imageSize = int(imageSize)
    
    while imageSize > 0:
        buf = conn.recv(1024).decode('utf-8')
        image += buf.strip()
        received_size = len(buf)
        bytesReceived += received_size
        imageSize -= received_size
        percent = (int(bytesReceived) / int(total)) * 100
        print("download complete : " + str(percent) + "%")
        
    print("image received")    
    imgbuf = base64.b64decode(image)           
    filename = 'androidImage.jpg'
    f = open(filename, 'wb')
    f.write(imgbuf)
    f.close()
    
s.close()
