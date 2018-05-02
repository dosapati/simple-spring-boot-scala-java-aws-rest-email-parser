package com.powerup.rest

import java.io.{InputStream, StringWriter}
import java.util
import java.util.List
import java.util.concurrent.atomic.AtomicLong

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.{S3Object, S3ObjectInputStream}
import org.springframework.web.bind.annotation._
import com.google.gson.Gson
import hello.Greeting
import org.apache.commons.io.IOUtils
import org.apache.james.mime4j.codec.DecodeMonitor
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder
import org.apache.james.mime4j.parser.{ContentHandler, MimeStreamParser}
import org.apache.james.mime4j.stream.{BodyDescriptorBuilder, MimeConfig}
import tech.blueglacier.email.{Attachment, Email}
import tech.blueglacier.parser.CustomContentHandler

import scala.collection.JavaConversions._

/**
  * Created by dosapati on 5/1/18.
  */
@RestController
@RequestMapping(path=Array("/v1/api"))
object MailController {

  @GetMapping (path=Array("/ping"))
  def ping() : String = {

    "I'm running :--)))"
  }

  @PostMapping (path=Array("/jsonPost"))
  def processMail(@RequestBody input:String) : String = {

    println("calling request body -->",input)
    val map = new Gson().fromJson(input, classOf[java.util.Map[String, Object]])

    println("input is -->",map)

    "I'm running with "+input+" :-)) "
  }

  private val counter = new AtomicLong
  val gson = new Gson

  @RequestMapping(Array("/processMail")) def greeting(@RequestParam(value = "bucket", defaultValue = "~~~") bucket: String, @RequestParam(value = "key", defaultValue = "~~~") key: String): Greeting = {
    val retObj: Greeting = new Greeting
    retObj.setId(counter.incrementAndGet + 9218)
    val s3: AmazonS3 = AmazonS3ClientBuilder.defaultClient
    try {
      val o: S3Object = s3.getObject(bucket, key)
      val s3is: S3ObjectInputStream = o.getObjectContent
      processEmail(bucket, key, retObj, s3is)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    retObj
  }


  def processEmail(bucket: String, key: String, retObj: Greeting, s3is: InputStream) = {
    val contentHandler: ContentHandler = new CustomContentHandler
    val mime4jParserConfig: MimeConfig = MimeConfig.DEFAULT
    val bodyDescriptorBuilder: BodyDescriptorBuilder = new DefaultBodyDescriptorBuilder
    val mime4jParser: MimeStreamParser = new MimeStreamParser(mime4jParserConfig, DecodeMonitor.SILENT, bodyDescriptorBuilder)
    mime4jParser.setContentDecoding(true)
    mime4jParser.setContentHandler(contentHandler)
    mime4jParser.parse(s3is)
    val email: Email = contentHandler.asInstanceOf[CustomContentHandler].getEmail
    val attachments: util.List[Attachment] = email.getAttachments
    val calendar: Attachment = email.getCalendarBody
    val htmlBody: Attachment = email.getHTMLEmailBody
    val plainText: Attachment = email.getPlainTextEmailBody
    val to: String = email.getToEmailHeaderValue
    val cc: String = email.getCCEmailHeaderValue
    val from: String = email.getFromEmailHeaderValue
    val writer: StringWriter = new StringWriter
    IOUtils.copy(email.getPlainTextEmailBody.getIs, writer, "UTF-8")
    val theString: String = writer.toString
    //String theString = IOUtils.toString(inputStream, encoding);
    //StringBuilder retContent = new StringBuilder();
    //email.getHeader()
    email.getHeader.forEach(r => {
      println(r.getName," ~~~ ",r.getBody)
    })
    val retContent: StringBuilder = new StringBuilder
    retContent.append("bucket ->" + bucket).append("~~~key->" + key)
    retContent.append("~~~to->" + to).append("~~~from->" + from).append("~~~subject->" + email.getEmailSubject).append("~~~plainText\n->" + theString)
      .append("headers -->" + gson.toJson(email.getHeader))
    println("Content is ->",retContent)
    retObj.setContent(retContent.toString)
  }
}
