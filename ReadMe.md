# Pekko Persistence Compatability Test

In this project I am trying to test `pekko-persistence` is working after migrating from `akka-persistence`. Following
things are verified

- [ScalaTest: LevelDB with jackson-json serialization](./src/test/scala/com/seeta/pekko/test/persistence/leveldb/LevelDBSpec.scala)
    - Actor messages (`Tweet`) are persisted with akka
    - Persisted messages are recovered with pekko
    - Deleted all tweets to make sure test case passes
    - Verified total events from akka and pekko
- [ManualTest: LevelDB with jackson-json serialization](./src/main/scala/com/seeta/pekko/test/persistence/leveldb/LevelDBMain.scala)
    - Step1 :
        - Invoke `com.seeta.pekko.test.persistence.leveldb.LevelDBMain` with `akka` argument which
          loads `application-akka.conf`
          and sends `10` tweets in each run to the akka actor.
        - Before terminating itself, number of messages received will be printed in the console. Same number of messages
          should be expected in the pekko actor.
    - Step2:
        - Invoke `com.seeta.pekko.test.persistence.leveldb.LevelDBMain` with `pekko`
          , `expected number of messages as number` argument which loads `application-pekko.conf` and creates pekko
          actors
        - Logs error or success at the end of the run. If successful wipes out all tweets for the next run.


## Start Postgres locally

### PostgresSQL commands

```shell
docker compose up postgres # to start the container

# to find all the table names: \d+
# to describe journal: \d+ journal
# to describe snapshot: \d+ snapshot

select * from journal;

select * from snapshot;
```