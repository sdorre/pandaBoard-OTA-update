## OTA update simulation on a Pandaboard

This application simulate an OTA update on this device. To do so, the application check on Build.VERSION package the Incremental number on the device and check with the number returned by an HTTP Server.
If there is a difference between these two number, the device had to reboot.

# Application configuration
There is a Settings Fragment that you can start with the menu. You can defined the server address.
example : "http://192.168.1.3"
and the port where to find your HTTP server.
example : 8080

# Server
If you want to use this application you need to create a simple server. The command given below let you do that.

while true; do echo -e "HTTP/1.1 200 OK\n\n {release:4.0.1-2.1}" | nc -l -p 8080; done

This command need to have netcat installed on your Linux PC.
