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

/**
 * The class is responsible for 
 * <p>
 * Created by Sergey Korenko on 24.06.14.
 */
class WinRMDslEngine {
  private final WinRMOptions options
  private SessionDelegate delegate

  WinRMDslEngine(WinRMOptions options) {
    this.options = options
  }

  def remoteSession(Closure cl) {
    executeSession(cl, null, null)
  }

  private executeSession(Closure cl, Object context, Closure configure) {
    def result = null

    if (cl) {
      if (delegate == null) {
        delegate = new SessionDelegate(options)
      }

      cl.delegate = delegate
      cl.resolveStrategy = Closure.DELEGATE_FIRST
      result = cl(context)
    }

    result
  }
}
