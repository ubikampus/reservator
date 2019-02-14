#!/bin/bash
cd $( dirname "$0" )
git pull && ./gradlew assembleRelease && \
cp app/build/outputs/apk/release/app-release.apk reservator-$( date "+%Y-%m-%d" ).apk

