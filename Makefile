main:
	mvn compile
run:
	mvn exec:java -Dexec.mainClass=projet.SinglePoint
clean:
	mvn clean


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
