/*
 * Copyright (C) 2011-2015 Aestas/IT
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

package com.aestasit.infrastructure.winrm.dsl

import com.aestasit.infrastructure.winrm.WinRMOptions
import com.aestasit.infrastructure.winrm.log.SysOutLogger
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.*
import static com.aestasit.infrastructure.winrm.client.util.Constants.*

class SessionDelegateTest {

  static WinRMOptions options
  static SessionDelegate sessionDelegate

  @BeforeClass
  static void init(){
    options = new WinRMOptions()
    options.with {
      logger = new SysOutLogger()
      verbose = true
      execOptions.with {
        showOutput = true
        showCommand = true
      }
    }
    sessionDelegate = new SessionDelegate(options)
  }

  @Test
  void testSetUrlHTTP() {
    sessionDelegate.url = 'vagrant:vagrant@winrmhost:5985'

    assertEquals( PROTOCOL_HTTP, sessionDelegate.protocol)
    assertEquals( 'winrmhost', sessionDelegate.host)
    assertEquals( 'vagrant', sessionDelegate.username)
    assertEquals( 5985, sessionDelegate.port)
  }

  @Test
  void testSetUrlHTTPS() {
    sessionDelegate.url = 'vagrant:vagrant@winrmhost:5986'

    assertEquals( PROTOCOL_HTTPS, sessionDelegate.protocol)
    assertEquals( 'winrmhost', sessionDelegate.host)
    assertEquals( 'vagrant', sessionDelegate.username)
    assertEquals( 5986, sessionDelegate.port)
  }
}
