# Spark Kafka SDK - Simplify Your Kafka And Spark Process

---


## Requirements

* JDK-1.7

## Building

```
$ build/sbt assembly
```

* assembly jar location: target/scala-2.10/spark-kafka-sdk-assembly-1.0.jar

## Running

#### 1. read kafka via spark streaming

```
$ spark-submit \
	--master <master url> \
	--class com.sogou.spark.streaming.KafkaStreaming \
	spark-kafka-sdk-assembly-1.0.jar
```

#### 2. read kafka via kafka consumer api

```
$ spark-submit \
	--master <master url> \
	--class com.sogou.kafka.consumer.KafkaConsumer \
	spark-kafka-sdk-assembly-1.0.jar
```

## Configuraion

| Param | Description |
| ------------ | ----------- |
| app.kafka-streaming.processor.class | kafka streaming processor class |
| app.kafka-consumer.processor.class | kafka consumer processor class |
| kafka.zookeeperQuorum | kafka zookeeper address |
| kafka.topics | kafka topics |
| kafka.consumerGroup | kafka consumer group |
| kafka.consumerThreadNum | kafka consumer thread num |
| spark.app.name | spark app name |
| spark.streaming.batchDurationSeconds | spark streaming batch duration seconds |


## Kafka Streaming Demo

#### 1. Extends RDDProcessor trait and implement process method

```scala
class KafkaStreamingToHbaseDemo extends RDDProcessor {

  override def process(rdd: RDD[String]) = {
    val table = "kafka-streaming-to-hbase-demo"

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

#### 2. Update application.conf

```
kafka {
  zookeeperQuorum = "localhost:2181/kafka"
  topics = "mytopic"
  consumerGroup = "mygroup"
  consumerThreadNum = 5
}

spark {
  app.name = "myapp"
  streaming {
    batchDurationSeconds = 10
  }
}

app {
  kafka-consumer {
    processor.class = com.sogou.example.KafkaConsumerToConsoleDemo
  }
  kafka-streaming {
    processor.class = com.sogou.example.KafkaStreamingToHbaseDemo
  }
}
```

#### 3. Running

```
$ spark-submit \
	--master "local[*]" \
	--class com.sogou.spark.streaming.KafkaStreaming \
	spark-kafka-sdk-assembly-1.0.jar
```

## Kafka Consumer Demo

#### 1. Extends KafkaMessageProcessor trait and implement process method (you can also implement KafkaMessageProcessor's child class such as FlumeEventProcessor)

```scala
class KafkaConsumerToConsoleDemo extends FlumeEventProcessor {
  private val LOG = LoggerFactory.getLogger(getClass)

  override def processEvent(event: Event) = {
    val body = new String(event.getBody)
    LOG.info(body)
  }
}
```

#### 2. Update application.conf

```
kafka {
  zookeeperQuorum = "localhost:2181/kafka"
  topics = "mytopic"
  consumerGroup = "mygroup"
  consumerThreadNum = 5
}

spark {
  app.name = "myapp"
  streaming {
    batchDurationSeconds = 10
  }
}

app {
  kafka-consumer {
    processor.class = com.sogou.example.KafkaConsumerToConsoleDemo
  }
  kafka-streaming {
    processor.class = com.sogou.example.KafkaStreamingToHbaseDemo
  }
}
```

#### 3. Running

```
$ spark-submit \
	--master "local[*]" \
	--class com.sogou.kafka.consumer.KafkaConsumer \
	spark-kafka-sdk-assembly-1.0.jar
```

## About Spark Streaming

Please refer to the [streaming programming guide](http://spark.apache.org/docs/latest/streaming-programming-guide.html) for more informatioin

## About Spark Hbase Integration

Please refer to the project [hbase-rdd](http://gitlab.dev.sogou-inc.com/sogou-spark/hbase-rdd) for more informatioin