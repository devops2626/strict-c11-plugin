#!/bin/sh
./gradlew publishToMavenLocal
echo "✅ Plugin published to local Maven repository."
echo "Add 'mavenLocal()' to your consumer build.gradle repositories to use it."
