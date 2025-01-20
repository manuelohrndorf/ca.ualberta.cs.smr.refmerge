# https://github.com/kasmtech/workspaces-images
# https://github.com/linuxserver/docker-webtop/blob/ubuntu-kde/Dockerfile
# https://www.kasmweb.com/docs/latest/how_to/building_images.html
# https://hub.docker.com/r/kasmweb/ubuntu-noble-desktop
FROM kasmweb/ubuntu-noble-desktop:1.16.0
USER root

# HTTPS VNC
EXPOSE 6901

##### ENVIRONMENT VARIABLES #####

# User and Group ID
ENV PUID=1000
ENV PGID=1000

ENV HOME=/home/kasm-user
ENV STARTUPDIR=/dockerstartup
ENV INST_SCRIPTS=$STARTUPDIR/install

ENV DEBIAN_FRONTEND=noninteractive

# Java
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

##### INSTALL PACKAGES #####

# Add sudo and configure it for kasm-user
RUN apt-get update \
    && apt-get install -y sudo \
    && echo 'kasm-user ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers \
    && rm -rf /var/lib/apt/list/*

RUN apt-get update && \
	echo "INSTALL DEV PACKAGES" && \
	apt-get install --no-install-recommends -y \
		git \
		openjdk-8-jdk \
		maven \
		python3-full \
		wget \
		unzip && \
	echo "INSTALL KDE DESKTOP" && \
	apt-get install --no-install-recommends -y \
		dolphin \
		gwenview \
		kde-config-gtk-style \
		kdialog \
		kfind \
		khotkeys \
		kio-extras \
		knewstuff-dialog \
		konsole \
		ksystemstats \
		kubuntu-settings-desktop \
		kubuntu-wallpapers \
		kubuntu-web-shortcuts \
		kwin-addons \
		kwin-x11 \
		kwrite \
		plasma-desktop \
		plasma-workspace \
		plymouth-theme-kubuntu-logo \
		qml-module-qt-labs-platform \
		systemsettings && \
	echo "KDE TWEAKS" && \
	sed -i \
		's/applications:org.kde.discover.desktop,/applications:org.kde.konsole.desktop,/g' \
		/usr/share/plasma/plasmoids/org.kde.plasma.taskmanager/contents/config/main.xml && \
	echo "CLEAN UP" && \
	apt-get autoremove -y && \
	apt-get autoclean -y && \
	apt-get clean && \
	rm -rf \
		/var/lib/apt/lists/* \
		/var/tmp/* \
		/tmp/* \
		$HOME/.cache \
		/root/.cache \
		/home/*/.cache \
		/var/log/* \
		/usr/share/doc/* \
		/usr/share/man/* \
		/usr/share/info/*

# Install IntelliJ IDEA Community Edition 2020.1.2
RUN wget https://download.jetbrains.com/idea/ideaIC-2020.1.2.tar.gz -O /tmp/ideaIC.tar.gz && \
    tar -xzf /tmp/ideaIC.tar.gz -C /opt/ && \
    rm /tmp/ideaIC.tar.gz && \
    ln -s /opt/idea-IC-201.7846.76/bin/idea.sh /usr/local/bin/intellij

##### REFACTORING MINER #####
	
# Prepare Git folder:
RUN mkdir -p $HOME/git

# Clone repository: RefactoringMiner
# RUN git clone --branch 2.1.0 https://github.com/tsantalis/RefactoringMiner.git $HOME/git/com.github.tsantalis.refactoringminer
RUN git clone --branch 2.1.0 https://github.com/manuelohrndorf/com.github.tsantalis.refactoringminer.git $HOME/git/com.github.tsantalis.refactoringminer

# Build repository: RefactoringMiner
WORKDIR $HOME/git/com.github.tsantalis.refactoringminer
RUN ./gradlew jar

# Install RefactoringMiner in Maven as local libarary
USER $PUID
RUN mvn install:install-file -Dfile=$HOME/git/com.github.tsantalis.refactoringminer/build/libs/RefactoringMiner-2.1.0.jar -DgroupId=com.github.tsantalis -DartifactId=refactoring-miner -Dversion=2.1.0 -Dpackaging=jar -DgeneratePom=true
USER root

##### REF MERGE #####

# Clone repository: RefMerge
# RUN git clone https://github.com/ualberta-smr/RefMerge.git $HOME/git/ca.ualberta.cs.smr.refmerge
RUN git clone --branch IntelliJ2020.1.2 https://github.com/manuelohrndorf/ca.ualberta.cs.smr.refmerge.git $HOME/git/ca.ualberta.cs.smr.refmerge

# Setup Git identity for RefMerge
RUN git config --global user.email "you@example.com"
RUN git config --global user.name "Your Name"
RUN git config --global init.defaultBranch main

##### SAMPLE DATA #####

# Install Python packages for report_view.py
WORKDIR $HOME/git/ca.ualberta.cs.smr.refmerge/samples
RUN python3 -m venv /opt/venv && \
    /opt/venv/bin/pip install --no-cache-dir -r requirements.txt
ENV PATH="/opt/venv/bin:$PATH"

WORKDIR $HOME/git/ca.ualberta.cs.smr.refmerge/samples/move_and_modify_method
RUN chmod -R +x create_git_repo.sh
RUN ./create_git_repo.sh

WORKDIR $HOME/git/ca.ualberta.cs.smr.refmerge/samples/pull_up_and_move_method
RUN chmod -R +x create_git_repo.sh
RUN ./create_git_repo.sh

WORKDIR $HOME/git/ca.ualberta.cs.smr.refmerge/samples/pull_up_and_move_method_conflict
RUN chmod -R +x create_git_repo.sh
RUN ./create_git_repo.sh

##### CREATE DESKTOP SHORTCUTS #####

# Clear default desktop shortcuts
RUN rm -rf /home/kasm-default-profile/Desktop

# Ensure the Desktop directory exists
RUN mkdir -p $HOME/Desktop

# Create a desktop shortcut for IntelliJ: RefactoringMiner
RUN printf "[Desktop Entry]\n\
Type=Application\n\
Name=IntelliJ IDEA: RefactoringMiner\n\
Exec=/usr/local/bin/intellij $HOME/git/com.github.tsantalis.refactoringminer\n\
Icon=/opt/idea-IC-201.7846.76/bin/idea.png\n\
Terminal=false\n\
Categories=Development;\n" > $HOME/Desktop/intellij-refactoringminer.desktop && \
    chmod +x $HOME/Desktop/intellij-refactoringminer.desktop

# Create a desktop shortcut for IntelliJ: RefMerge
RUN printf "[Desktop Entry]\n\
Type=Application\n\
Name=IntelliJ IDEA: RefMerge\n\
Exec=/usr/local/bin/intellij $HOME/git/ca.ualberta.cs.smr.refmerge\n\
Icon=/opt/idea-IC-201.7846.76/bin/idea.png\n\
Terminal=false\n\
Categories=Development;\n" > $HOME/Desktop/intellij-refmerge.desktop && \
    chmod +x $HOME/Desktop/intellij-refmerge.desktop

##### KDE RELATED SETTINGS #####

ENV XDG_RUNTIME_DIR=/run/user/$PUID

# Disable hardware acceleration
ENV LIBGL_ALWAYS_SOFTWARE=1
ENV MESA_LOADER_DRIVER_OVERRIDE=llvm
ENV MESA_GLSL=off

RUN mkdir -p /run/user/$PUID && \
	chown -R $PUID:$PGID $XDG_RUNTIME_DIR && \
	chmod -R 0700 $XDG_RUNTIME_DIR

# TODO: https://github.com/linuxserver/docker-webtop/blob/ubuntu-kde/root/defaults/startwm.sh
# COPY /root /

##### SET USER FILE PERMISSIONS #####

RUN chown $PUID:$PGID $HOME && \
	$STARTUPDIR/set_user_permission.sh $HOME && \
	chown -R $PUID:$PGID $HOME

USER $PUID