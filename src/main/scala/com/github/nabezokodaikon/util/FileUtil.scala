package com.github.nabezokodaikon.util

import akka.http.scaladsl.model.{ ContentTypes, HttpCharsets, MediaTypes }
import akka.http.scaladsl.model.ContentType.WithCharset
import com.github.nabezokodaikon.util.Loan.using
import com.typesafe.scalalogging.LazyLogging
import java.io.File
import java.io.FileNotFoundException
import java.io.{ BufferedInputStream, ByteArrayInputStream, FileInputStream, FileOutputStream, IOException, OutputStreamWriter }
import scala.io.Source
import scala.util.control.Exception.catching

object FileUtil extends LazyLogging {

  val enc = "UTF-8"

  def readText(name: String): String = {
    catching(classOf[FileNotFoundException]).either {
      using(Source.fromFile(name, enc)) { src =>
        src.mkString
      }
    } match {
      case Right(text) => text
      case Left(e) =>
        logger.error(e.getMessage)
        "No such file or directory."
    }
  }

  def writeText(name: String, text: String): Unit = {
    catching(classOf[IOException]).either {
      using(new OutputStreamWriter(new FileOutputStream(name, false), enc)) { out =>
        out.write(text)
      }
    } match {
      case Right(_) => ()
      case Left(e) =>
        logger.error(e.getMessage)
    }
  }

  def readBinary(name: String): Array[Byte] = {
    catching(classOf[IOException]).either {
      using(new BufferedInputStream(new FileInputStream(name))) { in =>
        Stream.continually(in.read).takeWhile(!_.equals(-1)).map(_.toByte).toArray
      }
    } match {
      case Right(bin) => bin
      case Left(e) =>
        logger.error(e.getMessage)
        Array[Byte]()
    }
  }

  def writeBinary(name: String, bin: Array[Byte]): Unit = {
    catching(classOf[IOException]).either {
      using(new FileOutputStream(name)) { out =>
        out.write(bin, 0, bin.length)
      }
    } match {
      case Right(_) => ()
      case Left(e) =>
        logger.error(e.getMessage)
    }
  }

  def getCurrentDirectory(): String = {
    new File(".").getAbsoluteFile().getParent()
  }

  def getExtension(file: String): String = {
    file.split('.').last
  }

  def getContentType(file: String): WithCharset = {
    getExtension(file) match {
      case "html" => ContentTypes.`text/html(UTF-8)`
      case "css" => WithCharset(MediaTypes.`text/css`, HttpCharsets.`UTF-8`)
      case "js" => WithCharset(MediaTypes.`application/javascript`, HttpCharsets.`UTF-8`)
      case _ => WithCharset(MediaTypes.`text/plain`, HttpCharsets.`UTF-8`)
    }
  }
}
