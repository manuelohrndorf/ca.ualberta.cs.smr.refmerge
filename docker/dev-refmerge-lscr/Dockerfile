# https://docs.linuxserver.io/images/docker-webtop/
# https://github.com/linuxserver/docker-webtop
# FIXME: segmentation fault crashes
# FROM lscr.io/linuxserver/webtop:ubuntu-kde
FROM lscr.io/linuxserver/webtop:ubuntu-kde-af041999-ls70

# HTTP VNC
EXPOSE 3000
# HTTPS VNC
EXPOSE 3001

##### ENVIRONMENT VARIABLES #####

# User and Group ID
ENV PUID=1000
ENV PGID=1000

ENV HOME=/config

# Java
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

##### INSTALL PACKAGES #####

# Install additional tools: Git and OpenJDK 8
RUN apt-get update && apt-get install -y \
    git \
    openjdk-8-jdk \
	maven \
    wget \
    unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

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

##### COPY SAMPLE DATA #####

RUN cp -r $HOME/git/ca.ualberta.cs.smr.refmerge/docker/ca.ualberta.cs.smr.refmerge.samples/ $HOME/git/ca.ualberta.cs.smr.refmerge.samples/
WORKDIR $HOME/git/ca.ualberta.cs.smr.refmerge.samples/move_and_modify_method
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

##### MOVE: /home/kasm-user > /config #####

# Workaround: during Docker build, e.g., Maven, uses /home/kasm-user but our abc user home directory is /config
RUN mv /home/kasm-user/* /home/kasm-user/.* $HOME/ 2>/dev/null || true

##### SET USER FOLDER PERMISSIONS #####

USER root
RUN chown -R $PUID:$PGID /config
USER $PUID
RUN chmod -R u+rw $HOME
USER root

##### KASM-VNC RELATED SETTINGS: AVOID DESKTOP CRASHES #####

# create x11 tmp folder
RUN mkdir -p /tmp/.X11-unix && \
	chown root:root /tmp/.X11-unix && \
    chmod 1777 /tmp/.X11-unix