package com.seeta.pekko.test.persistence.leveldb

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import com.seeta.pekko.test.PrintHelpers.showInConsole
import org.apache.pekko.actor.testkit.typed.scaladsl.{ ActorTestKit => PekkoTestKit }
import com.seeta.pekko.test.{
  AddTweet,
  Command,
  DeleteAllTweets,
  GetAllTweetsFromAkka,
  GetAllTweetsFromPekko,
  Response,
  Tweets
}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class LevelDBSpec extends AnyWordSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll {
  private val count       = 5
  private val akkaConfig  = ConfigFactory.load("application-akka.conf")
  private val pekkoConfig = ConfigFactory.load("application-pekko.conf")
  private val akkaTestKit = ActorTestKit(akkaConfig)

  override def afterAll(): Unit = {
    akkaTestKit.shutdownTestKit()
  }

  "LevelDB persistence compatability with akka-persistence" should {
    "be successful" in {
      implicit val system  = akkaTestKit.system
      val akkaProbe        = akkaTestKit.createTestProbe[Response]()
      val akkaLevelDBActor = akkaTestKit.spawn(AkkaLevelDB.behavior(), "leveldb-akka-actor")
      (1 to count).foreach(i => akkaLevelDBActor ! AddTweet(s"Tweet $i"))

      akkaLevelDBActor ! GetAllTweetsFromAkka(akkaProbe.ref)
      val tweets = akkaProbe.expectMessageType[Tweets]
      tweets.all.size shouldBe count

      showInConsole(s"Shutting down Akka actor (total events = ${tweets.totalEvents})", attentionGrabbing = true)
      akkaTestKit.shutdownTestKit()

      val pekkoTestKit      = PekkoTestKit("leveldb", pekkoConfig)
      val pekkoProbe        = pekkoTestKit.createTestProbe[Response]()
      val pekkoLevelDBActor = pekkoTestKit.spawn(PekkoLevelDB.behavior(), "leveldb-pekko-actor")
      pekkoLevelDBActor ! GetAllTweetsFromPekko(pekkoProbe.ref)
      val tweetsFromPekko = pekkoProbe.expectMessageType[Tweets]
      tweetsFromPekko.all.size shouldBe count

      pekkoLevelDBActor ! DeleteAllTweets

      pekkoLevelDBActor ! GetAllTweetsFromPekko(pekkoProbe.ref)
      val pekkoTweets = pekkoProbe.expectMessageType[Tweets]
      pekkoTweets.all.size shouldBe 0

      showInConsole(s"Shutting down Pekko actor (total events = ${pekkoTweets.totalEvents})", attentionGrabbing = true)
      pekkoTestKit.shutdownTestKit()

      // To pekko actor DeleteAllTweets message is sent which not seen by akka actor so 1 less event
      tweets.totalEvents shouldBe (pekkoTweets.totalEvents - 1)
    }
  }
}
