# Development Container for RefMerge

This Docker compose starts a Linux container including a desktop accessible through a web browser or RustDesk.
All dependencies for building and running the RefMerge tool are included in the container.
You must have installed [Docker](https://docs.docker.com/get-started/get-docker/) and [Git](https://git-scm.com/downloads) on your local system.

## Build the Docker Image

First clone the fork of RefMerge and check out the migration branch (IntelliJ2020.1.2).
The original repository of this project is [Ellis et al.](https://github.com/ualberta-smr/RefMerge).
```
cd <path to your git folder>
git clone --branch IntelliJ2020.1.2 https://github.com/manuelohrndorf/ca.ualberta.cs.smr.refmerge.git
```

Assuming Docker is running, initially, build the Docker image with:
```
cd ca.ualberta.cs.smr.refmerge/docker/dev-container-refmerge
docker compose build --no-cache
```

Then start the container with:
```
docker compose up --build
```
In principle, you can install packages (```sudo apt update && sudo apt install -y <packages>```, no sudo password) and make changes within the containerized desktop as on a normal Ubuntu Linux OS.
However, only the user data in ```/config``` (the user's home directory) is mapped to a Docker volume ```dev_container_refmerge_data``` (see docker-compose.yml), i.e., if you delete the container, only changes wrt. the volume are persistent until you also delete the volume.
 (Checkout the corresponding pages in the Docker Desktop UI or use ```docker ps``` and ```docker volume ls``` to list your containers and volumes.)
If more packages or other dependencies are needed for the project, you should add them (add some point) to the Dockerfile for reproducibility; then rerun the command(s) above to update the container.

**Please do not forget to frequently save, commit and push all your progress on the project to the Git repository for backup!**

## Access the Remote Desktop

### KasmVNC:

After the container has been started, go to ```http://localhost:3000/``` in your browser.
The desktop is only accessible locally.
Note, to use it remotely you would have to remove ```127.0.0.1``` from the docker-compose.yml and consider using a password and HTTPS ```https://localhost:3001/```.
Adjusting the size of the browser window will set the according screen resolution of the desktop.
In the browser window in the left (initially collapsed) side panel, you can also edit settings such as the streaming quality.

```Issue:``` Please note, under *Windows Docker WSL* , currently, there seems to be an issue with memory spikes that can cause crashes of the desktop session.
It seems to be mostly stable if the streaming settings do not exceed *medium streaming setting at FHD 1920 × 1080 pixel, 24 FPS*.
Switching to [Docker Hyper-V](https://forums.docker.com/t/make-hyper-v-default-engine-on-docker-desktop-for-windows-installation/120377/2) also seems to help; however, that is less performant than WSL.
(For testing, open something that causes frequent screen changes, e.g, [animated gifs](https://tenor.com/) then watch the [Docker stats](https://www.docker.com/blog/how-to-monitor-container-memory-and-cpu-usage-in-docker-desktop/) graphs in the Docker desktop UI.)
Alternatively, use RustDesk as described below.

### RustDesk:

RustDesk is installed in the container and can be used to access the desktop using a separated app on your system.
*Please note that this makes the container's desktop also remotely accessible via an ID and password!*
Use KasmVNC to initially connect to the desktop, then open the RustDesk app from the desktop shortcut.
In order to connect to it, install the corresponding [client for your system](https://github.com/rustdesk/rustdesk/releases/tag/1.3.7).
Then enter the ID and one time password shown in the app of the container.
You can also set a permanent password by clicking on the pencil icon, then click: ```Unlock security settings``` (no password) and click ```Set permanent Password```.

To automate the connection process (without connecting to KasmVNC), you can do the following adjustments in the Dockerfile:
1. To automatically start RustDesk at container start, remove the comments (first ```#```) in the Dockerfile below ```AUTOSTART RUSTDESK```.
1. Moreover, you can also add more desktop resolutions by adding the according lines to the additional script in ```Dockerfiles/add_resolutions.sh```.
1. To apply the changes you have to delete the container and volume if already created (using the Docker desktop app or according [commands](https://www.digitalocean.com/community/tutorials/how-to-remove-docker-images-containers-and-volumes)), then rebuild the image as described above.
Be aware that this resets all changes wrt. the container, including the RustDesk ID and password.

After connecting with RustDesk you can change the desktop resolutions in toolbar options.
Moreover, you can set the keyboard to compatibility mode for correcting the keyboard layout.
I also recommend setting the image quality to```Optimize reaction time```.

## Running RefMerge in the Dev-Container

Stopping the container would be comparable to a shut down of the OS.
This is also what Docker does if you shut down your host system or Docker itself; therefore, you may have to start the container after rebooting.
```
docker stop dev-container-refmerge
```
Starting the container would be comparable to booting the OS.
```
docker start dev-container-refmerge
```

The desktop has two links to start IntelliJ for the projects RefMerge and RefactoringMiner:
- IntelliJ IDEA: RefMerge
- IntelliJ IDEA: RefactoringMiner

The corresponding projects are in the git folder in the home directory: ```/config/git/```
- ```/config/git/ca.ualberta.cs.smr.refmerge```
- ```/config/git/com.github.tsantalis.refactoringminer```

Some intial samples for RefMerge are also in the project repository:
- ```/config/git/ca.ualberta.cs.smr.refmerge/samples/```

RefMerges uses a binary build of RefactoringMiner, so technically the RefactoringMiner project is not needed until we want to also modify the code of the refactoring detection algorithm.
In order to start RefMerge:
1. Run ```IntelliJ IDEA: RefMerge``` from the desktop.
1. You may have to wait until the build of the project is finished (see loading bar on the bottom); otherwise, the following tree and menu entries will not yet show up...
1. In IntelliJ, open the Gradle side panel on the right.
1. Open the tree: ```RefMerge > Tasks > intellij > runIde```...
1. then right click and select ```Create...``` (later will be shown as ```Edit...```)
1. In the OS file explorer (see task bar), go to the sample ```/config/git/ca.ualberta.cs.smr.refmerge/samples/move_and_modify_method/move_and_modify_method.repository.txt``` and copy the text line ```LEFT_COMMIT...;RIGHT_COMMIT```.
1. Insert the line into IntelliJ Run Configuration under ```Environment variables``` and close the dialog with OK.
1. Then right click again on ```runIde``` and select ```Run...``` or ```Debug...```.
This will start a second development instance of IntelliJ that loads RefMerge as an IDE plugin.
   - For debugging you can add a breakpoint to ```ca/ualberta/cs/smr/refmerge/RefMerge.java``` in the method ```doMerge```, which is the main process of the algorithm.
1. In the start dialog of the second instance, select ```Open or Import```...
1. then navigate to and open ```/config/git/ca.ualberta.cs.smr.refmerge/samples/move_and_modify_method/move_and_modify_method.repository```.
It is important to use the sample folder which ends on **.repository**; otherwise, RefMerge will not work.
1. In the toolbar, select: ```Tools > RunRefMerge``` to execute the merge process.
  - RefMerge will try to merge the given ```LEFT_COMMIT``` and ```RIGHT_COMMIT``` from the Git repository of the currently opened project.
  - If you have set a breakpoint you can watch the algorithm steps in the debugging view of the initial IntelliJ instance.
  - Have a look into the ```file:///config/git/ca.ualberta.cs.smr.refmerge/samples/move_and_modify_method/report.html``` example report.
    - In branch ```main```, from version ```1``` to version ```2``` the method ```foo()``` is moved from class ```ClassB``` to ```ClassA```.
    - In branch ```branchA``` in the method ```foo()``` from version ```1``` (in branch ```main```) to version ```1-3``` a new line with a print statement is inserted.
    - Assuming we are at branch ```main``` version ```2``` and try to merge version ```1-3``` from branch ```branchA``` a conflict appears.
  - RefMerge then performs the following steps:
    1. Reverting the move of ```foo()``` in the ```main``` branch.
    1. Then it performes the merge of branch ```branchA```.
       Now without contflicts.
    1. Then the move of ```foo()``` is replayed.
    1. The result should be the method ```foo()``` in the class ```ClassA```, including the print statement.

## Live Observing the Repository

The repository contains a Python script that starts a small web server that renders the content of the files in the repository to a HTML page.
This is a live view of the files in repository, which can be helpfull to observe changes in the files during debugging the the algorithm step by step.

1. Open the file ```/config/git/ca.ualberta.cs.smr.refmerge/samples/report_view.py``` and find the variable ```ROOT_DIRECTORY`` at the bottom of the code. 
   Then set folder  of the sample repository to be observed.
1. In the ```/config/git/ca.ualberta.cs.smr.refmerge/samples/``` folder right click and ```Open Terminal Here```.
1. Then run ```python3 report_view.py```, which starts a small web server.
1. Right click and ```Open Link``` to see a web report of the repository at ```http://127.0.0.1:5000```.

## Creating an Example

Currently (with room for improvement ;), the example repositories are created by putting the versions of the Java files into a corresponding folder structure.
The first level of this structure represent the two branches to be merged.
The next folder level represents the commits.
Each of the commit folders contains a snapshot of the project at this version.
To create a Git repository from that structure, modify and run the Bash script (contained in each sample folder) ```bash ./create_git_repo.sh``` for your specific example.

## Generating a Report

The HTML report can be generated using the Python script ```/config/git/ca.ualberta.cs.smr.refmerge/samples/report_generator.py```

1. Open the Python script and find the variables ```root_directory``` and ```output_file```.
   The ```root_directory```point to the plain folder sturcture (not the Git repository) from which the sample was created.
   The ```output_file``` specifies where to store the HTML report file.
   Adjust the variables for your specific example.
1. In the ```/config/git/ca.ualberta.cs.smr.refmerge/samples/``` folder right click and ```Open Terminal Here```.
1. Then run ```python3 report_generator.py```, which generates the report.
