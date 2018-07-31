package com.kafkaToSparkToDSE

/**
  */
import java.sql.Timestamp
import java.text.{DateFormat, SimpleDateFormat}

import com.datastax.driver.core.Session

import collection.JavaConversions._

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.streaming._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.types.{IntegerType,StringType}



object query {


  def main(args: Array[String]) {

    println(s"entered main")

    val sparkJob = new SparkJob()
    try {
      sparkJob.runJob()
    } catch {
      case ex: Exception =>
        println("error in main running spark job")
    }
  }
}

class SparkJob extends Serializable {

  println(s"before build spark session")

  val sparkSession =
    SparkSession.builder
      .appName("queryrun")
      .config("spark.cassandra.connection.host", "localhost")
      .getOrCreate()

  println(s"after build spark session")

  def runJob() = {


/*
    val joined_df = sparkSession.sql ("""  
          select a.customer_id
	  ,count(*) as trans_count 
	  from bank.transaction 
	  inner join a
	  on t_st.account_no=a.account_no
          inner join c_st
	  on c_st.customer_id = a.customer_id
          group by a.customer_id
	 """); 
*/

    val joined_df = sparkSession.sql ("""  
          select account_no
	  ,count(*) as trans_count 
	  from bank.transaction 
          group by account_no
	 """); 
    joined_df.show()
    sparkSession.stop()
  }
}
