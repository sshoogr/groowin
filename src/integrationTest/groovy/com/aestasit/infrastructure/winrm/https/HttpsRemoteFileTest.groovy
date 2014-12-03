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

package com.aestasit.infrastructure.winrm.https

import com.aestasit.infrastructure.winrm.dsl.RemoteFile
import org.junit.Test

class HttpsRemoteFileTest extends BaseHttpsIntegrationTest {

  @Test
  void testRemoteFileGetContent() throws Exception {
    engine.remoteManagement {
      assert remoteFile('c:\\Windows\\System32\\drivers\\etc\\hosts').text.contains('localhost')
    }
  }

  @Test
  void testRemoteFileSetContent() throws Exception {
    engine.remoteManagement {
      def filename = 'c:\\temp\\empty.groovy'
      def file = remoteFile(filename)
      file.saveEmpty()
      assert file.canWrite()
      file.text = 'Hello World'
      assert file.canRead()
      assert !file.isHidden()
      assert file.length() < 100l
      assert new Date(file.lastModified())
      file.delete()
    }
  }

  @Test
  void testRemoteFileList() throws Exception {
    engine.remoteManagement {
      def folderName = "C:\\temp${System.currentTimeMillis()}\\"
      def folder = remoteFile(folderName)
      folder.mkdir()
      assert folder.exists()
      assert folder.isDirectory()

      def file1 = remoteFile("${folderName}file1.txt")
      file1.saveEmpty()
      file1.text = "Wallace and Gromit"

      def file2 = remoteFile("${folderName}file2.csv")
      file2.saveEmpty()
      file2.text = "Wallace, Gromit\nGromit,Wallace"

      RemoteFile[] files = folder.listFiles()
      assert files.length == 2
      assert files[0].text.contains('Gromit')
      assert files[1].text.contains('Gromit')

      exec('rmdir', '/s', '/q', folderName)
    }
  }
}
