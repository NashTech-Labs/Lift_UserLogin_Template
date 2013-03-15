name := "User Login"

organization := "Knoldus"

version := "0.1"

scalaVersion := "2.9.2"



resolvers += "Scala-tools" at "https://oss.sonatype.org/content/groups/scala-tools"

resolvers += "Sonatype Snapshot" at "http://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Google Api client" at "http://mavenrepo.google-api-java-client.googlecode.com/hg/"

{
    val liftVersion = "2.5-M4"
    val dispatchVersion = "0.8.8"
    libraryDependencies ++= Seq(
            "net.liftweb" % "lift-mongodb-record_2.9.2" % "2.5-M4",
            "net.liftweb" % "lift-machine_2.9.1" % "2.5-SNAPSHOT",
            "net.liftmodules" %% "widgets" % (liftVersion+"-1.2") % "compile->default",
            "net.liftmodules" % "mongoauth_2.9.2" % (liftVersion+"-0.3"),
            "com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0",
            "org.scalatest" %% "scalatest" % "1.6.1" % "test",
            "junit" % "junit" % "4.7" % "test",
            "org.mortbay.jetty" % "jetty" % "6.1.22" % "test",
            "org.eclipse.jetty" % "jetty-webapp" % "7.5.1.v20110908" % "container",
            "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
            "net.databinder" %% "dispatch-http" % dispatchVersion,
            "net.databinder" %% "dispatch-http-json" % dispatchVersion,
            "org.scribe" % "scribe" % "1.1.2",
            "org.apache.solr" % "solr-solrj" % "3.3.0",
            "org.apache.solr" % "solr-core" % "3.3.0",
            "org.openid4java" % "openid4java" % "0.9.5",
            "junit" % "junit" % "4.7" % "test",
            "com.google.apis" % "google-api-services-oauth2" % "v2-rev9-1.7.2-beta",
            "net.liftmodules"   %% "lift-jquery-module" % (liftVersion + "-2.1"),
            "org.specs2" %% "specs2" % "1.12" % "test"
     )
}

scalacOptions += "-deprecation"

seq(webSettings :_*)


