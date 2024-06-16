
# DAD - proj

https://hub.docker.com/r/busuiocstefania/c01
https://hub.docker.com/r/busuiocstefania/c02
https://hub.docker.com/r/busuiocstefania/c03


for activemq session: docker run -d --name activemq --network forshare -p 61616:61616 -p 8161:8161 rmohr/activemq

C01:
export PATH="/opt/software/jdk-21/bin:$PATH"
java -jar target/c01-0.0.1.jar

C02:
export PATH="/opt/software/jdk-21/bin:$PATH"
java -jar target/c02-0.0.1.jar

C03:
export PATH="/opt/software/jdk-21/bin:$PATH"
java -jar target/c03-0.0.1.jar