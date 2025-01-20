@echo off

SET "PATH_MAIN_1=pull_up_and_move_method.repository_plain\main\1"
FOR %%I IN ("%PATH_MAIN_1%") DO SET "PATH_MAIN_1=%%~fI"

SET "PATH_MAIN_2=pull_up_and_move_method.repository_plain\main\2"
FOR %%I IN ("%PATH_MAIN_2%") DO SET "PATH_MAIN_2=%%~fI"

SET "PATH_BRANCH_A_1_3=pull_up_and_move_method.repository_plain\branchA\1-3"
FOR %%I IN ("%PATH_BRANCH_A_1_3%") DO SET "PATH_BRANCH_A_1_3=%%~fI"

SET "REPO_NAME=pull_up_and_move_method.repository"

REM Create a new git repository
mkdir "%REPO_NAME%"
cd "%REPO_NAME%" || exit /b 1
git init
git branch -m main

REM Copy the folder into the repository
xcopy "%PATH_MAIN_1%" . /E /I /Q /Y

REM Stage the copied folder
git add .

REM Commit the changes: initial commit
git commit -m "initial commit"

REM Copy the folder into the repository
xcopy "%PATH_MAIN_2%" . /E /I /Q /Y

REM Create branch of: initial commit
git branch branchA

REM Stage the copied folder
git add .

REM Commit the changes: edit on main
git commit -m "edit on main"

REM Switch to branch: branchA
git checkout branchA

REM Copy the folder into the repository branch
xcopy "%PATH_BRANCH_A_1_3%" . /E /I /Q /Y

REM Stage the copied folder
git add .

REM Commit the changes: edit on branchA
git commit -m "edit on branchA"

REM Get the latest commit hash and write it to a file
FOR /F "delims=" %%H IN ('git rev-parse HEAD') DO (
    echo LEFT_COMMIT=%%H;RIGHT_COMMIT=%%H;> ../%REPO_NAME%.txt
)

REM Switch to main
git checkout main
