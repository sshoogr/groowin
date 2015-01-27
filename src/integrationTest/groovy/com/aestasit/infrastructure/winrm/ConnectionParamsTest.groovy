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

package com.aestasit.infrastructure.winrm

import org.junit.Test

class ConnectionParamsTest extends BaseIntegrationTest {

  // under Windows UnknownHostException
  // under Linux   HttpHostConnectException
  @Test(expected = Exception.class)
  void testFailConnection() {
    engine.options.with {
      defaultHost = 'some_fake_address'
    }

    engine.remoteManagement {
      exec('type', 'C:\\temp\\fictional.file')
    }
  }

  // caused by TimeoutException
  @Test(expected = WinRMException.class)
  void testLongOperation() throws Exception {
    engine.options.with {
      defaultHost = '192.168.25.25'
      execOptions.with {
        maxWait = 5000l
      }
    }

    engine.remoteManagement {
      exec('ping', ['localhost', '-n', '25'] as String[])
    }
  }
}
