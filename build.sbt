name := "User Login"

organization := "Knoldus"

version := "0.1"

scalaVersion := "2.10.0"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Scala-tools" at "https://oss.sonatype.org/content/groups/scala-tools"

resolvers += "Sonatype Snapshot" at "http://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Google Api client" at "http://mavenrepo.google-api-java-client.googlecode.com/hg/"

{
    val liftVersion = "2.5-RC2"
    val dispatchVersion = "0.8.9"
    libraryDependencies ++= Seq(
    		"com.typesafe.akka" % "akka-actor" % "2.1-M1",
			"com.typesafe.akka" % "akka-remote" % "2.1-M1",
			"com.typesafe.akka" % "akka-testkit" % "2.1-M1",
			"com.typesafe.akka" % "akka-kernel" % "2.1-M1",
            "net.liftweb" % "lift-mongodb-record_2.10" % liftVersion,
            "net.liftweb" % "lift-mapper_2.10" % liftVersion,
            "net.liftmodules" % "widgets_2.10" % (liftVersion+"-1.2") % "compile->default",
            "net.liftmodules" % "mongoauth_2.10" % (liftVersion+"-0.4"),
            "org.mongodb" % "casbah_2.10" % "2.5.1",
            "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
            "junit" % "junit" % "4.7" % "test",
            "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container" artifacts (Artifact("javax.servlet", "jar", "jar")),
            "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container",
            "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
            "net.databinder" % "dispatch-http_2.10" % dispatchVersion,
            "net.databinder" % "dispatch-http-json_2.10" % dispatchVersion,
            "org.scribe" % "scribe" % "1.1.2",
            "org.apache.solr" % "solr-solrj" % "3.3.0",
            "org.apache.solr" % "solr-core" % "3.3.0",
            "org.openid4java" % "openid4java" % "0.9.5",
            "junit" % "junit" % "4.7" % "test",
            "com.google.apis" % "google-api-services-oauth2" % "v2-rev9-1.7.2-beta",
            "net.liftmodules" % "lift-jquery-module_2.10" % (liftVersion + "-2.2"),
            "org.specs2" % "specs2_2.10" % "1.14" % "test"
     )
}

scalacOptions += "-deprecation"

seq(webSettings :_*)
