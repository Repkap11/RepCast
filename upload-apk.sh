#!/bin/bash
if [ "$#" -ne 1 ]; then
	echo "usage: Please spesify the version of the app to upload."
	exit
fi
APP_VERSION=$1
LOCAL_FILE=./app/build/outputs/apk/debug/RepCast-$APP_VERSION-debug.apk
REMOTE_USERNAME=mark
REMOTE_SERVER=repkam09.com
REMOTE_FILE=/home/mark/website/repkam09.com/dl/repcast/repcast.apk
REMOTE_TARGET=$REMOTE_USERNAME@$REMOTE_SERVER:$REMOTE_FILE
echo "Copying $LOCAL_FILE to $REMOTE_TARGET"
rsync --update $LOCAL_FILE $REMOTE_TARGET

