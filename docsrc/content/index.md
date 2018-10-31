---
title: "Fraud Structured Streaming Proofshop"
weight: 100
type: index
---

This is a guide for how to use the Fraud Structured Streaming Proofshop brought to you by the DataStax field team.

### Motivation

The Fraud Structured Streaming Proofshop is a targeted event to prove structured streaming capability within DSE and demonstrate that prospect can use this for their own use cases.  


### What is included?

A Powerpoint presentation will walk the prospect through DSE Spark Structured Streaming and the use case. 

As part of this proofshop, a structured streaming job pulls transaction load off a kafka queue.  These rows are writting to a transaction table but additionally, the transaction stream is joined realtime to a customer change table to see if any of the new transactions are from customers that recently changed their account.

This Fraud Structured Streaming proofshop demo has an API call to create a row in a cust_change table.  In a production fraud application, logic would be needed to determine which customer field changes (email address, phone number, mailing address).  For this demo, the API call sets a row in a cust_change table to represent a change in the customer table.  

When a row in the cust_change table successfully joins to the transaction, a row is written to the cust_fraud table as a flag to be investigated by the bank.

### Business Take Aways

DataStax enables immediate, real-time fraud detection. This Fraud Structured Streaming proofshop demonstrates this real time fraud detection using the following scenario:  

    Detect transactions occuring within 24 hours of a customer profile change for further fraud personnel analysis.

 (in real time!)


DataStax-powered solutions deliver a highly personalized, responsive, and consistent experience whatever the channel, location, or volume of customers and transactions. Customers will have an engaging experience that drives customer satisfaction and advocacy, which translates to increased brand loyalty and revenue growth.

### Technical Take Aways

Understand how spark structured streaming allows real-time decision making in an easy-to-create and easy-to-maintain Spark Dataset environment.  Paralleling the transition from RDDs to Datasets, streaming has gone from complex DStreams to easy-to-use Structured Streaming.  The combination of structured streaming with DataStax allows joining streams and cassandra tables to do real-time analytics.  Key technical note:  the stream receives a refreshed copy of any joined cassandra tables on each stream window refresh.

Additional note:  This demo will struggle on the default instance type of m3.xlarge.  Instead, use the m3.2xlarge.
