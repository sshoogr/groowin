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

import com.aestasit.infrastructure.winrm.mock.WinRMHostMock
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static com.aestasit.infrastructure.winrm.DefaultWinRM.execOptions
import static com.aestasit.infrastructure.winrm.DefaultWinRM.remoteManagement
import static org.junit.Assert.*

/**
 * Base class for testing WinRM via HTTP using groovy-winrm-mock project
 *
 * @author Sergey Korenko
 */
class BaseWinRMHttpTest {

  @BeforeClass
  static void initClient() {
    WinRMHostMock.startWinRMServer()

    WinRMHostMock.command('whoami', [] as String[], 0, 'win-l9po57hvelf', null)
    WinRMHostMock.command('mkdr', [] as String[], 1, null, "mkdur is not recognized as an internal or external command,\noperable program or batch file.")
    WinRMHostMock.command('rmdir', ['/S', '/Q', 'c:\\tempGroowin'] as String[], 1, null, "The system cannot find the path specified.")
  }

  @AfterClass
  static void shutdownHttpTestServer() {
    WinRMHostMock.stopWinRMServer()
  }

  @Test
  void testStaticMethods() throws Exception {
    execOptions {
      maxWait = 30000
    }
    remoteManagement('user:secret@localhost:5985') {
      def result = exec 'whoami'

      assertEquals( 0, result.exitStatus)
      assertEquals( 'win-l9po57hvelf', result.output)
    }
  }

  @Test
  void testUnknownCommand() throws Exception{
    remoteManagement('user:secret@localhost:5985') {
      def result = exec 'mkdr'

      assertEquals( 1, result.exitStatus)
      assert result.output.contains('mkdur is not recognized as an internal or external command')
    }
  }

  @Test
  void testCannotFindPath(){
    remoteManagement('user:secret@localhost:5985') {
      def result = exec('rmdir', '/S', '/Q', 'c:\\tempGroowin')

      assertEquals( 1, result.exitStatus)
      assert result.output.contains('The system cannot find the path specified')
    }
  }
}