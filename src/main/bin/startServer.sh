#!/usr/bin/env bash

jar="aroma-example-application.jar"

nohup java -jar $jar > application.log &
