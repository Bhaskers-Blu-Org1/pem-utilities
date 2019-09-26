#!/bin/bash

export JAVA_OPTS="-Dfile.encoding=UTF8 -Djavax.net.ssl.trustStore=<keystore_file_path> -Djavax.net.ssl.trustStorePassword=<keystore_password>"
# Add the following to JAVA_OPTS to enable SSL debug logs - "-Djavax.net.debug=ssl:handshake:verbose:keymanager:trustmanager"

./bin/sfg2pem-migrator Config.properties

echo "Done"