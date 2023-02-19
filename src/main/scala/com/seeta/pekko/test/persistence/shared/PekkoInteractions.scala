package com.seeta.pekko.test.persistence.shared

import com.seeta.pekko.test.PrintHelpers.showInConsole
import com.seeta.pekko.test.{ Command, DeleteAllTweets, GetAllTweetsFromPekko, Response, Tweets }
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import org.apache.pekko.actor.typed.{ Behavior, ActorSystem => PekkoActorSystem }
import org.apache.pekko.util.{ Timeout => PekkoTimeout }

import scala.concurrent.duration._

trait PekkoInteractions {
  private def askPekkoActorForTweets(
      actor: org.apache.pekko.actor.typed.ActorRef[Command]
  )(implicit timeout: PekkoTimeout, system: PekkoActorSystem[Command]): Future[Response] = {
    import org.apache.pekko.actor.typed.scaladsl.AskPattern._
    actor.ask(ref => GetAllTweetsFromPekko(ref))
  }

  protected def checkTweetsInPekkoActor(
      expectedCount: Int,
      behavior: Behavior[Command],
      name: String,
      configName: String
  ): Unit = {
    val config                                     = ConfigFactory.load(configName)
    implicit val system: PekkoActorSystem[Command] = PekkoActorSystem(behavior, name, config)
    implicit val ec: ExecutionContext              = system.executionContext
    implicit val timeout: PekkoTimeout             = PekkoTimeout(1.second)

    askPekkoActorForTweets(system).onComplete {
      case Success(Tweets(tweets, totalEvents)) =>
        if (expectedCount == tweets.size) {
          showInConsole(
            s"""Successful: 
               |           Total tweets are equal to expected number of tweets. 
               |           Expected tweets are $expectedCount. 
               |           Total events are $totalEvents
               |""".stripMargin,
            attentionGrabbing = true
          )
          system ! DeleteAllTweets
        } else {
          showInConsole(
            s"""
               |ERROR: 
               |      Total tweets received from the pekko actor: '${tweets.size}'. Total events = $totalEvents
               |       Expected messages are = $expectedCount. Maybe passed expected count is wrong :)
               |       Something went wrong have a look
               |""".stripMargin,
            attentionGrabbing = true
          )
        }
        system.terminate()
      case Failure(exception) =>
        system.log.error("Error", exception)
        system ! DeleteAllTweets
        system.terminate()
    }
  }
}
