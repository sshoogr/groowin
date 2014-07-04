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

import org.junit.Test

class RemoteFileTest extends BaseIntegrationTest {

  @Test
  void testRemoteFileGetContent() throws Exception {
    engine.remoteManagement {
      assert remoteFile('c:\\Windows\\System32\\drivers\\etc\\hosts').text.contains('localhost')
    }
  }

  @Test
  void testRemoteFileSetContent() throws Exception {
    engine.remoteManagement {
      String filename = 'c:\\temp\\empty.groovy'
      remoteFile(filename).saveEmpty()
      remoteFile(filename).text = 'Hello World'
      assert remoteFile(filename).canRead()
      assert !remoteFile(filename).isHidden()
      println new Date(remoteFile(filename).lastModified())
      assert remoteFile(filename).length() < 1000l
      exec('del', filename)
    }
  }

}
