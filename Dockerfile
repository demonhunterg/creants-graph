FROM openjdk:8-jre

EXPOSE 9393

ADD ./build/libs /tmp/creants-graph-2x
WORKDIR "/tmp/creants-graph-2x"

ENTRYPOINT ["java", "-Xms128m", "-Xmx128m", "-cp", "creants-graph-2x.jar:lib/*", "-Dfile.encoding=UTF-8", "com.creants.graph.CreantsGraphApplication"]