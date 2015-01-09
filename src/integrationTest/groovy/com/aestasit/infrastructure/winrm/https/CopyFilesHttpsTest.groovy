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

import org.junit.Test

class CopyFilesHttpsTest extends BaseHttpsIntegrationTest {

  @Test
  void testCopyRemoteDir() {
    engine.remoteManagement {
      cp {
        from { remoteDir 'c:\\Windows\\System32\\drivers\\etc\\' }
        into { localDir 'C:\\temporary' }
      }
    }
  }

  @Test
  void testCopyRemoteFile() {
    engine.remoteManagement {
      cp {
        from { remoteFile 'c:\\Windows\\System32\\drivers\\etc\\hosts' }
        into { localFile 'temporary\\234' }
      }
    }

    def newFolder = new File('temporary')
    newFolder.deleteDir()
  }

  @Test
  void testCopyLocalDir() {
    String folderToCreate = "folder_${System.currentTimeMillis()}"
    def newFolder = new File(folderToCreate)
    newFolder.mkdir()
    def temp = new File(newFolder, "file1.txt")
    temp.createNewFile()
    temp = new File(newFolder, "file2.txt")
    temp.createNewFile()
    temp.text = "Ping"


    engine.remoteManagement {
      cp {
        from { localDir folderToCreate }
        into { remoteDir 'C:\\temporary' }
      }

      exec('rmdir', '/s', '/q', 'C:\\temporary')
    }

    newFolder.deleteDir()
  }

  @Test
  void testCopyLocalFile() {
    engine.remoteManagement {
      cp(new File('./test.file').absoluteFile.canonicalPath, 'C:\\temp')
      exec('del', '/F', '/Q', 'C:\\temp\\test.file')
    }
  }
}
