package com.seeta.pekko.test.persistence.jdbc

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import com.seeta.pekko.test.Command
import com.seeta.pekko.test.persistence.shared.AkkaBehaviors

object AkkaPostgresSQL extends AkkaBehaviors {
  def behavior(): Behavior[Command] = eventSourceBehaviourWithSnapshotting(
    PersistenceId.ofUniqueId("postgresql-persistence-test-id")
  )
}
