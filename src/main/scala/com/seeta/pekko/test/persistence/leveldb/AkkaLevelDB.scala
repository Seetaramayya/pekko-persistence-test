package com.seeta.pekko.test.persistence.leveldb

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }
import com.seeta.pekko.test._

object AkkaLevelDB {
  def behavior(): Behavior[Command] = {
    val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
      command match {
        case AddTweet(message) =>
          Effect.persist(TweetAdded(Tweet(state.tweets.headOption.map(_.id).getOrElse(0L) + 1, message)))
        case GetAllTweetsFromAkka(replyTo) =>
          Effect.none.thenReply(replyTo)(s => Tweets(s.tweets, s.counter))
        case GetAllTweetsFromPekko(_) => Effect.none
        case DeleteAllTweets          => Effect.persist(TweetsDeleted)
      }
    }
    val eventHandler: (State, Event) => State = { (state, event) =>
      event match {
        case TweetAdded(tweet) =>
          println(s"[Akka State mutation with add event]: $tweet is going to be added")
          State(tweet +: state.tweets, state.counter + 1)
        case TweetsDeleted =>
          println("[Akka State mutation with delete event]: All tweets wiped out")
          State(Seq.empty[Tweet], state.counter + 1)
      }
    }

    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("leveldb-persistence-test-id"),
      emptyState = State.initial,
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
  }

}
