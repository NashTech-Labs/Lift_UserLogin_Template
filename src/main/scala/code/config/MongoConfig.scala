package code
package config

import net.liftweb._
import common._
import json._
import mongodb._
import util.Props
import com.mongodb.{ DBAddress, Mongo }

object MongoConfig extends Loggable {
  implicit val formats = DefaultFormats
  def init() {
    /*
         * First checks for existence of mongo.default.url.
         *  If not found, then checks for mongo.default.host, 
         *  port, and name. Uses defaults if those
         * are not found.
         */
    val defaultDbAddress =
      Props.
        get("mongo.default.url") map (
          url => new DBAddress(url)) openOr (
            new DBAddress(
              Props.get("mongo.default.host", "localhost"),
              Props.getInt("mongo.default.port", 27017),
              Props.get("mongo.default.name", "knol")))

    MongoDB.defineDb(
      DefaultMongoIdentifier,
      new Mongo(defaultDbAddress),
      defaultDbAddress.getDBName)

    logger.info("MongoDB inited: %s".
      format(defaultDbAddress.toString))
  }
}
