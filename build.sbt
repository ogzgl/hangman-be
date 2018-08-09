name := "hf_playhangman"
 
version := "1.0" 
      
lazy val `hf_playhangman` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
libraryDependencies += "com.typesafe.play" %% "play-test" % "2.6.17"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"
libraryDependencies += "org.mockito" % "mockito-core" % "1.9.5"