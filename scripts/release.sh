#! /usr/bin/bash

declare GIT_STATUS="$(git status -s)"

# if [[ $GIT_STATUS != "" ]]; then
#     echo "git status is $GIT_STATUS"
#     exit 0
# else
#     echo "No empty changes"
# fi

declare GIT_BRANCH="$(git branch --show-current)"

if [[ $GIT_BRANCH != "main" ]]; then
    echo "Please move changes for release to 'main'"
    echo "CURRENT BRANCH -> $GIT_BRANCH"
    exit 0
fi

declare PROJ_VERSION_PROPERTY="$(./gradlew properties | grep "version:")"
declare PROJ_VERSION="${PROJ_VERSION_PROPERTY//version: /}"
declare -a EXISTING_TAGS=$(git tag -l)

echo "Project Version $PROJ_VERSION"

for tags in "${EXISTING_TAGS[@]}"; do
    if [[ $tags =~ $PROJ_VERSION ]]; then
        echo "Tag Exists"
        echo "Exiting"
        exit 0
    fi
done

echo "Generating dokka docs"
./gradlew dokkaVersion -q -s
echo "Saving docs and updating existing head commit"

git add --all && git commit -a --amend --no-edit && git push origin $GIT_BRANCH --force

git tag "version/$PROJ_VERSION" -m "Release Version $PROJ_VERSION"

# git push origin --tags

echo "Publishing to GitHub and Gradle Plugin Portal"

# ./gradlew publishGprPublicationToGitHubPackagesRepository publishPlugins -q -s