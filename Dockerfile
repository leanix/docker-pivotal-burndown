FROM leanix/java-newrelic:8-jre

COPY target/pivotal-burndown.jar /
COPY run.sh /

RUN chmod +x /run.sh

WORKDIR /
CMD ["./run.sh"]

