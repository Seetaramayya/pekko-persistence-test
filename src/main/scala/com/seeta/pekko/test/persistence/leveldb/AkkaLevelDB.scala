package com.seeta.pekko.test.persistence.leveldb

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import com.seeta.pekko.test._
import com.seeta.pekko.test.persistence.shared.AkkaBehaviors

object AkkaLevelDB extends AkkaBehaviors {
  def behavior(): Behavior[Command] = eventSourceBehaviourWithSnapshotting(
    PersistenceId.ofUniqueId("leveldb-persistence-test-id")
  )
}
