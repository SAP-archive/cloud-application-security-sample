#!/usr/bin/env bash
echo "Hint: run script with 'source localEnvironmentSetup.sh'"
echo "This script prepares the current shell's environment variables (not permanently)"

export SPRING_PROFILES_ACTIVE='cloud,uaamock,localdb'
export VCAP_APPLICATION='{}'


