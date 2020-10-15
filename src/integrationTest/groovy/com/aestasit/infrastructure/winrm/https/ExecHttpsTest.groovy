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
package com.aestasit.infrastructure.winrm.https

import org.junit.Test

class ExecHttpsTest extends BaseHttpsIntegrationTest {

  @Test
  void testTypeHostFiles() {
    engine.remoteManagement {
      exec('type', 'c:\\Windows\\System32\\drivers\\etc\\hosts')
      exec(
          command: 'type',
          arguments: ['d:\\Windows\\System32\\drivers\\etc\\hosts'],
          failOnError: false
      )
    }
  }

  @Test
  void testCommandCreateFile() {
    engine.remoteManagement('vagrant:vagrant@192.168.25.25:5986') {
      exec('mkdir', 'c:\\tempGroowin')
      exec('echo.', 'This is a test string in a test file', '>', 'c:\\tempGroowin\\testEcho.file')
      exec('rmdir', '/S', '/Q', 'c:\\tempGroowin')
    }
  }

  @Test
  void testCommandMap() {
    engine.remoteManagement {
      def result = exec(command: 'dir', showOutput: false)
      result.output.eachLine { line -> println ">>>>> OUTPUT: ${line}" }
      println ">>>>> EXIT: ${result.exitStatus}"
    }
  }

  @Test
  void testOk() throws Exception {
    engine.remoteManagement {
      assert ok('dir')
    }
  }

  @Test
  void testFail() throws Exception {
    engine.remoteManagement {
      assert fail('mkdur dur')
    }
  }

  @Test
  void testTimeout() throws Exception {
    engine.remoteManagement {
      exec('timeout', '15')
    }
  }

}
