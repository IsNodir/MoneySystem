FROM openjdk:17-slim
EXPOSE 8080
VOLUME /systemDocker
ADD /build jars
CMD ["java", "-jar" , "/jars/libs/MoneySystem-1.0.0-SNAPSHOT.jar"]
