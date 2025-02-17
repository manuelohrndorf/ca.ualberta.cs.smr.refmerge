# Development Container for RefMerge

This container starts a Linux container including a desktop accessible through a web browser.
All dependencies for building and running the tool are included in the container.
You must have installed [Docker](https://docs.docker.com/get-started/get-docker/) and [Git](https://git-scm.com/downloads) installed on your local system.

Start by cloning the repository fork and checking out the migration branch (IntelliJ2020.1.2).
The original repository of this project is [Ellis et al.](https://github.com/ualberta-smr/RefMerge).
```
cd <path to your git folder>
git clone --branch IntelliJ2020.1.2 https://github.com/manuelohrndorf/ca.ualberta.cs.smr.refmerge.git
```

Assuming Docker is started, initially, build the docker image with:
```
cd ca.ualberta.cs.smr.refmerge/docker/dev-container-refmerge
docker compose build --no-cache
```

Then start the container with:
```
docker compose up --build
```
In principle, you can install packages and make changes within the container as on a normal Arch Linux OS.
However, only the user data in ```/config``` is mapped to a Docker volume ```dev_container_refmerge_data``` (see docker-compose.yml), i.e., if you delete the container, only changes wrt. the volume are persistent until you also delete the volume.
 (Chechout the corresponding pages in the Docker Desktop UI or use ```docker ps``` and ```docker volume ls```.)
If more packages or other dependencies are needed, you can add them (add some point) to the Dockerfile for reproducibility, and rerun the command above to update the container.

**Please frequently commit and push all your progress on the project to the Git repository for backup!**

After the container has been started, go to ```http://localhost:3000/``` in your browser.
The desktop is only accessible locally.
If some one wants to use it remotly you would have to remove ```127.0.0.1``` from the docker-compose.yml and consider using a password and ```https://localhost:3001/```.
In the browser window in the left collapse side panel, you can also edit settings such as the streaming quality (recommended, high, 60 FPS).

Stopping the container would be comparable to a hibernation of the machine running the system.
This is also what Docker does if you shutdown your host system or Docker itself; therefore, you may have to restat the container after rebooting.
```
docker stop dev-container-refmerge
```
Starting the container would be comparable to a waking up the machine running the container.
```
docker start dev-container-refmerge
```

On the desktop should be two links to start IntelliJ:
- IntelliJ IDEA: RefMerge: Which is needed to start the tooling.
- IntelliJ IDEA: RefactoringMiner: RefMerges uses a builld of RefactoringMiner.
So technically its not needed 

The corresponding projects are in the git folder in the home directory: ```/config/git/```
- ```/config/git/ca.ualberta.cs.smr.refmerge```
- ```/config/git/com.github.tsantalis.refactoringminer```

Some intial samples for RefMerge are in the project repository:
- ```/config/git/ca.ualberta.cs.smr.refmerge/samples/```

RefMerges uses a binary build of RefactoringMiner, so technically its not needed until we want to also modify the code of the refactory detection.
In order to start RefMerge:
1. Run ```IntelliJ IDEA: RefMerge``` from the desktop.
1. You may have to wait until the build of the project is finished...
1. In IntelliJ, open the Gradle sidepanel on the left.
1. Open the tree: ```RefMerge > Tasks > intellij > runIde```...
1. then right click and select ```Edit...```
1. Go to the sample ```/config/git/ca.ualberta.cs.smr.refmerge/samples/move_and_modify_method/move_and_modify_method.repository.txt``` and copy the text ```LEFT_COMMIT...;RIGHT_COMMIT```.
1. Insert the line into Run Configuration under ```Environment variables``` and close the dialog with OK.
1. Then right again on ```runIde``` and select ```Run...``` or ```Debug...```.
This will start a second instance of IntelliJ that loads RefMerge as a IDE plugin.
1. In the start dialog of the second instance, select ```Open or Import```...
1. then naviagte to and open ```/config/git/ca.ualberta.cs.smr.refmerge/samples/move_and_modify_method/move_and_modify_method.repository```.
It is important to use the sample folder which ends on **.repository**; otherwise it will not work.
1. In the toolbar select: ```Tools > RunRefMerge```.
