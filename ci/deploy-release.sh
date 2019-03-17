#!/bin/bash
set -e
set -uxo pipefail

# Decrypt and import signing key
openssl aes-256-cbc -K $encrypted_ec79e61fc360_key -iv $encrypted_ec79e61fc360_iv -in ci/dropwizard.asc.enc -out ci/dropwizard.asc -d
gpg --fast-import ci/dropwizard.asc

# Avoid error message: "gpg: signing failed: Inappropriate ioctl for device"
# https://tutorials.technology/solved_errors/21-gpg-signing-failed-Inappropriate-ioctl-for-device.html
export GPG_TTY=$(tty)

./mvnw -B deploy --settings 'ci/settings.xml' -DperformRelease=true -Dmaven.test.skip=true

# Documentation
./mvnw -B site site:stage --settings 'ci/settings.xml' -Dmaven.test.skip=true

DOCS_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
mkdir gh-pages
mv target/staging gh-pages/"${DOCS_VERSION}"
