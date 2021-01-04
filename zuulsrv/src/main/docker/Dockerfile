FROM adoptopenjdk/openjdk11:jdk-11.0.9.1_1-alpine
RUN  apk update && apk upgrade && apk add netcat-openbsd
RUN mkdir -p /usr/local/zuulservice
ADD @project.build.finalName@.jar /usr/local/zuulservice/
ADD run.sh run.sh
RUN chmod +x run.sh
CMD ./run.sh
