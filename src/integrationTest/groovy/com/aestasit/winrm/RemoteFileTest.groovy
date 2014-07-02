package com.aestasit.winrm

import com.aestasit.winrm.dsl.WinRMDslEngine
import com.aestasit.winrm.log.SysOutLogger
import org.junit.BeforeClass
import org.junit.Test

class RemoteFileTest {
  static WinRMOptions options
  static WinRMDslEngine engine

  @BeforeClass
  def static void defineOptions() {
    options = new WinRMOptions()
    options.with {
      defaultHost = '192.168.56.101'
      defaultUser = 'vagrant'
      defaultPassword = 'vagrant'
      defaultPort = 5985

      execOptions.with {
        showOutput = true
        failOnError = true
      }

      verbose = true
      logger = new SysOutLogger()
    }
    engine = new WinRMDslEngine(options)
  }

  @Test
  void testFileOwner() {
    engine.remoteSession {
      // showing file ownership
      exec('del', 'C:\\temp\\hosts')

      copying {
        from { localFile 'c:\\Windows\\System32\\drivers\\etc\\hosts' }
        into { remoteDir 'C:\\temp' }
      }
      // showing file ownership
      exec('icacls', 'C:\\temp\\hosts')

      // taking ownership
      exec('takeown', '/f', 'C:\\temp\\hosts')

      // ICACLS <filename> /grant vagrant:F
      exec('icacls', 'C:\\temp\\hosts', '/grant', "$options.defaultUser:F")

      // showing file ownership
      exec('icacls', 'C:\\temp\\hosts')
    }
  }


  @Test
  void testRemoteFile() throws Exception {
    engine.remoteSession {
      assert remoteFile('c:\\Windows\\System32\\drivers\\etc\\hosts').text.contains('localhost')
    }
  }
}
