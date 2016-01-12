#!/usr/bin/env bash

jar="banana-example-application.jar"

nohup java -jar $jar > application.log &
