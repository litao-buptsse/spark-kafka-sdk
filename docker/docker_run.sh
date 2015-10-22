#!/bin/bash

dir=`dirname $0`
dir=`cd $dir; pwd`

REGISTRY=registry.docker.dev.sogou-inc.com:5000
IMAGE=clouddev/spark-kafka-sdk
VERSION=1.0

CONFIG_FILE=$dir/application.conf
LOG4J_FILE=$dir/log4j.properties
LOG_DIR=$dir/logs

mkdir -p $LOG_DIR

docker run -d --net=host \
    -v $LOG_DIR:/search/app/logs \
    -v $CONFIG_FILE:/search/app/application.conf \
    -v $LOG4J_FILE:/search/app/log4j.properties \
    $REGISTRY/$IMAGE:$VERSION