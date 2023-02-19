package com.seeta.pekko.test

import akka.actor.typed.ActorRef
import org.apache.pekko.actor.typed.{ ActorRef => PekkoActorRef }

sealed trait Command
sealed trait Response

final case class Tweet(id: Long, message: String)
final case class AddTweet(message: String) extends Command

final case class GetAllTweetsFromAkka(akkaReply: ActorRef[Response])        extends Command
final case class GetAllTweetsFromPekko(pekkoReply: PekkoActorRef[Response]) extends Command
final case class Tweets(all: Seq[Tweet], totalEvents: Int)                  extends Response

case object DeleteAllTweets extends Command

sealed trait Event
final case class TweetAdded(tweet: Tweet) extends Event
case object TweetsDeleted                 extends Event

final case class State(tweets: Seq[Tweet], counter: Int)
object State {
  val initial: State = State(Seq(), 0)
}
