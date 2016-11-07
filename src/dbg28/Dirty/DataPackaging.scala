package dbg28.Dirty

import dbg28.{Graph, InputVerification, Point}

import scala.util.matching.Regex


/**
  * EECS 293
  * Created by Daniel on 11/6/2016.
  * dbg28@case.edu
  *
  * Accepts user input in List format, converts it into Graphs and Points\
  * On the untrusted side of the barricade
  */
object DataPackaging {
  val graphPattern: Regex = "([A-Z])".r
  val rowOrColumnPattern: Regex = "(\\d)".r

  /**
    * Accepts user input, converts it to a usable form and sends it to be Verified
    * @param input user input in List of Strings form
    */
  def processInput(input: List[String]): Unit = {
    val dimensions = parseDimensions(input); val rows  = dimensions.head; val columns = dimensions(1)
    InputVerification.setDimensions(rows, columns)
    val graphStrings = partitionInputIntoGraphStrings(input, rows)
    val graphs = stringsToGraphs(graphStrings dropRight 1)
    InputVerification.addGraphs(graphs)
    val layeredGraphs = stringListToLayeredGraph(graphStrings.last)
    InputVerification.addLayeredGraph(layeredGraphs)
    InputVerification.verify()

    println(graphs.mkString("\n"))
    println(layeredGraphs)
  }

  /**
    * Converts a list of list of Strings to a sorted list of Graphs
    * @param allStrings the Strings to convert
    * @return List of Graphs, sorted by Letter
    */
  private def stringsToGraphs(allStrings: List[List[String]]): List[Graph] =  {
     // Nested method that extracts character of a List[String] that is a Graph
     def getCharOfStringList(list: CharSequence): Char =  graphPattern.findFirstIn(list).getOrElse("").head
    allStrings.map(f => {
      val char = getCharOfStringList(f.flatten.mkString)
      stringListToGraph(f, char)
    }).sorted
  }


  /**
    * Converts a single List of lines of text to a Graph
    * @param list the List to convert
    * @return Graph representation of input
    */
  private def stringListToGraph(list: List[String], charExpected: Char): Graph = {
    def isCapital(char: Char): Boolean =  graphPattern.findFirstIn(char.toString).isDefined && (char equals charExpected)
    val points = for( sIndex <- list.indices; charInd <- list(sIndex).indices if isCapital(list(sIndex)(charInd))) yield Point(sIndex, charInd)
    val char: Character = list(points.head.x)(points.head.y)
    Graph(char, points.toSet)
  }

  /**
    *  Takes the total user input and partitions it into input for each graph
    * @param input the total input from user
    * @param rows amount of rows each graph should contain
    * @return a List of Lists of Strings, where each sublist is all the strings in one graph
    */
  private def partitionInputIntoGraphStrings(input: List[String], rows: String):  List[List[String]] = {
    val inputWithoutDimensions = input diff List(input.head, input(1))
    val rowInt: Int = rows.toInt // maybe try catch this
    inputWithoutDimensions.sliding(rowInt, rowInt + 1).toList // check toInt failures
  }

  /**
    * Parses the List form of the user input to identify
    *
    * @param toCheck the UserInput list to find dimensions in
    * @return the Strings containing the graph dimensions
    */
  private def parseDimensions(toCheck: List[String]): List[String] = {
    val rows = rowOrColumnPattern.findFirstMatchIn(toCheck.head)
    val columns = rowOrColumnPattern.findFirstMatchIn(toCheck(1))
    if(rows.isEmpty | columns.isEmpty){
      throw new IllegalArgumentException("First two lines do not contain rows and columns")
    }
    else{
      List(rows.get.group(0), columns.get.group(0))
    }
  }


  /**
    *  Converts a List of Strings containing multiple picture types into a List of Graphs of those points
    * @param mergedString the input List of Strings
    * @return the Graphs representing the input Strings
    */
  private def stringListToLayeredGraph(mergedString: List[String]): List[Graph] = {
    def isCapital(char: Char): Boolean =  graphPattern.findFirstIn(char.toString).isDefined
    var listOfGraphs: List[Graph] = mergedString.flatten.filter(isCapital).map(Graph(_, Set.empty[Point])).distinct.sorted
    listOfGraphs = listOfGraphs.map(f => stringListToGraph(mergedString, f.char))
    listOfGraphs
  }
}
