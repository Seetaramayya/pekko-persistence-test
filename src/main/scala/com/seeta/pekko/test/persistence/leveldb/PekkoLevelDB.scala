package com.seeta.pekko.test.persistence.leveldb

import com.seeta.pekko.test._
import com.seeta.pekko.test.persistence.shared.PekkoBehaviors
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.persistence.typed.PersistenceId

object PekkoLevelDB extends PekkoBehaviors {
  def behavior(): Behavior[Command] = eventSourceBehaviourWithSnapshotting(
    PersistenceId.ofUniqueId("leveldb-persistence-test-id")
  )
}
