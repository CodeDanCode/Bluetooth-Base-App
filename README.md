# Bluetooth-Base-App
this is a starter app for bluetooth connection with Raspberry Pi

For this project there are two parts, the Raspberry Pi python script/setup and the android application.

Raspberry Pi Setup: 

make sure all packages are up to date
```
sudo apt-get update
sudo apt-get upgrade
```
then install bluetooth packages for managing bluetooth
```
sudo apt install bluetooth pi-bluetooth bluez blueman
```
restart system
```
sudo reboot
```
pair bluetooth devices through GUI or by terminal
```
bluetoothctl
agent on
scan on
```
once device is found through scan
```
scan off
```
pair the device where [xx:xx:xx:xx:xx:xx] is the mac address of the desired device
```
pair [xx:xx:xx:xx:xx:xx]
```
once paired, the devices will auto connect. To avoid pairing everytime you have disconnected we should trust the device
```
trust [xx:xx:xx:xx:xx:xx]
```
now that your devices are paired and trusted the next part is to add the python code to your working directory in the raspberry pi

run the script then connect through the android app. 
