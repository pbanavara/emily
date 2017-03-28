package co.axoni.processemail

import java.io.{InputStream, FileWriter, BufferedWriter}

import co.axoni.classify.SvmPredict
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by pbanavara on 07/12/15.
  */
object ClassifyEmail {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  def NEWgenerateModelFromTrainingData(listOfTrainingFileNames: String) {
    val stream: InputStream = getClass.getResourceAsStream("/ml/data/regexpsForConfirmNotconfirm.txt")
    val regexpList = scala.io.Source.fromInputStream(stream).mkString.split("\n")
    val writer = new BufferedWriter(new FileWriter("training.txt"))
    scala.io.Source.fromFile(listOfTrainingFileNames).getLines().foreach(
      trainingFile => {
        val trainingData = scala.io.Source.fromFile(trainingFile).mkString.toLowerCase
        val featureVector = generateFeatureVector(trainingData)
        writer.write(new StringBuilder().append(featureVector.mkString(" ")).append("\n").toString())
      }
    )
    writer.flush()
    writer.close()
  }

  def generateModelFromTrainingData(listOfTrainingFileNames: String) {
    val regexpStream: InputStream = getClass.getResourceAsStream("/ml/data/regexpsForConfirmNotconfirm.txt")
    val regexpList = scala.io.Source.fromInputStream(regexpStream).mkString.split("\n")

    val trainingDataFeatureVectors = new BufferedWriter(new FileWriter("/src/main/resources/ml/data/trainingFVForConfirmNotconfirm.txt"))
    // val writer = new BufferedWriter(new FileWriter("training.txt"))

    scala.io.Source.fromFile(listOfTrainingFileNames).getLines().foreach(
      trainingFile => {
        val trainingData = scala.io.Source.fromFile(trainingFile).mkString.toLowerCase
        val featureVector: Array[String] = new Array[String](regexpList.size+1)
        if (trainingFile.contains("not")) featureVector(0) = "0"
        else featureVector(0) = "1"
        regexpList.foreach(ex => {
          val regexp = ex.r
          val res = regexp findFirstIn trainingData
          if (res != None) {
            val indexOfRegExp: Int = regexpList.indexOf(ex)
            logger.debug("Filname :::%s".format(trainingFile))
            logger.debug(trainingData)
            logger.debug(indexOfRegExp.toString)
            featureVector(indexOfRegExp+1) = new StringBuilder().append((indexOfRegExp+1).toString).append(":1").toString()
          } else {
            val indexOfRegExp: Int = regexpList.indexOf(ex)
            featureVector(indexOfRegExp+1) = new StringBuilder().append((indexOfRegExp+1).toString).append(":0").toString()
          }
        })
        trainingDataFeatureVectors.write(new StringBuilder().append(featureVector.mkString(" ")).append("\n").toString())
        //logger.debug(featureVector.mkString(" "))
      }
    )
    trainingDataFeatureVectors.flush()
    trainingDataFeatureVectors.close()
  }

  def generateFeatureVector(input: String) : Array[String] = {
    val stream : InputStream = getClass.getResourceAsStream("/ml/data/regexpsForConfirmNotconfirm.txt")
    val regexpList = scala.io.Source.fromInputStream(stream).mkString.split("\n")
    // val regexpList = scala.io.Source.fromFile("regexpsForConfirmNotconfirm.txt").mkString.split("\n")
    var featureVector: Array[String] = new Array[String](regexpList.size+1)
    featureVector(0) = "4"
    regexpList.foreach(ex => {
      val regexp = ex.r
      val res = regexp findFirstIn(input)
      if (res != None) {
        val indexOfRegExp: Int = regexpList.indexOf(ex)
        featureVector(indexOfRegExp+1) = new StringBuilder().append((indexOfRegExp+1).toString).append(":1").toString()
      } else {
        val indexOfRegExp: Int = regexpList.indexOf(ex)
        featureVector(indexOfRegExp+1) = new StringBuilder().append((indexOfRegExp+1).toString).append(":0").toString()
      }
    })
    logger.debug(featureVector.mkString(" "))
    return featureVector
  }

  def classifyEmails(inputStr: String): Boolean = {
    val featureVector = generateFeatureVector(inputStr.toLowerCase())
    // val modelFileName = "emily.model"
    val modelFileName = getClass.getResource("/ml/model/emily.model")
    logger.debug("Input: %s".format(inputStr))
    // val prediction = SvmPredict.predictEmail(featureVector.mkString(" "), modelFileName)
    val prediction = SvmPredict.predictEmail(featureVector.mkString(" "), modelFileName.getFile)
    logger.debug("From SVM Classifier: Confirmed:%s".format(prediction))
    if (prediction.stripLineEnd == "1.0") {
      return true
    }
    else {
      return false
    }
  }

  /*
  def main(args : Array[String]): Unit = {
    // classifyEmails("""Let's meet at coffee shop inside Leela - it's called Citrus""")
    generateModelFromTrainingData("trainEmailsList.txt")
  }
*/
}
