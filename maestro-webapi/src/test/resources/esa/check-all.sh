#!/bin/bash
 find . -name "check.py" -print -execdir python3 check.py --jar ../../../../../target/webapi-1.0.7-SNAPSHOT.jar \;
