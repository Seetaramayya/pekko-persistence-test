package com.seeta.pekko.test.persistence.jdbc

import com.seeta.pekko.test.Command
import com.seeta.pekko.test.persistence.shared.PekkoBehaviors
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.persistence.typed.PersistenceId

object PekkoPostgresSQL extends PekkoBehaviors {
  def behavior(): Behavior[Command] = eventSourceBehaviourWithSnapshotting(
    PersistenceId.ofUniqueId("postgresql-persistence-test-id")
  )
}
