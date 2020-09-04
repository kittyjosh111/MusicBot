FROM openjdk:16-jdk-alpine

WORKDIR /usr/src/app

COPY ./target/JMusicBot-Snapshot-All.jar .

CMD java -jar -Dnogui=true JMusicBot-Snapshot-All.jar