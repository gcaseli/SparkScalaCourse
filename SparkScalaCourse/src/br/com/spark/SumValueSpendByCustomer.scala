package br.com.spark

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext

object SumValueSpendByCustomer {

  //convert data to (CustomerId, amountSpent) tuples
  def parseLine(line:String) = {
    val fields = line.split(",")
    val customerId = fields(0).toInt
    val valueSpent = fields(2).toFloat
    (customerId, valueSpent)
  }

  def main(args: Array[String]): Unit = {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using the local machine
    val sc = new SparkContext("local", "SumValueSpendByCustomer")

    // Load each line of my book into an RDD
    val input = sc.textFile("../source/customer-orders.csv")

    val parsedLines = input.map(parseLine)

    // Reduce by customerId adding the value spent
    val sumOfSpentValues = parsedLines.reduceByKey( (x,y) =>  x+y)

    // Flip (customerId, amount) tuples to (amount, customerId) and then sort by key (the amount)
    val spentAmountSorted = sumOfSpentValues.map( x => (x._2, x._1) ).sortByKey()

    /*
    // Collect, format, and print the results
    val results = spentAmountSorted.collect()

    for (result <- results) {
      val customerId = result._1
      val valueSpent = result._2
      val formattedSum = f"$valueSpent%.1f"

      println(s"$customerId spent total value: $formattedSum")
    }*/

    for (result <- spentAmountSorted) {
      val customerId = result._2
      val valueSpent = result._1
      val formattedSum = f"$valueSpent%.1f"

      println(s"$customerId spent total value: $formattedSum")
    }
  }

}
