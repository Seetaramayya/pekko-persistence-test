package com.seeta.pekko.test.persistence.leveldb

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.persistence.typed.PersistenceId
import org.apache.pekko.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }
import com.seeta.pekko.test._
import org.slf4j.Logger

object PekkoLevelDB {
  def behavior(): Behavior[Command] = {
    val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
      command match {
        case AddTweet(message) =>
          Effect.persist(TweetAdded(Tweet(state.tweets.headOption.map(_.id).getOrElse(0L) + 1, message)))
        case GetAllTweetsFromPekko(replyTo) => Effect.none.thenReply(replyTo)(s => Tweets(s.tweets, s.counter))
        case GetAllTweetsFromAkka(_)        => Effect.none
        case DeleteAllTweets                => Effect.persist(TweetsDeleted)
      }
    }
    val eventHandler: (State, Event) => State = { (state, event) =>
      event match {
        case TweetAdded(tweet) =>
          println(s"[Pekko State mutation with add event]: $tweet is going to be added")
          state.copy(tweet +: state.tweets)
          State(tweet +: state.tweets, state.counter + 1)
        case TweetsDeleted =>
          println("[Pekko State mutation with delete event]: All tweets wiped out in Pekko")
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
