package br.com.spark

import org.apache.spark.SparkContext
import org.apache.log4j._

/** Compute the average number of friends by age in a social network. */
object FriendsByAge {

  /** A function that splits a line of input into (age, numFriends) tuples. */
  def parseLine(line: String) = {
    // Split by commas
    val fields = line.split(",")
    // Extract the age and numFriends fields, and convert to integers
    val age = fields(2).toInt
    val numFriends = fields(3).toInt
    // Create a tuple that is our result.
    (age, numFriends)
  }

  def parseLineFirstname(line:String) ={
    val fields = line.split(",")
    val name = fields(1)
    val numFriends = fields(3).toInt

    (name, numFriends)
  }

  def main(args: Array[String]): Unit = {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "FriendsByAge")

    // Load each line of the source data into an RDD
    val lines = sc.textFile("../source/fakefriends.csv")

    // Use our parseLines function to convert to (age, numFriends) tuples
    val rdd = lines.map(parseLineFirstname)

    // here we'll have a tuplas, like
    // (name, numfriends) => (nome, (numfriends, 1)
    // ('ben', 311) => ('ben', (311, 1))
    // ('will', 300) => ('will', (300, 1))
    // ('ben', 2) => ('ben', (2, 1))
    val tuplas = rdd.mapValues(x => (x, 1))

    // add up all values for eache unique key
    // ('ben', 313) => ('ben', (313, 2))
    val reduce = tuplas.reduceByKey( (x,y) => (x._1 + y._1, x._2 + y._2))


    // So now we have tuples of (age, (totalFriends, totalInstances))
    // To compute the average we divide totalFriends / totalInstances for each age.
    val averagesByAge = reduce.mapValues(x => x._1 / x._2)

    // Collect the results from the RDD (This kicks off computing the DAG and actually executes the job)
    val results = averagesByAge.collect()

    // Sort and print the final results.
    results.sorted.foreach(println)
  }

  /** Our main function where the action happens */
  def main2(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "FriendsByAge")

    // Load each line of the source data into an RDD
    val lines = sc.textFile("../source/fakefriends.csv")

    // Use our parseLines function to convert to (age, numFriends) tuples
    val rdd = lines.map(parseLine)

    // Lots going on here...
    // We are starting with an RDD of form (age, numFriends) where age is the KEY and numFriends is the VALUE
    // We use mapValues to convert each numFriends value to a tuple of (numFriends, 1)
    // Then we use reduceByKey to sum up the total numFriends and total instances for each age, by
    // adding together all the numFriends values and 1's respectively.
    val totalsByAge = rdd.mapValues(x => (x, 1)).reduceByKey( (x,y) => (x._1 + y._1, x._2 + y._2))

    // So now we have tuples of (age, (totalFriends, totalInstances))
    // To compute the average we divide totalFriends / totalInstances for each age.
    val averagesByAge = totalsByAge.mapValues(x => x._1 / x._2)

    // Collect the results from the RDD (This kicks off computing the DAG and actually executes the job)
    val results = averagesByAge.collect()

    // Sort and print the final results.
    results.sorted.foreach(println)
  }

  //numero médio de amigos pelo primeiro nome
}
