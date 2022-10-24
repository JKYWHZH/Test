#FROM java:8-jre-alpine
FROM openjdk:11-jre
MAINTAINER JKYWHZH
COPY Test-0.0.1-SNAPSHOT.jar /usr/local/goHome/
RUN cd / && mkdir logs && chmod +x /usr/local
EXPOSE 10001
ENTRYPOINT ["nohup","java","-jar","/usr/local/goHome/Test-0.0.1-SNAPSHOT.jar" , ">/logs/gohome.log 2>&1 &"]