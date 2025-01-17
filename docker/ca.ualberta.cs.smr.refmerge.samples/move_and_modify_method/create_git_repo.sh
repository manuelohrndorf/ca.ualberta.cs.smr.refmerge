#!/bin/bash

# Define paths
PATH_MAIN_1=$(realpath "move_and_modify_method.repository_plain/main/1")
PATH_MAIN_2=$(realpath "move_and_modify_method.repository_plain/main/2")
PATH_BRANCH_A_1_3=$(realpath "move_and_modify_method.repository_plain/branchA/1-3")
REPO_NAME="move_and_modify_method.repository"

# Create a new git repository
mkdir "$REPO_NAME"
cd "$REPO_NAME" || exit 1
git init
git branch -m main

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

# Switch to main
git checkout main

