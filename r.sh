(echo "#!/bin/java -jar" ; cat ./mapedit.jar) > ./mapedit.jar.tmp
mv ./mapedit.jar.tmp ./mapedit.jar
chmod a+rx ./mapedit.jar