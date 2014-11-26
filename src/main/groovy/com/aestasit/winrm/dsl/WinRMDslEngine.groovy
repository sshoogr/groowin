/*
 * Copyright (C) 2011-2014 Aestas/IT
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

package com.aestasit.winrm.dsl

import com.aestasit.winrm.WinRMOptions

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * WinRM DSL entry-point class that gives access to SessionDelegate instance.
 *
 * @author Andrey Adamovich
 *
 */
class WinRMDslEngine {

  private final WinRMOptions options
  private SessionDelegate delegate

  WinRMDslEngine(WinRMOptions options) {
    this.options = options
  }

  def remoteManagement(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    executeSession(cl, null, null)
  }

  def remoteManagement(String url, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    remoteManagement(url, null, cl)
  }

  def remoteManagement(String url, Object context,
                       @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    executeSession(cl, context) { SessionDelegate sessionDelegate ->
      sessionDelegate.url = url
    }
  }

  private executeSession(
      @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl, Object context,
      @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure configure) {
    def result = null
    if (cl) {
      if (delegate == null) {
        delegate = new SessionDelegate(options)
      }
      if (configure != null) {
        configure(delegate)
      }
      cl.delegate = delegate
      cl.resolveStrategy = DELEGATE_FIRST
      result = cl(context)
    }
    result
  }

}
