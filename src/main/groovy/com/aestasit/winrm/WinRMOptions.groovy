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

package com.aestasit.winrm

import com.aestasit.winrm.log.Logger

/**
 * The class is responsible for 
 * <p>
 * Created by Sergey Korenko on 24.06.14.
 */
class WinRMOptions extends CommonOptions {

  // WinRM connection options.
  String defaultHost        = null
  String defaultUser        = null
  String defaultPassword    = null
  int defaultPort           = 5985
  boolean verbose           = false
  Logger logger             = null

  // exec options
  ExecOptions execOptions = new ExecOptions()
  def execOptions(Closure cl) {
    cl.delegate = execOptions
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

  // CIFS copy options
  CopyOptions copyOptions = new CopyOptions()
  def copyOptions(Closure cl) {
    cl.delegate = copyOptions
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
}
