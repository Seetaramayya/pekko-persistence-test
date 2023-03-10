package com.seeta.pekko.test.persistence.leveldb

import com.seeta.pekko.test.PrintHelpers.showInConsole
import com.seeta.pekko.test.persistence.shared.{ AkkaInteractions, PekkoInteractions }

import scala.util.Try

object LevelDBMain extends AkkaInteractions with PekkoInteractions {

  def main(args: Array[String]): Unit = args match {
    case Array("akka") => sendTweetsToAkkaActor(AkkaLevelDB.behavior(), "leveldb-akka-actor", "application-akka.conf")
    case Array("pekko", input) =>
      Try(input.toInt).toOption.fold(
        showInConsole("second argument must be number\nEg: <pekko> <20>", attentionGrabbing = true)
      )(count =>
        checkTweetsInPekkoActor(
          expectedCount = count,
          PekkoLevelDB.behavior(),
          "leveldb-pekko-actor",
          "application-pekko.conf"
        )
      )
    case _ => showInConsole("<type: akka || pekko>", attentionGrabbing = true)
  }
}
