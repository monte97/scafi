/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.distrib.actor.serialization

import play.api.libs.json.{JsArray, JsValue, Json}

trait JsonPrimitivesSerialization {
  def anyToJs: PartialFunction[Any, JsValue] = {
    case b:Boolean => Json.obj("type" -> "Boolean", "val" -> b)
    case b:Byte => Json.obj("type" -> "Byte", "val" -> b)
    case i:Int => Json.obj("type" -> "Int", "val" -> i)
    case l:Long => Json.obj("type" -> "Long", "val" -> l)
    case f:Float => Json.obj("type" -> "Float", "val" -> f)
    case d:Double => Json.obj("type" -> "Double", "val" -> d)
    case c:Char => Json.obj("type" -> "Char", "val" -> c.toString)
    case s:String => Json.obj("type" -> "String", "val" -> s)
  }
  def jsToAny: PartialFunction[JsValue, Any] = {
    case b if (b \ "type").as[String] == "Boolean" => (b \ "val").as[Boolean]
    case b if (b \ "type").as[String] == "Byte" => (b \ "val").as[Byte]
    case i if (i \ "type").as[String] == "Int" => (i \ "val").as[Int]
    case l if (l \ "type").as[String] == "Long" => (l \ "val").as[Long]
    case f if (f \ "type").as[String] == "Float" => (f \ "val").as[Float]
    case d if (d \ "type").as[String] == "Double" => (d \ "val").as[Double]
    case c if (c \ "type").as[String] == "Char" => (c \ "val").as[String].head
    case s if (s \ "type").as[String] == "String" => (s \ "val").as[String]
  }
}

trait JsonOptionSerialization extends JsonPrimitivesSerialization {
  override def anyToJs: PartialFunction[Any, JsValue] = super.anyToJs orElse {
    case Some(o) => Json.obj("type" -> "Option", "isDefined" -> true, "val" -> anyToJs(o))
    case None => Json.obj("type" -> "Option", "isDefined" -> false)
  }
  override def jsToAny: PartialFunction[JsValue, Any] = super.jsToAny orElse {
    case o if (o \ "type").as[String] == "Option" =>
      if ((o \ "isDefined").as[Boolean]) { Some(jsToAny((o \ "val").get)) } else { None }
  }
}

trait JsonCollectionsSerialization extends JsonPrimitivesSerialization {
  override def anyToJs: PartialFunction[Any, JsValue] = super.anyToJs orElse {
    case l:List[Any] => Json.obj("type" -> "List", "list" -> JsArray(l.map(anyToJs)))
    case m: Map[Any,Any] => Json.obj("type" -> "Map", "keys" -> anyToJs(m.keys.toList), "values" -> anyToJs(m.values.toList))
  }
  override def jsToAny: PartialFunction[JsValue, Any] = super.jsToAny orElse {
    case l if (l \ "type").as[String] == "List" => (l \ "list").as[JsArray].value.map(jsToAny).toList
    case m if (m \ "type").as[String] == "Map" =>
      (jsToAny((m \ "keys").get).asInstanceOf[List[Any]] zip jsToAny((m \ "values").get).asInstanceOf[List[Any]]).toMap
  }
}