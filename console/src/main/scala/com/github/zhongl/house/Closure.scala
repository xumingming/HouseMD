/*
 * Copyright 2012 zhongl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.zhongl.house

import management.ManagementFactory
import instrument.Instrumentation
import Reflections._

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
trait Closure extends (() => Unit) {
  override def toString() = "Closure: " + nativeToStringOf(this)

  protected def output(line: String)

  protected def instrumentation: Instrumentation
}

abstract class Summary extends Closure {
  def apply() {
    val runtime = ManagementFactory.getRuntimeMXBean
    output("name : " + runtime.getName)
  }
}

trait ListMapByPattern extends Closure {

  def apply() {
    for ((k, v) <- map) {
      if (pattern == null ||
        k.toLowerCase.contains(pattern.toLowerCase)) output(k + " = " + v)
    }
  }

  protected def pattern: String

  protected def map: Map[String, String]
}

abstract class Enviroment(protected val pattern: String = null) extends ListMapByPattern {
  protected def map = sys.env
}

abstract class Properites(protected val pattern: String = null) extends ListMapByPattern {
  protected def map = sys.props.toMap
}

abstract class LoadedClasses(regex: String = ".+", loaderHierarchies: Boolean = false)
  extends Closure {
  private[this] val tab = "\t"

  def apply() {
    instrumentation.getAllLoadedClasses filter {_.getName.matches(regex)} foreach {
      c =>
        output(c.getName + originOf(c))
        if (loaderHierarchies) layout(c.getClassLoader)
    }
  }

  private[this] def layout(cl: ClassLoader, lastIndents: String = "- ") {
    cl match {
      case null => Unit
      case _    =>
        val indents = tab + lastIndents
        output(indents + nativeToStringOf(cl))
        layout(cl.getParent, indents)
    }
  }

  private[this] def originOf(c: Class[_]): String = " -> " + Utils.sourceOf(c)
}

//abstract class Trace(regex: String) extends Closure {
//  def apply() {
//
//
//  }
//}
