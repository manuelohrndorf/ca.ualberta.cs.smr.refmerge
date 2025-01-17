# Use LinuxServer.io's VNC base image
# https://docs.linuxserver.io/images/docker-webtop/
FROM lscr.io/linuxserver/webtop:ubuntu-kde

##### KASM-VNC RELATED SETTINGS #####

# VNC port
EXPOSE 5901
# HTTP port (not recommended due to frequent desktop crashes (segmentation error))
EXPOSE 6901
# HTTPS port (desktop crashes (segmentation error) sometimes on first connect, just F5 refresh the browser page)
EXPOSE 3001
# RDP port
EXPOSE 3389

# Set environment variables for user ID (e.g., USER $PUID), group ID, home directory
ENV PUID=1000
ENV PGID=1000
ENV HOME=/config

# Avoid desktop crashes
RUN mkdir -p /tmp/.X11-unix
RUN chown root:root /tmp/.X11-unix && \
    chmod 1777 /tmp/.X11-unix

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
	
# Set environment Java variables
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Install IntelliJ IDEA Community Edition 2020.1.2
RUN wget https://download.jetbrains.com/idea/ideaIC-2020.1.2.tar.gz -O /tmp/ideaIC.tar.gz && \
    tar -xzf /tmp/ideaIC.tar.gz -C /opt/ && \
    rm /tmp/ideaIC.tar.gz && \
    ln -s /opt/idea-IC-201.7846.76/bin/idea.sh /usr/local/bin/intellij

##### REFACTORING MINER #####
	
# Prepare Git folder:
RUN mkdir -p /config/git

# Clone repository: RefactoringMiner
RUN git clone --branch 2.1.0 https://github.com/tsantalis/RefactoringMiner.git /config/git/com.github.tsantalis.refactoringminer
WORKDIR /config/git/com.github.tsantalis.refactoringminer

# Patch: RefactoringMiner
COPY com.github.tsantalis.refactoringminer/build.gradle /config/git/com.github.tsantalis.refactoringminer/

# Build repository: RefactoringMiner
RUN ./gradlew jar

# Install RefactoringMiner in Maven as local libarary
USER $PUID
RUN mvn install:install-file -Dfile=/config/git/com.github.tsantalis.refactoringminer/build/libs/RefactoringMiner-2.1.0.jar -DgroupId=com.github.tsantalis -DartifactId=refactoring-miner -Dversion=2.1.0 -Dpackaging=jar -DgeneratePom=true
USER root

##### REF MERGE #####

# Clone repository: RefMerge
RUN git clone https://github.com/ualberta-smr/RefMerge.git /config/git/ca.ualberta.cs.smr.refmerge
WORKDIR /config/git/ca.ualberta.cs.smr.refmerge

# Patch: RefMerge
COPY ca.ualberta.cs.smr.refmerge/build.gradle /config/git/ca.ualberta.cs.smr.refmerge/
COPY ca.ualberta.cs.smr.refmerge/src/main/java/ca/ualberta/cs/smr/refmerge/utils/GitUtils.java /config/git/ca.ualberta.cs.smr.refmerge/src/main/java/ca/ualberta/cs/smr/refmerge//utils/
COPY ca.ualberta.cs.smr.refmerge/src/main/java/ca/ualberta/cs/smr/refmerge/utils/Utils.java /config/git/ca.ualberta.cs.smr.refmerge/src/main/java/ca/ualberta/cs/smr/refmerge/utils/

# Setup Git identity for RefMerge
RUN git config --global user.email "abc@example.com"
RUN git config --global user.name "abc"

##### COPY SAMPLE DATA #####

COPY ca.ualberta.cs.smr.refmerge.samples/ /config/git/ca.ualberta.cs.smr.refmerge.samples/

##### CREATE DESKTOP SHORTCUTS #####

# Ensure the Desktop directory exists
RUN mkdir -p /config/Desktop

# Create a desktop shortcut for IntelliJ: RefactoringMiner
RUN printf "[Desktop Entry]\n\
Type=Application\n\
Name=IntelliJ IDEA: RefactoringMiner\n\
Exec=/usr/local/bin/intellij /config/git/com.github.tsantalis.refactoringminer\n\
Icon=/opt/idea-IC-201.7846.76/bin/idea.png\n\
Terminal=false\n\
Categories=Development;\n" > /config/Desktop/intellij-refactoringminer.desktop && \
    chmod +x /config/Desktop/intellij-refactoringminer.desktop

# Create a desktop shortcut for IntelliJ: RefMerge
RUN printf "[Desktop Entry]\n\
Type=Application\n\
Name=IntelliJ IDEA: RefMerge\n\
Exec=/usr/local/bin/intellij /config/git/ca.ualberta.cs.smr.refmerge\n\
Icon=/opt/idea-IC-201.7846.76/bin/idea.png\n\
Terminal=false\n\
Categories=Development;\n" > /config/Desktop/intellij-refmerge.desktop && \
    chmod +x /config/Desktop/intellij-refmerge.desktop

##### MOVE: /home/kasm-user > /config #####

# Workaround: during Docker build, e.g., Maven, uses /home/kasm-user but our abc user home directory is /config
RUN mv /home/kasm-user/* /home/kasm-user/.* /config/ 2>/dev/null || true

##### SET USER FOLDER PERMISSIONS #####

USER root
RUN chown -R $PUID:$PGID /config
USER $PUID
RUN chmod -R u+rw /config
USER root