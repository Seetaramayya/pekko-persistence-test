package com.seeta.pekko.test.persistence.jdbc

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import com.seeta.pekko.test.PrintHelpers.showInConsole
import org.apache.pekko.actor.testkit.typed.scaladsl.{ ActorTestKit => PekkoTestKit }
import com.seeta.pekko.test._
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PostgresSQLPersistenceSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {
  private val count       = 5
  private val akkaConfig  = ConfigFactory.load("application-akka-persistence-jdbc.conf")
  private val pekkoConfig = ConfigFactory.load("application-pekko-persistence-jdbc.conf")
  private val akkaTestKit = ActorTestKit(akkaConfig)

  override def afterAll(): Unit = {
    akkaTestKit.shutdownTestKit()
  }

  "PostgresSQL persistence compatability with akka-persistence" should {
    "be successful" in {
      implicit val system  = akkaTestKit.system
      val akkaProbe        = akkaTestKit.createTestProbe[Response]()
      val akkaLevelDBActor = akkaTestKit.spawn(AkkaPostgresSQL.behavior(), "akka-jdbc-actor")
      (1 to count).foreach(i => akkaLevelDBActor ! AddTweet(s"Tweet $i"))

      akkaLevelDBActor ! GetAllTweetsFromAkka(akkaProbe.ref)
      val tweets = akkaProbe.expectMessageType[Tweets]
      tweets.all.size shouldBe count

      showInConsole(s"Shutting down Akka actor (total events = ${tweets.totalEvents})", attentionGrabbing = true)
      akkaTestKit.shutdownTestKit()

      val pekkoTestKit      = PekkoTestKit("leveldb", pekkoConfig)
      val pekkoProbe        = pekkoTestKit.createTestProbe[Response]()
      val pekkoLevelDBActor = pekkoTestKit.spawn(PekkoPostgresSQL.behavior(), "pekko-jdbc-actor")
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
