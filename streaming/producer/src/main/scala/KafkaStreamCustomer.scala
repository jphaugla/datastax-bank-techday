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

class produceCustMessages(brokers: String, topic: String, numCustomers : Int, customerFileName: String) extends Actor {

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
      var customer = 0
      val messages = for (line <- Source.fromFile(customerFileName).getLines().drop(1) if customer <= numCustomers.toInt )yield {
        val cols = line.split(",").map(_.trim)
        customer += 1
        // grab the first column from the csv file holding the customer id
        val customer_id = {
          cols(0).toString()
        }
        val address_line1 = {
          cols(1).toString()
        }
        val address_line2 = {
          cols(2).toString()
        }
        val address_type = {
          cols(3).toString()
        }
        val bill_pay_enrolled = {
          cols(4).toString()
        }
        val city = {
          cols(5).toString()
        }
        val country_code = {
          cols(6).toString()
        }
        val created_by = {
          cols(7).toString()
        }
        val created_datetime  = {
          cols(8).toString()
        }
        val customer_nbr = {
          cols(9).toString()
        }
        val customer_origin_system = {
          cols(10).toString()
        }
        val customer_status = {
          cols(11).toString()
        }
        val customer_type = {
          cols(12).toString()
        }
        val date_of_birth = {
          cols(13).toString()
        }
        val email_address = {
          cols(14).toString()
        }
        val first_name = {
          cols(15).toString()
        }
        val gender = {
          cols(16).toString()
        }
        val government_id = {
          cols(17).toString()
        }
        val government_id_type = {
          cols(18).toString()
        }
        val last_name = {
          cols(19).toString()
        }
        val last_updated = {
          cols(20).toString()
        }
        val last_updated_by = {
          cols(21).toString()
        }
        val middle_name = {
          cols(22).toString()
        }
        val phone_numbers = {
          cols(23).toString()
        }
        val prefix = {
          cols(24).toString()
        }
        val str = s"${customer_id};${address_line1};${address_line2};${address_type};${bill_pay_enrolled};${city};${country_code};${created_by};${created_datetime.toString};${customer_nbr};${customer_origin_system};${customer_status};${customer_type};${date_of_birth};${email_address};${first_name};${gender};${government_id};${government_id_type};${last_name};${last_updated.toString};${last_updated_by};${middle_name};${phone_numbers};${prefix}"
        if (customer < 5)
          println(str)
        new KeyedMessage[String, String](topic, str)
      }
      kafka.producer.send(messages.toSeq: _*)
    }

    case _ => println("Not a valid message!")
  }
}


// Produces some random words between 1 and 100.
object KafkaStreamCustomerProducer extends App {

  /*
   * Get runtime properties from application.conf
   */

  val kafkaHost = "localhost:9092"
  println(s"kafkaHost $kafkaHost")
  val kafkaTopic = "customer"
  println(s"kafkaTopic $kafkaTopic")
  val numCustomers = 100
  println(s"numCustomers $numCustomers")
  val numRecords = 28800
  println(s"numRecords $numRecords")
  val waitMillis = 10000
  println(s"waitMillis $waitMillis")
  val customerFileName = "./producer/data/customer.csv"
  println(s"customerFileName $customerFileName")

  /*
   * Set up the Akka Actor
   */
  val system = ActorSystem("KafkaStreamCustomer")
  val messageActor = system.actorOf(Props(new produceCustMessages(kafkaHost, kafkaTopic, numCustomers,customerFileName)), name="genMessages")

  /*
   * Message Loop
   */
  var numRecsWritten = 0
  while(numRecsWritten < numRecords) {
    messageActor ! "send"

    numRecsWritten += numCustomers

    println(s"${numRecsWritten} records written.")

    Thread sleep waitMillis
  }

}

