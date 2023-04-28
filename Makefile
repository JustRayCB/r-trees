COMMAND_LINUX_BASED=./mvnw
COMMAND_WINDOWS=.\mvnw.cmd

USED_COMMAND=""

UNAME := $(shell uname)


ifeq ($(OS),Windows_NT)
	USED_COMMAND=$(COMMAND_WINDOWS)
else
	USED_COMMAND=$(COMMAND_LINUX_BASED)
endif

main:
	$(USED_COMMAND) compile
install:
	$(USED_COMMAND) clean install
run:
	$(USED_COMMAND) exec:java -Dexec.mainClass=projet.Main -Dexec.cleanupDaemonThreads=false
clean:
	$(USED_COMMAND) clean


# POUR RAYAN
singlePoint:
	~/nvim.appimage src/main/java/projet/SinglePoint.java
tree:
	~/nvim.appimage src/main/java/projet/RTree.java
node:
	~/nvim.appimage src/main/java/projet/Node.java
leaf:
	~/nvim.appimage src/main/java/projet/Leaf.java
internalNode:
	~/nvim.appimage src/main/java/projet/InternalNode.java
m:
	~/nvim.appimage src/main/java/projet/Main.java
