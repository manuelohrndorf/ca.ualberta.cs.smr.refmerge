# https://docs.linuxserver.io/images/docker-webtop/
# https://github.com/linuxserver/docker-webtop
# https://github.com/linuxserver/docker-webtop/releases
# FIXME: Memory crashes on WSL: dmesg -T | grep -i oom
FROM lscr.io/linuxserver/webtop:ubuntu-kde-951db35e-ls75

##### PORTS #####

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
    nano \
	htop \
	dos2unix \
    git \
    openjdk-8-jdk \
	maven \
	python3-full \
    wget \
    unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
	
# Download and install RustDesk 1.3.7
RUN apt-get update && apt-get install -y \
    libxdo3 \
	gstreamer1.0-pipewire && \
    wget https://github.com/rustdesk/rustdesk/releases/download/1.3.7/rustdesk-1.3.7-x86_64.deb -O /tmp/rustdesk.deb && \
    dpkg -i /tmp/rustdesk.deb && \
    apt-get install -f -y && \
    rm /tmp/rustdesk.deb && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/*


##### CREATE DESKTOP SHORTCUTS #####

# Clear default desktop shortcuts
RUN rm -rf /home/kasm-default-profile/Desktop

# Ensure the Desktop directory exists
RUN mkdir -p $HOME/Desktop

# Create a desktop shortcut for RustDesk
RUN printf "[Desktop Entry]\n\
Type=Application\n\
Name=RustDesk\n\
Exec=rustdesk %%u\n\
Icon=rustdesk\n\
Terminal=false\n\
Categories=Network;RemoteAccess;\n" > $HOME/Desktop/rustdesk.desktop && \
    chmod +x $HOME/Desktop/rustdesk.desktop

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

##### CREATE AUTOSTARTS #####

# RustDesk
RUN mkdir -p $HOME/.config/autostart
RUN printf "[Desktop Entry]\n\
Type=Application\n\
Name=RustDesk\n\
Exec=/usr/bin/rustdesk %%u\n\
StartupNotify=false\n\
X-GNOME-Autostart-enabled=true\n" > $HOME/.config/autostart/rustdesk.desktop && \
    chmod +x $HOME/.config/autostart/rustdesk.desktop

# Add resolution scripts	
RUN mkdir -p $HOME/.config/my-scripts
COPY Dockerfiles/add_resolution.sh $HOME/.config/my-scripts/add_resolution.sh
COPY Dockerfiles/add_resolutions.sh $HOME/.config/my-scripts/add_resolutions.sh
RUN chmod +x $HOME/.config/my-scripts/add_resolution.sh && \
    chmod +x $HOME/.config/my-scripts/add_resolutions.sh && \
    dos2unix $HOME/.config/my-scripts/add_resolution.sh && \
	dos2unix $HOME/.config/my-scripts/add_resolutions.sh
RUN printf "[Desktop Entry]\n\
Type=Application\n\
Name=Resolution\n\
Exec=bash $HOME/.config/my-scripts/add_resolutions.sh\n\
StartupNotify=false\n\
X-GNOME-Autostart-enabled=true\n" > $HOME/.config/autostart/resolutions.desktop && \
    chmod +x $HOME/.config/autostart/resolutions.desktop

##### MOVE: /home/kasm-user > /config #####

# Workaround: during Docker build, e.g., Maven, uses /home/kasm-user but our abc user home directory is /config
RUN mv /home/kasm-user/* /home/kasm-user/.* $HOME/ 2>/dev/null || true

##### SET USER FOLDER PERMISSIONS #####

USER root
RUN chown -R $PUID:$PGID $HOME
USER $PUID
RUN chmod -R u+rw $HOME
USER root
