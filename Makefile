main:
	mvn compile
run:
	mvn exec:java -Dexec.mainClass=projet.SinglePoint
clean:
	mvn clean


# POUR RAYAN
SinglePoint:
	~/nvim.appimage src/main/java/projet/SinglePoint.java
tree:
	~/nvim.appimage src/main/java/projet/RTree.java
