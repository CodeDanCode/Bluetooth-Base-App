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
once paired, the devices will auto connect on first pairing. To avoid pairing everytime you have disconnected we should trust the device
```
trust [xx:xx:xx:xx:xx:xx]
```
now that your devices are paired and trusted the next part is to add the python code to your working directory in the raspberry pi

Android Setup:

Open android studio, import existing project, make any necessary changes to the existing project and install app to android device.

Making the Connection: 

Once devices are paired and trusted change to the working directory in the RPi terminal and run the script.
```
python rpi_bluetooth.py
```
once started open app on the android device and click the connect button.  
This should show all devices that your phone has been paired to.  
click on the raspberry pi that is listed. this should make the insecure connection for bluetooth communication.  
once connected the android app should display "Connected to RaspberryPi".  
click the send button and you should see "Received 1" in the Raspberry Pi terminal.  

and there you go you have successfully sent/received data between two devices using bluetooth.  
