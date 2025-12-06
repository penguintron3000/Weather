#!/bin/bash
ITERATIONS=100
LOGFILE="test_output.log"
for ((i=1; i<=ITERATIONS; i++)); do
	~/Library/Android/sdk/platform-tools/adb shell pm clear edu.uiuc.cs427app >> "$LOGFILE" 2>&1
	./gradlew :app:connectedAndroidTest --info >> "$LOGFILE" 2>&1
	./gradlew :app:test --rerun-tasks >> "$LOGFILE" 2>&1
done

PATTERN=' > .* FAILED$|Tests on .* failed|FAILURE: Build failed with an exception'
if rg -n "$PATTERN" "$LOGFILE"; then
    echo "" | tee -a "$LOGFILE"
    echo "FAILURES FOUND!"
else
    echo "No failures found"
fi
