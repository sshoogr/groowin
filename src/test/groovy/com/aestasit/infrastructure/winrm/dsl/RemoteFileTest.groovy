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

package com.aestasit.infrastructure.winrm.dsl

import com.aestasit.infrastructure.winrm.WinRMException
import org.junit.Ignore
import org.junit.Test

class RemoteFileTest {

  String host = 'localhost'
  String user = 'user'
  String password = 'secret1234'

  @Test
  @Ignore
  void testLocalFileCreation() {
    // TODO: why is this failing?
    def filepath = 'C:/temp/new.csv'
    RemoteFile file = new RemoteFile(host, user, password, filepath)
    file.initialize()
    file.saveEmpty()
    assert file.canRead()
    file.text = 'Site,Today,Week Before,Month Before\naestasit,4,33,171\ngroovy,0,5,28\naetomation,0,4,41'
    file.text.split(",").contains('aetomation')
    new File(filepath).delete()
  }

  @Test(expected = WinRMException.class)
  void testWrongDestinationFilePath() {
    RemoteFile file = new RemoteFile(host, user, password, 'C/temp/new.csv')
    file.initialize()
  }

  @Test(expected = WinRMException.class)
  void testShortDestinationFilePath() {
    RemoteFile file = new RemoteFile(host, user, password, 'C')
    file.initialize()
  }

  @Test(expected = WinRMException.class)
  void testMissedSeparatorAfterColon() {
    RemoteFile file = new RemoteFile(host, user, password, 'C:temp/new.csv')
    file.initialize()
  }

}
