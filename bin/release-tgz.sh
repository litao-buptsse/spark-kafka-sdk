#!/bin/bash

dir=`dirname $0`
rootDir=`cd $dir/..; pwd`

cd $rootDir
build/sbt clean assembly
ret=$?; [ $ret != 0 ] && exit $ret

name=`cat build.sbt | grep name | head -n 1 | awk -F"\"" '{print $2}'`
version=`cat build.sbt | grep version | head -n 1 | awk -F"\"" '{print $2}'`
scalaVersion=`cat build.sbt | grep scalaVersion | head -n 1 | awk -F"\"" '{print $2}'`
tgzName=${name}_${scalaVersion}-$version

tmpDir=$rootDir/.tmp; mkdir -p $tmpDir; cd $tmpDir
tmpDistDir=$tmpDir/$tgzName; mkdir -p $tmpDistDir

mkdir -p $tmpDistDir/bin $tmpDistDir/conf $tmpDistDir/lib

cp $rootDir/target/scala-2.10/*-assembly-*.jar $tmpDistDir/lib
cp $rootDir/bin/start*.sh $tmpDistDir/bin
cp $rootDir/conf/* $tmpDistDir/conf
cp $rootDir/README.md $tmpDistDir
tar -zcvf $tgzName.tgz $tgzName

distDir=$rootDir/dist; mkdir -p $distDir
rm -fr $distDir/$tgzName.tgz
mv $tmpDir/$tgzName.tgz $distDir

rm -fr $tmpDir