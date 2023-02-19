package com.seeta.pekko.test.persistence.shared

import akka.actor.typed.{ ActorSystem, Behavior }
import akka.util.Timeout
import com.seeta.pekko.test.PrintHelpers.showInConsole
import com.seeta.pekko.test.{ AddTweet, Command, GetAllTweetsFromAkka, Response, Tweets }
import com.seeta.pekko.test.persistence.leveldb.AkkaLevelDB
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import scala.concurrent.duration._

trait AkkaInteractions {
  private def sendTweet(actor: akka.actor.typed.ActorRef[Command], message: String): Unit = actor ! AddTweet(message)

  private def askAkkaActorForTweets(
      actor: akka.actor.typed.ActorRef[Command]
  )(implicit timeout: Timeout, system: ActorSystem[Command]): Future[Response] = {
    import akka.actor.typed.scaladsl.AskPattern._
    actor.ask(ref => GetAllTweetsFromAkka(ref))
  }

  protected def sendTweetsToAkkaActor(behavior: Behavior[Command], name: String, configName: String): Unit = {
    val config                                = ConfigFactory.load(configName)
    implicit val system: ActorSystem[Command] = ActorSystem(behavior, name, config)
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
}
