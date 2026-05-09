#!/bin/sh
set -eu

PORT_TO_USE="${PORT:-8080}"

# Render injects PORT dynamically; patch Tomcat connector before start.
sed -i "s/port=\"8080\"/port=\"${PORT_TO_USE}\"/" /usr/local/tomcat/conf/server.xml

exec catalina.sh run
