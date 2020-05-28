# -*- coding: utf-8 -*-
"""
Created on Thu May 28 18:45:19 2020

@author: Daniel
"""

import bluetooth

server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
port = 1

print (server_socket)

server_socket.bind(("",port))
server_socket.listen(1)
print("Running Script")

client_socket,address = server_socket.accept()
print("Client_socket Accepted")
print("Accepted Connection from",address)

running = True

while running:
    data = client_socket.recv(1024)
    client_socket.send("Connected to RaspberryPi")
    
    if(data == "q"):
        print("Quit")
        running = False
    else:
        print("Received %s" %data)

client_socket.close()
server_socket.close()

