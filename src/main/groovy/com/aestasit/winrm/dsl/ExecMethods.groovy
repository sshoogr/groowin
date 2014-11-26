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

package com.aestasit.winrm.dsl

import com.aestasit.winrm.ExecOptions
import com.aestasit.winrm.WinRMException
import com.xebialabs.overthere.CmdLine
import com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler

/**
 * Mix-in class for SessionDelegate implementing EXEC functionality.
 *
 * @author Andrey Adamovich
 */
class ExecMethods {

  /**
   * Executes command on a remote host.
   *
   * @param cmd a command to execute remotely.
   * @param arguments command arguments.
   * @return result of the command execution on a remote machine. The result is an instance of <code>CommandOutput</code>.
   */
  CommandOutput exec(String cmd, String... arguments) {
    doExec(cmd, new ExecOptions(options.execOptions), arguments?.toList())
  }

  /**
   * Executes command on a remote host.
   *
   * @param execOptions execution options.
   * @return result of the command execution on a remote machine. The result is an instance of <code>CommandOutput</code>.
   */
  CommandOutput exec(Map execOptions) {
    doExec(execOptions.command, new ExecOptions(options.execOptions, execOptions), execOptions.arguments?.toList())
  }

  /**
   * Execute the specified command and returns a boolean to
   * signal if the command execution was successful.
   *
   * @param cmd a command to execute remotely.
   * @return true , if command was successful.
   */
  boolean ok(String cmd, String... arguments) {
    def result = doExec(cmd, new ExecOptions([failOnError: false, showOutput: false]), arguments?.toList())
    result.exitStatus == 0
  }

  /**
   * Execute the specified command and returns a boolean to
   * signal if the command execution was unsuccessful.
   *
   * @param cmd a command to execute remotely.
   * @return true , if command was unsuccessful.
   */
  boolean fail(String cmd, String... arguments) {
    !ok(cmd, arguments)
  }

  private CommandOutput doExec(String cmd, ExecOptions options, List<String> arguments = []) {
    CommandOutput output = null
    cifsConnection { CifsWinRmConnection connection ->
      CmdLine cmdLine = composeCmdLine(cmd, arguments)
      output = catchExceptions(options) {
        executeCommand(cmdLine, options, connection)
      }
    }
    output
  }

  static private CmdLine composeCmdLine(String cmd, List<String> arguments = []) {
    CmdLine cmdLine = new CmdLine()
    cmdLine.addArgument(cmd)
    for (String arg : arguments) {
      cmdLine = cmdLine.addArgument(arg)
    }
    cmdLine
  }

  private CommandOutput catchExceptions(ExecOptions options, Closure cl) {
    try {
      return cl()
    } catch (WinRMException e) {
      failWithException(options, e)
    }
  }

  private CommandOutput failWithException(ExecOptions options, Throwable e) {
    if (options.failOnError) {
      throw new WinRMException("Command failed with the exception ", e)
    } else {
      logger.warn("Caught exception: " + e.getMessage())
      new CommandOutput(-1, e.getMessage(), e)
    }
  }

  private CommandOutput executeCommand(CmdLine cmd, ExecOptions options, CifsWinRmConnection connection) {
    if (options.showCommand) {
      logger.info("> " + cmd)
    }
    def outputHandler = CapturingOverthereExecutionOutputHandler.capturingHandler()
    def errorHandler = CapturingOverthereExecutionOutputHandler.capturingHandler()
    connection.execute(outputHandler, errorHandler, cmd)
    if (errorHandler.output) {
      throw new WinRMException("Executing command line [$cmd] failed. The error caused [$errorHandler.output]")
    }
    if (options.showOutput && outputHandler.output) {
      logger.info(outputHandler.output)
    }
    new CommandOutput(0, outputHandler.output)
  }
}
