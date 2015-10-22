FROM registry.docker.dev.sogou-inc.com:5000/clouddev/spark-client-sunshine-nm:1.5

ENV APPROOT /search/app
WORKDIR $APPROOT
ADD spark-kafka-sdk_2.10.4-1.0 $APPROOT

RUN mkdir -p logs
CMD  sh start.sh >logs/spark-kafka-sdk.out 2>&1

VOLUME /search/app/logs
