package com.github.pjfanning.session.converters

import scala.jdk.CollectionConverters._

object MapConverters {

  implicit def toImmutableMap[A, B](m: java.util.Map[A, B]): scala.collection.immutable.Map[A, B] = m.asScala.toMap

}
