package com.seeta.pekko.test.persistence.leveldb

import akka.actor.typed.ActorSystem
import akka.util.Timeout
import com.seeta.pekko.test.PrintHelpers.showInConsole
import com.seeta.pekko.test._
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.{ ActorSystem => PekkoActorSystem }
import org.apache.pekko.util.{ Timeout => PekkoTimeout }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

object LevelDBMain {

  private def sendTweet(actor: akka.actor.typed.ActorRef[Command], message: String): Unit = actor ! AddTweet(message)

  private def askAkkaActorForTweets(
      actor: akka.actor.typed.ActorRef[Command]
  )(implicit timeout: Timeout, system: ActorSystem[Command]): Future[Response] = {
    import akka.actor.typed.scaladsl.AskPattern._
    actor.ask(ref => GetAllTweetsFromAkka(ref))
  }

  private def askPekkoActorForTweets(
      actor: org.apache.pekko.actor.typed.ActorRef[Command]
  )(implicit timeout: PekkoTimeout, system: PekkoActorSystem[Command]): Future[Response] = {
    import org.apache.pekko.actor.typed.scaladsl.AskPattern._
    actor.ask(ref => GetAllTweetsFromPekko(ref))
  }

  private def sendTweetsToAkkaActor(): Unit = {
    val config                                = ConfigFactory.load("application-akka.conf")
    implicit val system: ActorSystem[Command] = ActorSystem(AkkaLevelDB.behavior(), "leveldb-akka-actor", config)
    implicit val ec: ExecutionContext         = system.executionContext
    implicit val timeout: Timeout             = Timeout(1.second)

    (1 to 10).foreach(i => sendTweet(system, s"Tweet number = $i"))

    askAkkaActorForTweets(system).onComplete {
      case Success(Tweets(tweets, totalEvents)) =>
        showInConsole(
          s"""
             |Total tweets received from the akka actor: '${tweets.size}'.
             |      total events: $totalEvents
             |""".stripMargin,
          attentionGrabbing = true
        )
        system.terminate()
      case Failure(exception) =>
        exception.printStackTrace()
        system.terminate()
    }
  }

  private def checkTweetsInPekkoActor(expectedCount: Int): Unit = {
    val config = ConfigFactory.load("application-pekko.conf")
    implicit val system: PekkoActorSystem[Command] =
      PekkoActorSystem(PekkoLevelDB.behavior(), "leveldb-pekko-actor", config)
    implicit val ec: ExecutionContext  = system.executionContext
    implicit val timeout: PekkoTimeout = PekkoTimeout(1.second)

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

  def main(args: Array[String]): Unit = {
    args match {
      case Array("akka") => sendTweetsToAkkaActor()
      case Array("pekko", input) =>
        Try(input.toInt).toOption.fold(
          showInConsole("second argument must be number\nEg: <pekko> <20>", attentionGrabbing = true)
        )(count => checkTweetsInPekkoActor(expectedCount = count))
      case _ => showInConsole("<type: akka || pekko>", attentionGrabbing = true)
    }
  }
}
