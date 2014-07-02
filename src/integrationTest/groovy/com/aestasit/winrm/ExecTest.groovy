package com.aestasit.winrm

import org.junit.Test

class ExecTest extends BaseIntegrationTest {
  @Test
  void testTypeHostsFile() {
    engine.remoteSession {
      exec('type', 'c:\\Windows\\System32\\drivers\\etc\\hosts')
    }
  }

  @Test
  void testCommandCreateFile() {
    engine.remoteSession {
      exec('echo.', 'This is a test string in a test file','>','c:\\temp\\testEcho.file')
      exec('del', 'c:\\temp\\testEcho.file')
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
