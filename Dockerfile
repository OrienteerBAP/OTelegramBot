FROM orienteer/orienteer:latest

RUN mkdir -p /usr/src/OTelegramModule/
WORKDIR /usr/src/OTelegramModule/
ADD . /usr/src/OTelegramModule/
RUN mvn clean install

RUN mv target/OTelegramModule.war /orienteer/
RUN cp orienteer.properties /orienteer/
RUN mvn clean
RUN rm -rf OTelegramModule/

WORKDIR /orienteer/
RUN ln -s -f OTelegramModule.war active.war
