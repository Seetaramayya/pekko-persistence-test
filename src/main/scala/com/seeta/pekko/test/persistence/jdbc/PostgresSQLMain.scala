package com.seeta.pekko.test.persistence.jdbc

import com.seeta.pekko.test.PrintHelpers.showInConsole
import com.seeta.pekko.test.persistence.shared.{ AkkaInteractions, PekkoInteractions }

import scala.util.Try

object PostgresSQLMain extends AkkaInteractions with PekkoInteractions {
  def main(args: Array[String]): Unit = args match {
    case Array("akka") =>
      sendTweetsToAkkaActor(
        AkkaPostgresSQL.behavior(),
        "postgres-sql-akka-actor",
        "application-akka-persistence-jdbc.conf"
      )
    case Array("pekko", input) =>
      Try(input.toInt).toOption.fold(
        showInConsole("second argument must be number\nEg: <pekko> <20>", attentionGrabbing = true)
      )(count =>
        checkTweetsInPekkoActor(
          expectedCount = count,
          PekkoPostgresSQL.behavior(),
          "postgres-sql-pekko-actor",
          "application-pekko-persistence-jdbc.conf"
        )
      )
    case _ => showInConsole("<type: akka || pekko>", attentionGrabbing = true)
  }
}
