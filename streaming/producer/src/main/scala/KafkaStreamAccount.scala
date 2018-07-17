/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Modified by jasonhaugland on 10/20/16.
 */
import java.sql.Timestamp
import java.util.Properties

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import scala.io.Source

class produceMessages(brokers: String, topic: String, numAccounts : Int, accountFileName: String) extends Actor {

  val sigma = 1
  val xbar = 0


  object kafka {
    val producer = {
      val props = new Properties()
      props.put("metadata.broker.list", brokers)
      props.put("serializer.class", "kafka.serializer.StringEncoder")

      val config = new ProducerConfig(props)
      new Producer[String, String](config)
    }
  }

  def receive = {
    case "send" => {
      val threshold = scala.util.Random.nextGaussian();
      val event_time = new Timestamp(System.currentTimeMillis())
      val epoch_hr = (event_time.getTime/3600000).toString
      var account = 0
      val messages = for (line <- Source.fromFile(accountFileName).getLines().drop(1) if account <= numAccounts.toInt )yield {
        val cols = line.split(",").map(_.trim)
        account += 1
        // grab the first column from the csv file holding the account id
        val customer_id = {
          cols(0).toString()
        }
        val account_no = {
          cols(1).toString()
        }
        val depth = (scala.util.Random.nextGaussian() * sigma + xbar)
        val metric = (scala.util.Random.nextGaussian() * sigma + xbar)
        val str = s"${customer_id};${account_no};${epoch_hr};${event_time.toString};${depth.toString};${metric.toString}"
        if (account < 5)
          println(str)
        new KeyedMessage[String, String](topic, str)
      }
      kafka.producer.send(messages.toSeq: _*)
    }

    case _ => println("Not a valid message!")
  }
}

// Produces some random words between 1 and 100.
object KafkaStreamAccountProducer extends App {

  /*
   * Get runtime properties from application.conf
   */

  val kafkaHost = "localhost:9092"
  println(s"kafkaHost $kafkaHost")
  val kafkaTopic = "account"
  println(s"kafkaTopic $kafkaTopic")
  val numAccounts = 100
  println(s"numAccounts $numAccounts")
  val numRecords = 28800
  println(s"numRecords $numRecords")
  val waitMillis = 10000
  println(s"waitMillis $waitMillis")
  val accountFileName = "./producer/data/account.csv"
  println(s"accountFileName $accountFileName")

  /*
   * Set up the Akka Actor
   */
  val system = ActorSystem("KafkaStreamAccount")
  val messageActor = system.actorOf(Props(new produceMessages(kafkaHost, kafkaTopic, numAccounts,accountFileName)), name="genMessages")

  /*
   * Message Loop
   */
  var numRecsWritten = 0
  while(numRecsWritten < numRecords) {
    messageActor ! "send"

    numRecsWritten += numAccounts

    println(s"${numRecsWritten} records written.")

    Thread sleep waitMillis
  }

}

