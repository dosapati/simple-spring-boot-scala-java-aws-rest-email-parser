package com.powerup.mail

import java.io.{File, FileInputStream, InputStream}

import com.powerup.rest.MailController
import hello.Greeting

object TestProcessEmail extends App{

  print("Test Process Email...")

  val fis = new FileInputStream(new File("/Users/dosapati/Developer/samples/gs-rest-service/complete/test_data/9bjkmv1jc7dcb1hjkrj02qktidp2rfrv43c4kt01.eml"))

  MailController.processEmail("TEST_BUCKET","TEST_KEY",new Greeting(),fis)

}
