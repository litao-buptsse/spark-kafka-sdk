# Spark Kafka SDK - Simplify Your Kafka And Spark Process

---


## Requirements

* JDK-1.7

## Building


### 1. docker build

```
$ cd docker; make
```

* docker image: registry.docker.dev.sogou-inc.com:5000/clouddev/spark-kafka-sdk:1.0

### 2. release tgz

```
$ bin/release-tgz.sh
```

* tgz location: dist/spark-kafka-sdk_2.10.4-1.0.tgz


## Running

### 1. docker_run.sh

```
$ mkdir app; cp docker/docker_run.sh app/; cp conf/* app/
$ cd app/; ./docker_run.sh
```

### 2. bin/start.sh

```
$ tar -xzvf spark-kafka-sdk-scala_2.10.4-1.0.tgz
$ cd spark-kafka-sdk-scala_2.10.4-1.0; ./run.sh <input>
```

## Configuraion

* config file location: conf/application.conf

| Param | Description |
| ------------ | ----------- |
| app.kafkaStreaming.processor.class | kafka streaming processor class |
| app.kafkaConsumer.processor.class | kafka consumer processor class |
| kafka.zookeeperQuorum | kafka zookeeper address |
| kafka.topics | kafka topics |
| kafka.consumerGroup | kafka consumer group |
| kafka.consumerThreadNum | kafka consumer thread num |
| spark.app.name | spark app name |
| spark.streaming.batchDurationSeconds | spark streaming batch duration seconds |


## Spark Streaming Demo

```scala
class SparkStreamingToHbaseDemo extends RDDProcessor {

  override def process(rdd: RDD[String]) = {
    val table = "spark-streaming-to-hbase-demo"

    val wordCount = rdd.map((_, 1L)).reduceByKey(_ + _)
    val toHbase: RDD[(String, Map[String, Map[String, (Long, Long)]])] = wordCount.map(x =>
      (x._1, Map("cf" -> Map("pv" ->(x._2, System.currentTimeMillis)))))

    import unicredit.spark.hbase._

    implicit val config = HBaseConfig()
    implicit val longWriter = new Writes[Long] {
      def write(data: Long) = Bytes.toBytes(data)
    }
    toHbase.toHBase(table)
  }
}
```

## Kafka Consumer Demo

```scala
class KafkaConsumerToConsoleDemo extends FlumeEventProcessor {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def processEvent(event: Event) = {
    val body = new String(event.getBody)
    LOG.info(body)
  }
}
```

## About Spark Streaming

Please refer to the [streaming programming guide](http://spark.apache.org/docs/latest/streaming-programming-guide.html) for more informatioin

## About Spark Hbase Integration

Please refer to the project [hbase-rdd](http://gitlab.dev.sogou-inc.com/sogou-spark/hbase-rdd) for more informatioin