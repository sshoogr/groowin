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
import com.aestasit.infrastructure.winrm.log.Logger
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

  @Test
  void testStreamsCopying(){
    def file1 = new File('bigSourceFile.txt')
    for(int i = 0; i < 9999;i++){
      file1 <<"12345678910wedfrtgyhujikol;p[asdfghjkl;'zxcvbnm,.12345678910wedfrtgyhujikol;p[asdfghjkl;'zxcvbnm,.12345678910wedfrtgyhujikol;p[asdfghjkl;'zxcvbnm,.12345678910wedfrtgyhujikol;p[asdfghjkl;'zxcvbnm,."
    }
    def file2 = new File('destinationFile.txt')
    sessionDelegate.copyStreams(file2.newOutputStream(), file1.newInputStream(), file1.length(), true)

    assertEquals(file1.length(), file2.length())
  }

  @Test
  void testStreamsCopyingWithoutProgress(){
    def file1 = new File('bigSourceFile.txt')
    for(int i = 0; i < 9999;i++){
      file1 <<"12345678910wedfrtgyhujikol;p[asdfghjkl;'zxcvbnm,.12345678910wedfrtgyhujikol;p[asdfghjkl;'zxcvbnm,.12345678910wedfrtgyhujikol;p[asdfghjkl;'zxcvbnm,.12345678910wedfrtgyhujikol;p[asdfghjkl;'zxcvbnm,."
    }
    def file2 = new File('destinationFile.txt')
    sessionDelegate.copyStreams(file2.newOutputStream(), file1.newInputStream(), file1.length(), false)

    assertEquals(file1.length(), file2.length())

    file1.delete()
    file2.delete()
  }

  @Test
  void testLogCommand(){
    sessionDelegate.logger = new Logger(){
      String infoToLog
      String warnToLog
      String debugToLog
      @Override
      void info(String message) {
        infoToLog = message
      }

      @Override
      void warn(String message) {
        warnToLog = message
      }

      @Override
      void debug(String message) {
        debugToLog = message
      }
    }

    sessionDelegate.logCommand('rmdir', '/S', '/Q', 'c:\\tempGroowin')
    assertEquals( 'rmdir /S /Q c:\\tempGroowin', sessionDelegate.logger.infoToLog)

    sessionDelegate.logCommand('rmdir', '/S', '/Q', '"c:\\Program Files\\tempGroowin"')
    assertEquals( 'rmdir /S /Q "c:\\Program Files\\tempGroowin"', sessionDelegate.logger.infoToLog)

    sessionDelegate.logger = new SysOutLogger()
  }
}
