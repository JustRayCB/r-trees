main:
	./mvnw compile
run:
	# ./mvnw exec:java -Dexec.mainClass=projet.SinglePoint
	./mvnw exec:java -Dexec.mainClass=projet.Main >linear.txt
clean:
	./mvnw clean


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
