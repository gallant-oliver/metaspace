#!/bin/sh
MAIN_HOME=$(cd `dirname $0`; pwd)/..
${MAIN_HOME}/bin/metaspace_stop.py && ${MAIN_HOME}/bin/metaspace_start.py