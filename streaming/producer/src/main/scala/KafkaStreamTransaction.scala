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

class produceTranMessages(brokers: String, topic: String, numTransactions : Int, transactionFileName: String) extends Actor {

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
      var transaction = 0
      val messages = for (line <- Source.fromFile(transactionFileName).getLines().drop(1) if transaction <= numTransactions.toInt )yield {
        val cols = line.split(",").map(_.trim)
        transaction += 1
        // grab the first column from the csv file holding the transaction id
        val account_no = {
          cols(0).toString()
        }
        val tranpostdt = {
	    event_time.toString()
//          cols(1).toString()
        }
        val tranid = {
          cols(2).toString()
        }
        val amount = {
          cols(3).toString()
        }
        val bucket = {
          cols(4).toString()
        }
        val cardnum = {
          cols(5).toString()
        }
        val tranamt = {
          cols(6).toString()
        }
        val trancd = {
          cols(7).toString()
        }
        val trandescription = {
          cols(8).toString()
        }
        val transrsncd  = {
          cols(9).toString()
        }
        val transrsndesc  = {
          cols(10).toString()
        }
        val transrsntype  = {
          cols(11).toString()
        }
        val transtat = {
          cols(12).toString()
        }
        val trantype = {
          cols(13).toString()
        }
        val str = s"${account_no};${tranpostdt};${tranid};${amount};${bucket};${cardnum};${tranamt};${trancd};${trandescription};${transrsncd};${transrsndesc};${transrsntype};${transtat};${trantype}"
        if (transaction < 5)
          println(str)
        new KeyedMessage[String, String](topic, str)
      }
      kafka.producer.send(messages.toSeq: _*)
    }

    case _ => println("Not a valid message!")
  }
}


// Produces some random words between 1 and 100.
object KafkaStreamTranProducer extends App {

  /*
   * Get runtime properties from application.conf
   */

  val kafkaHost = "localhost:9092"
  println(s"kafkaHost $kafkaHost")
  val kafkaTopic = "transaction"
  println(s"kafkaTopic $kafkaTopic")
  val numTransactions = 100
  println(s"numTransactions $numTransactions")
  val numRecords = 28800
  println(s"numRecords $numRecords")
  val waitMillis = 10000
  println(s"waitMillis $waitMillis")
  val transactionFileName = "./producer/data/transaction.csv"
  println(s"transactionFileName $transactionFileName")

  /*
   * Set up the Akka Actor
   */
  val system = ActorSystem("KafkaStreamTransaction")
  val messageActor = system.actorOf(Props(new produceTranMessages(kafkaHost, kafkaTopic, numTransactions,transactionFileName)), name="genMessages")

  /*
   * Message Loop
   */
  var numRecsWritten = 0
  while(true) {
    messageActor ! "send"

    numRecsWritten += numTransactions

    println(s"${numRecsWritten} records written.")

    Thread sleep waitMillis
  }

}

