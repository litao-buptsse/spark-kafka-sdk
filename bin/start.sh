#!/bin/bash

dir=`dirname $0`
rootDir=`cd $dir/..; pwd`
binDir=$rootDir/bin
confDir=$rootDir/conf
libDir=$rootDir/lib

export SPARK_CLASSPATH=.:$confDir:$SPARK_CLASSPATH

spark-submit \
    --master yarn-client \
    --jars $confDir/application.conf,$confDir/log4j.properties \
    --executor-memory 512M \
    --driver-memory 1G \
    --num-executors 3 \
    --executor-cores 1 \
    --queue root.spark \
    --class com.sogou.spark.streaming.SparkStreaming \
    $libDir/spark-kafka-sdk-assembly-1.0.jar
