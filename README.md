# ServiceCheck
Check up on services!

#build
Package Frontend:
npm install
gulp

compile & run server:
mvn clean package
java -jar target/servicecheck-1.0-SNAPSHOT-fat.jar -conf appconf.json
