#!/bin/bash

# Define paths
PATH_MAIN_1=$(realpath "pull_up_and_move_method_conflict.repository_plain/main/1")
PATH_MAIN_2=$(realpath "pull_up_and_move_method_conflict.repository_plain/main/2")
PATH_BRANCH_A_1_3=$(realpath "pull_up_and_move_method_conflict.repository_plain/branchA/1-3")
REPO_NAME="pull_up_and_move_method_conflict.repository"

# Create a new git repository
mkdir "$REPO_NAME"
cd "$REPO_NAME" || exit 1
git init

# Copy the folder into the repository
cp -r "$PATH_MAIN_1/." .

# Stage the copied folder
git add .

# Commit the changes: initial commit
git commit -m "initial commit"

# Copy the folder into the repository
cp -r "$PATH_MAIN_2/." .

# Create branch of: initial commit
git branch branchA

# Stage the copied folder
git add .

# Commit the changes: edit on main
git commit -m "edit on main"

# Get the latest commit hash and write it to a file
LEFT_COMMIT=$(git rev-parse HEAD)

# Switch to branch: branchA
git checkout branchA

# Copy the folder into the repository branch
cp -r "$PATH_BRANCH_A_1_3/." .

# Stage the copied folder
git add .

# Commit the changes: edit on branchA
git commit -m "edit on branchA"

# Get the latest commit hash and write it to a file
RIGHT_COMMIT=$(git rev-parse HEAD)
echo "LEFT_COMMIT=$LEFT_COMMIT;RIGHT_COMMIT=$RIGHT_COMMIT;" > "../${REPO_NAME}.txt"

# Ensure the branch is 'main', and rename if necessary
git branch -m master main || echo "Branch already named main."

# Switch to main
git checkout main

