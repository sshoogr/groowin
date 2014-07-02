package com.aestasit.winrm

import com.aestasit.winrm.dsl.WinRMDslEngine
import com.aestasit.winrm.log.SysOutLogger
import org.junit.BeforeClass
import org.junit.Test

class ExecTest {
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
        showOutput = false
        failOnError = true
      }

      verbose = true
      logger = new SysOutLogger()
    }
    engine = new WinRMDslEngine(options)
  }

  @Test
  void testTypeHostsFile() {
    engine.remoteSession {
      exec('type', 'c:\\Windows\\System32\\drivers\\etc\\hosts')
    }
  }

  @Test
  void testCommandCreateFile() {
    engine.remoteSession {
      exec('echo.', 'This is a test string in a test file','>','c:\\temp\\test.file')
      exec('del', 'c:\\temp\\test.file')
    }
  }

  @Test
  void testCommandMap(){
    engine.remoteSession {
      def output = exec(command: 'dir', showOutput: false)
      output.output.eachLine { line -> println ">>>>> OUTPUT: ${line}" }
      println ">>>>> EXIT: ${output.exitStatus}"
    }
  }

  @Test
  void testOk() throws Exception {
    engine.remoteSession {
     assert ok('dir')
    }
  }

  @Test
  void testFail() throws Exception {
    engine.remoteSession {
      assert fail('mkdur dur')
    }
  }
}

