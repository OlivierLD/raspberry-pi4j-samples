#!/bin/bash
FROM_DIR=$(pwd)
echo -e "Packaging the server..."
rm -rf classes
rm -rf dist
javac -d classes -s src/main/java \
      src/main/java/oliv/events/Utils.java \
      src/main/java/oliv/events/ServerInterface.java \
      src/main/java/oliv/events/ChatTCPServer.java \
      src/main/java/oliv/events/server.java
mkdir dist
echo "Main-Class: oliv.events.server" > manifest.txt
echo "Compile-date: $(date)" >> manifest.txt
cd classes
jar -cfm ../dist/server.jar ../manifest.txt *
#
echo -e "To run the server:"
echo -e "cd ../dist"
echo -e "java -jar server.jar --server-verbose:false --server-port:8000"
echo -e "For help: java -jar server.jar --help"
#
cd ${FROM_DIR}
echo -e "Packaging the client..."
rm -rf classes
# rm -rf dist
javac -d classes -s src/main/java \
      src/main/java/oliv/events/Utils.java \
      src/main/java/oliv/events/ChatTCPClient.java \
      src/main/java/oliv/events/client.java
# mkdir dist
echo "Main-Class: oliv.events.client" > manifest.txt
echo "Compile-date: $(date)" >> manifest.txt
cd classes
jar -cfm ../dist/client.jar ../manifest.txt *
#
echo -e "To run the client:"
echo -e "cd ../dist"
echo -e "java -jar client.jar --client-name:XXX --server-port:8000 --server-name:localhost --client-verbose:false"
echo -e "For help: java -jar client.jar --help"
echo -e "... for the speech, try 'java -Dspeak-french=true -jar client --client-speech:true' (Mac only)"
#
cd ${FROM_DIR}
rm manifest.txt
echo -e "Done."
#
echo -e "--- T O   D I S T R I B U T E ---"
echo -e "* To start a server:"
echo -e "  - get the server.jar from the dist folder"
echo -e "  - from the folder where it lives in, run the 'java -jar server.jar ... ' command."
echo -e "* To start a client:"
echo -e "  - get the client.jar from the dist folder"
echo -e "  - from the folder where it lives in, run the 'java -jar -client.jar ... ' command (once server is started)."
echo -e "---------------------------------"
