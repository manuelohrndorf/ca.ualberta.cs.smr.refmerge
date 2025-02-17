# Development Container for RefMerge

This container starts a Linux container including a desktop accessible through a web browser.
All dependencies for building and running the tool are included in the container.
You must have installed [Docker](https://docs.docker.com/get-started/get-docker/) and [Git](https://git-scm.com/downloads) installed on your local system.

Start by cloning the repository and checking out the migration branch.
The original repository of this project is []().
```
cd <path to your git folder>
git clone
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

Stopping the container would be comparable to a hibernation of the machine running the system.
This is also what Docker does if you shutdown your host system or Docker itself; therefore, you may have to restat the container after rebooting.
```
docker stop dev-container-refmerge
```
Starting the container would be comparable to a waking up the machine running the container.
```
docker start dev-container-refmerge
```



