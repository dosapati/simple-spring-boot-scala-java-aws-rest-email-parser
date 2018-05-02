package com.powerup

/**
  * Created by dosapati on 5/1/18.
  */


import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.boot.SpringApplication

@Configuration
@EnableAutoConfiguration
@ComponentScan(Array("hello","com.powerup")) //({"hello","com.powerup"})
class SampleConfig


object MailApplication extends App {
  println("Running Mail Application ..... ")
  SpringApplication.run(classOf[SampleConfig]);
}
