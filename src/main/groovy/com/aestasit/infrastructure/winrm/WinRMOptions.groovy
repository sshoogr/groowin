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

package com.aestasit.infrastructure.winrm

import com.aestasit.infrastructure.winrm.log.Logger

import static com.aestasit.infrastructure.winrm.client.util.Constants.*

/**
 * Configuration object holding options used for DSL configuration.
 *
 * @author Sergey Korenko
 *
 */
class WinRMOptions extends CommonOptions {

  // WinRM connection options.
  String defaultHost
  String defaultUser
  String defaultPassword
  int defaultPort = PORT_HTTP
  boolean verbose = false
  Logger logger
  String defaultProtocol = PROTOCOL_HTTP

  // Exec options.
  ExecOptions execOptions = new ExecOptions()
  def execOptions(Closure cl) {
    cl.delegate = execOptions
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

  // CIFS copy options.
  CopyOptions copyOptions = new CopyOptions()
  def copyOptions(Closure cl) {
    cl.delegate = copyOptions
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

}
