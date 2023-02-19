package com.seeta.pekko.test.persistence.shared

import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior, RetentionCriteria }
import com.seeta.pekko.test.PrintHelpers.showInConsole
import com.seeta.pekko.test._

trait AkkaBehaviors {
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
        showInConsole(s"[Akka State mutation with add event]: $tweet is going to be added")
        State(tweet +: state.tweets, state.counter + 1)
      case TweetsDeleted =>
        showInConsole("[Akka State mutation with delete event]: All tweets wiped out")
        State(Seq.empty[Tweet], state.counter + 1)
    }
  }

  def eventSourceBehaviourWithSnapshotting(
      persistenceId: PersistenceId,
      maybeJournalPluginId: Option[String] = None,
      maybeSnapshotPluginId: Option[String] = None
  ): EventSourcedBehavior[Command, Event, State] = {
    val behavior = EventSourcedBehavior[Command, Event, State](
      persistenceId = persistenceId,
      emptyState = State.initial,
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
    val maybeBehavior = for {
      journalPluginId  <- maybeJournalPluginId
      snapshotPluginId <- maybeSnapshotPluginId
    } yield {
      behavior.withSnapshotPluginId(snapshotPluginId).withJournalPluginId(journalPluginId)
    }

    maybeBehavior
      .fold(behavior)(identity)
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 10, keepNSnapshots = 2))
  }
}
