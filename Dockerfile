FROM alpine

COPY ./CMSClient /usr/local/CMSClient

RUN apk update && apk add openjdk8 && apk add apache-ant

# Set environment
ENV JAVA_HOME /opt/jdk

ENV PATH ${PATH}:${JAVA_HOME}/bin

WORKDIR /usr/local/CMSClient

CMD [ "ant" ]

# Start the image with the jar file as the entrypoint

ENTRYPOINT ["java", "-jar", "./dist/CMSClient.jar"]

# EOF