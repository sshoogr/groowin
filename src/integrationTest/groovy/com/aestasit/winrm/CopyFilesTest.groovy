package com.aestasit.winrm

import org.junit.Test

class CopyFilesTest extends BaseIntegrationTest {
  @Test
  void testCopyRemoteDir() {
    engine.remoteSession {
      cp {
        from { remoteDir 'c:\\Windows\\System32\\drivers\\etc\\' }
        into { localDir 'C:\\temporary' }
      }
    }
  }

  @Test
  void testCopyRemoteFile() {
    engine.remoteSession {
      cp {
        from { remoteFile 'c:\\Windows\\System32\\drivers\\etc\\hosts' }
        into { localFile 'C:\\temporary\\234' }
      }
    }
  }

  @Test
  void testCopyLocalDir() {
    engine.remoteSession {
      cp {
        from { localDir 'c:\\Windows\\System32\\drivers\\etc\\' }
        into { remoteDir 'C:\\temporary' }
      }
    }
  }

  @Test
  void testCopyLocalFile() {
    engine.remoteSession {
      cp(new File('./test.file').absoluteFile.canonicalPath, 'C:\\temp')
    }
  }
}
