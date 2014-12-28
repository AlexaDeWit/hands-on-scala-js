package scalatex

import java.nio.file.Paths

import sbt.Keys._
import sbt._
object SbtPlugin extends sbt.Plugin{
  val scalatexDirectory = taskKey[sbt.File]("Clone stuff from github")
  val mySeq = Seq(
    scalatexDirectory := sourceDirectory.value / "scalatex",
    managedSources ++= {
      val inputDir = scalatexDirectory.value
      val outputDir = sourceManaged.value / "scalatex"
      val inputFiles = (inputDir ** "*.scalatex").get
      println("Generating Scalatex Sources...")
      val outputFiles = for(inFile <- inputFiles) yield {
        val outFile = new sbt.File(
          outputDir.getAbsolutePath + inFile.getAbsolutePath.drop(inputDir.getAbsolutePath.length)
        )
        val name = inFile.getName
        val objectName = name.slice(name.lastIndexOf('/')+1, name.lastIndexOf('.'))
        val pkgName =
          inFile.getAbsolutePath
            .drop(inputDir.getAbsolutePath.length + 1)
            .toString
            .split("/")
            .dropRight(1)
            .map(s => s"package $s")
            .mkString("\n")
        IO.write(
          outFile,
          s"""
            |$pkgName
            |import scalatags.Text.all._
            |
            |object $objectName{
            |  def apply() = scalatex.twf("${inFile.getAbsolutePath}")
            |}
            |
            |${IO.readLines(inFile).map("//"+_).mkString("\n")}
          """.stripMargin
        )
        outFile
      }
      outputFiles
    }
  )
  val scalatexSettings = inConfig(Test)(mySeq) ++ inConfig(Compile)(mySeq) ++ Seq(
    libraryDependencies += "com.lihaoyi" %% "scalatex-api" % "0.1.0",
    watchSources ++= ((scalatexDirectory in Compile).value ** "*.scalatex").get
  )
}
