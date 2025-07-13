#! /usr/bin/zsh

mkdir -v features

declare -a COMPONENTS=("ui" "data" "domain")

declare -r SRC_MAIN_KOTLIN="/src/main/kotlin/com/sample"

# Create a project directory
function createDirectory() {
  echo
  echo "Calling mkdir -vp $1$SRC_MAIN_KOTLIN"
  echo
  mkdir -vp "$1$SRC_MAIN_KOTLIN"
  echo
}

# Create a `build.gradle.kts` file within 
function createBuildGradleKtsFile() {
  echo
  echo "Creating $1/build.gradle.kts"
  echo
  touch "$1"
  echo
}

function populateBuildGradleKtsFile() {
  {
    echo "plugins {"
    echo "  alias(libs.plugins.kotlin.jvm)"
    echo "}"
  } > "$1"
}

function createFakeKotlinFile() {
  echo
  echo "Creating Mock .kt file at $1$SRC_MAIN_KOTLIN"
  echo
  touch "$1$SRC_MAIN_KOTLIN/SomeObject.kt"
  echo
}

function populateFakeKotlinFile() {
  {
    echo "package com.sample"
    echo
    echo "object SomeObject { }"
  } > "$1$SRC_MAIN_KOTLIN/SomeObject.kt"
}

for featName in {a..n}; do
    for subFeature in {0..5}; do
        for component in "${COMPONENTS[@]}"; do
            GENERATED_PROJECT="features/feature-$featName/subfeature-$subFeature/$component"
            GENERATED_BUILD_FILE="$GENERATED_PROJECT/build.gradle.kts"
            createDirectory "$GENERATED_PROJECT"
            createFakeKotlinFile "$GENERATED_PROJECT"
            populateFakeKotlinFile "$GENERATED_PROJECT"
            createBuildGradleKtsFile "$GENERATED_BUILD_FILE"
            populateBuildGradleKtsFile "$GENERATED_BUILD_FILE"
        done
    done
done