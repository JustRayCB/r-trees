main:
	mvn compile
run:
	mvn exec:java -Dexec.mainClass=projet.SinglePoint
clean:
	mvn clean
SinglePoint:
	~/nvim.appimage src/main/java/projet/SinglePoint.java
