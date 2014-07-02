package com.aestasit.winrm

import com.aestasit.winrm.dsl.WinRMDslEngine
import com.aestasit.winrm.log.SysOutLogger
import org.junit.BeforeClass
import org.junit.Test

class CopyFilesTest {

  static WinRMOptions options
  static WinRMDslEngine engine

  @BeforeClass
  def static void defineOptions() {
    options = new WinRMOptions()
    options.with {
      logger = new SysOutLogger()

      defaultHost = '192.168.56.101'
      defaultUser = 'vagrant'
      defaultPassword = 'vagrant'
      defaultPort = 5985

      verbose = true
    }
    engine = new WinRMDslEngine(options)
  }

  @Test
  void testCopyRemoteDir() {
    engine.remoteSession {
      copying {
        from { remoteDir 'c:\\Windows\\System32\\drivers\\etc\\' }
        into { localDir 'C:\\temporary' }
      }
    }
  }

  @Test
  void testCopyRemoteFile() {
    engine.remoteSession {
      copying {
        from { remoteFile 'c:\\Windows\\System32\\drivers\\etc\\hosts' }
        into { localFile 'C:\\temporary\\234' }
      }
    }
  }

  @Test
  void testCopyLocalDir() {
    engine.remoteSession {
      copying {
        from { localDir 'c:\\Windows\\System32\\drivers\\etc\\' }
        into { remoteDir 'C:\\temporary' }
      }
    }
  }

  @Test
  void testCopyLocalFile() {
    engine.remoteSession {
      copying(new File('c:\\bootmgr'), "C:\\temp")
    }
  }
}
