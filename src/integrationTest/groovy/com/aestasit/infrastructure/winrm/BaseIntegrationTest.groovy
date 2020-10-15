/*
 * Copyright (C) 2011-2020 Aestas/IT
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

import com.aestasit.infrastructure.winrm.dsl.WinRMDslEngine
import com.aestasit.infrastructure.winrm.log.SysOutLogger
import org.junit.BeforeClass

class BaseIntegrationTest {

  static WinRMOptions options
  static WinRMDslEngine engine

  @BeforeClass
  def static void defineOptions() {
    options = new WinRMOptions()
    options.with {
      logger = new SysOutLogger()
      defaultHost = '192.168.25.25'
      defaultUser = 'vagrant'
      defaultPassword = 'vagrant'
      defaultPort = 5985
      verbose = true
      execOptions.with {
        maxWait = 60000
      }
    }
    engine = new WinRMDslEngine(options)
  }
}