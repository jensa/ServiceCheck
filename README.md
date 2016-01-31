# ServiceCheck
Check up on services!

#Build
##Package frontend:
npm install

gulp

##Compile & run server:
mvn clean package

java -jar target/servicecheck-1.0-SNAPSHOT-fat.jar -conf appconf.json
