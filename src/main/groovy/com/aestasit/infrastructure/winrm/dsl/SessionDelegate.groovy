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

import com.aestasit.infrastructure.winrm.ExecOptions
import com.aestasit.infrastructure.winrm.WinRMException
import com.aestasit.infrastructure.winrm.WinRMOptions
import com.aestasit.infrastructure.winrm.client.WinRMClient
import com.aestasit.infrastructure.winrm.log.Logger
import com.aestasit.infrastructure.winrm.log.Slf4jLogger

import java.util.concurrent.TimeoutException
import java.util.regex.Pattern

import static com.aestasit.infrastructure.winrm.client.util.Constants.PORT_HTTP
import static com.aestasit.infrastructure.winrm.client.util.Constants.PROTOCOL_HTTP
import static com.aestasit.infrastructure.winrm.dsl.FileSetType.*
import static groovy.lang.Closure.DELEGATE_FIRST
import static org.apache.commons.io.FilenameUtils.*

/**
 * Closure delegate that is used to collect all WinRM options and give access to other DSL delegates.
 *
 * @author Andrey Adamovich
 */
class SessionDelegate {

  private static final Pattern WINRM_URL = ~/^(([^:@]+)(:([^@]+))?@)?([^:]+)(:(\d+))?$/

  private String protocol = PROTOCOL_HTTP
  private String host
  private int port = PORT_HTTP
  private String username
  private String password

  private final WinRMOptions options

  protected Logger logger
  private WinRMClient client
  private boolean changed

  SessionDelegate(WinRMOptions options) {
    this.options = options
    this.host = options.defaultHost
    this.username = options.defaultUser
    this.port = options.defaultPort
    this.password = options.defaultPassword
    this.protocol = options.defaultProtocol
    if (options.logger != null) {
      logger = options.logger
    } else {
      logger = new Slf4jLogger()
    }
  }

  RemoteFile remoteFile(String destination) {
    def file = new RemoteFile(host, user, password, destination)
    file.initialize()
    file
  }

  String getHost() {
    host
  }

  int getPort() {
    port
  }

  String getUser() {
    username
  }

  String getPassword() {
    password
  }

  void setChanged(changed) {
    this.changed = changed
  }

  void setHost(String host) {
    this.changed = changed || (this.host != host)
    this.host = host
  }

  void setPort(int port) {
    this.changed = changed || (this.port != port)
    this.port = port
  }

  void setUser(String username) {
    this.changed = changed || (this.username != username)
    this.username = username
  }

  void setPassword(String password) {
    this.changed = changed || (this.password != password)
    this.password = password
  }

  void setUrl(String url) {
    def matcher = WINRM_URL.matcher(url)
    if (matcher.matches()) {
      setHost(matcher.group(5))
      if (matcher.group(7)) {
        setPort(matcher.group(7).toInteger())
      } else {
        setPort(PORT_HTTP)
      }
      setUser(matcher.group(2))
      setPassword(matcher.group(4))
    } else {
      throw new MalformedURLException("Unknown URL format: " + url)
    }
  }

  boolean isSessionOpen() {
    client?.isConnected()
  }

  void connect() {
    try {
      if (!client || !sessionOpen || changed) {
        disconnect()

        client = new WinRMClient(protocol, host, port, username, password)

        if (options.verbose) {
          logger.info(">>> Connecting to $host")
        }

        client.openShell()
      }
    } finally {
      changed = false
    }
  }


  void disconnect() {
    if (sessionOpen) {
      try {
        client.deleteShell()
      } catch (Exception e) {
      } finally {
        if (options.verbose) {
          logger.info("<<< Disconnected from $host")
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //   ________   ________ _____
  //  |  ____\ \ / /  ____/ ____|
  //  | |__   \ V /| |__ | |
  //  |  __|   > < |  __|| |
  //  | |____ / . \| |___| |____
  //  |______/_/ \_\______\_____|
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////
//  def execOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = ExecOptions) Closure cl) {
//    options.execOptions(cl)
//  }

  CommandOutput exec(@DelegatesTo(strategy = DELEGATE_FIRST, value = ExecOptionsDelegate) Closure cl) {
    ExecOptionsDelegate delegate = new ExecOptionsDelegate(options.execOptions)
    cl.delegate = delegate
    cl.resolveStrategy = DELEGATE_FIRST
    cl()
    if (!delegate.command) {
      new WinRMException('Remote command is not specified!')
    }
    doExec(delegate.command, new ExecOptions(options.execOptions, delegate.execOptions))
  }

  /**
   * Executes command on a remote host.
   *
   * @param cmd a command to execute remotely.
   * @param arguments command arguments.
   * @return result of the command execution on a remote machine. The result is an instance of <code>CommandOutput</code>.
   */
  CommandOutput exec(String cmd, String... arguments) {
    doExec(cmd, new ExecOptions(options.execOptions), arguments)
  }

  /**
   * Executes command on a remote host.
   *
   * @param execOptions execution options
   * @return result of the command execution on a remote machine. The result is an instance of <code>CommandOutput</code>.
   */
  CommandOutput exec(Map execOptions) {
    if (!execOptions?.command) {
      throw new WinRMException("The 'command' parameter is not specified!")
    }
    doExec(execOptions.command, new ExecOptions(options.execOptions, execOptions), execOptions?.arguments as String[])
  }

  /**
   * Execute the specified command and returns a boolean to
   * signal if the command execution was successful.
   *
   * @param cmd a command to execute remotely.
   * @return true , if command was successful.
   */
  boolean ok(String cmd, String... arguments) {
    def result = doExec(cmd, new ExecOptions([failOnError: false, showOutput: false]), arguments)
    result.exitStatus == 0
  }

  /**
   * Execute the specified command and returns a boolean to
   * signal if the command execution was unsuccessful.
   *
   * @param cmd a command to execute remotely.
   * @return true if command was unsuccessful.
   */
  boolean fail(String cmd, String... arguments) {
    !ok(cmd, arguments)
  }

  private CommandOutput doExec(String cmd, ExecOptions options, String[] arguments = []) {
    connect()
    String commandId = null
    catchExceptions(options, commandId) {
      commandId = client.executeCommand(cmd, arguments)
      return getCommandExecutionResults(options, commandId)
    }
  }

  private CommandOutput catchExceptions(ExecOptions options, String commandId, Closure cl) {
    try {
      return cl()
    } catch (TimeoutException e) {
      stopExecution(commandId)
      failWithTimeout(options)
    } catch (WinRMException e) {
      stopExecution(commandId)
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

  private CommandOutput failWithTimeout(ExecOptions options) {
    setChanged(true)
    if (options.failOnError) {
      throw new WinRMException("Session timeout!")
    } else {
      logger.warn("Session timeout!")
      return new CommandOutput(-1, "Session timeout!")
    }
  }

  private CommandOutput getCommandExecutionResults(ExecOptions options, String commandId) {

    def commandExecOutput = null
    Thread thread = new Thread() {
      void run() {
        for (; !isInterrupted();) {
          commandExecOutput = client.commandExecuteResults(commandId)
          if (!commandExecOutput.commandRunning) {
            break
          }
        }
      }
    }
    thread.start()

    try {
      thread.join(options.maxWait)
    } catch (InterruptedException e) {
      thread.interrupt()
      throw new Exception(e)
    }

    if (thread.isAlive()) {
      thread.interrupt()
      throw new TimeoutException()
    }

    new CommandOutput(commandExecOutput.exitStatus,
        commandExecOutput.failed() ? commandExecOutput.errorOutput : commandExecOutput.output,
        commandExecOutput.exception)

  }

  private void stopExecution(String commandId) {
    try {
      if (options.verbose) {
        logger.warn("Stopping command execution with id = [$commandId]")
      }
      client.cleanupCommand(commandId)
      client.deleteShell()
    } catch (Exception e) {
      logger.warn("Error occurred during stopping command execution [${e.getMessage()}]")
    }
  }
  ////////////////////////////////////////////////////////////////////////////////////////////////
  //    ____ _____
  //  / ____|  __ \
  // | |    | |__) |
  // | |    |  ___/
  // | |____| |
  //  \_____|_|
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Copies file defined by file path from a local machine to a remote machine represented by file path
   *
   * @param sourceFile string points to the file on a machine from which script is run
   * @param dst name of the directory where to place a source file of a remote machine
   */
  def cp(String sourceFile, String dst) {
    if (options.verbose) {
      logger.info("Copy local file represented by string [$sourceFile] to remote folder represented by string [$dst]")
    }
    cp(new File(sourceFile), dst)
  }

  /**
   * Copies <code>File</code> instance from a local machine to a remote machine represented by file path
   *
   * @param sourceFile string points to the file on a machine from which script is run
   * @param dst name of the directory where to place a source file of a remote machine
   */
  def cp(File sourceFile, String dst) {
    if (options.verbose) {
      logger.info("Copy local file[${sourceFile.name}] to remote destination [$dst]")
    }
    CopyOptionsDelegate copySpec = new CopyOptionsDelegate()
    copySpec.with {
      from { localFile(sourceFile) }
      into { remoteDir(dst) }
    }
    upload(copySpec)
  }

  def cp(@DelegatesTo(strategy = DELEGATE_FIRST, value = CopyOptionsDelegate) Closure cl) {
    CopyOptionsDelegate copySpec = new CopyOptionsDelegate()
    cl.delegate = copySpec
    cl.resolveStrategy = DELEGATE_FIRST
    cl()
    if (!isCopyTypesDefined(copySpec)) {
      throw new WinRMException("Either copying source (from) or target (into) is of unknown type!")
    }
    if (!isSourceTargetDifferent(copySpec)) {
      throw new WinRMException("Copying source (from) and target (into) shouldn't be both local or both remote!")
    }

    if (copySpec.source.type == LOCAL) {
      upload(copySpec)
    } else if (copySpec.source.type == REMOTE) {
      download(copySpec)
    }
  }

  private static boolean isCopyTypesDefined(CopyOptionsDelegate copySpec) {
    copySpec?.source?.type && copySpec?.source?.type != UNKNOWN && copySpec?.target?.type && copySpec.target.type != UNKNOWN
  }

  private static boolean isSourceTargetDifferent(CopyOptionsDelegate copySpec) {
    copySpec.source.type != copySpec.target.type
  }

  private void download(CopyOptionsDelegate copySpec) {
    if (options.verbose) {
      logger.info("> Downloading remote file(s)")
    }

    // Download remote files.
    copySpec.source.remoteFiles.each { String srcFile ->
      copySpec.target.localDirs.each { File dstDir ->
        dstDir.mkdirs()
        doGet(srcFile, new File(dstDir.canonicalPath, getName(srcFile)))
      }
      copySpec.target.localFiles.each { File dstFile ->
        dstFile.parentFile.mkdirs()
        doGet(srcFile, dstFile)
      }
    }

    // Download remote directories.
    copySpec.source.remoteDirs.each { String srcDir ->
      remoteEachFileRecurse(srcDir) { String srcFile ->
        copySpec.target.localDirs.each { File dstDir ->
          def dstFile = new File(dstDir.canonicalPath, relativePath(srcDir, srcFile))
          dstFile.parentFile.mkdirs()
          doGet(srcFile, new File(dstDir.canonicalPath, relativePath(srcDir, srcFile)))
        }
      }
      copySpec.target.localFiles.each { File dstFile ->
        logger.warn("Can't copy remote directory ($srcDir) to a local file (${dstFile.path})!")
      }
    }
  }

  private void upload(CopyOptionsDelegate copySpec) {
    if (options.verbose) {
      logger.info("> Uploading file(s)")
    }
    def remoteDirs = copySpec.target.remoteDirs
    def remoteFiles = copySpec.target.remoteFiles
    createRemoteFolderStructure(remoteFiles, remoteDirs)
    def allLocalFiles = copySpec.source.localFiles + copySpec.source.localDirs
    uploadLocalFiles(allLocalFiles, remoteFiles, remoteDirs)
  }

  private void createRemoteFolderStructure(def remoteFiles, def remoteDirs) {
    remoteFiles.each { String dstFile ->
      def dstDir = getFullPathNoEndSeparator(dstFile)
      createRemoteDirectory(dstDir)
    }
    remoteDirs.each { String dstDir ->
      createRemoteDirectory(dstDir)
    }
  }

  private void uploadLocalFiles(def allLocalFiles, def remoteFiles, def remoteDirs) {
    if (options.verbose) {
      logger.info("> Uploading local file(s)")
    }
    allLocalFiles.each { File sourcePath ->
      if (sourcePath.isDirectory()) {
        sourcePath.eachFileRecurse { File childPath ->
          def relativePath = relativePath(sourcePath, childPath)
          remoteDirs.each { String dstDir ->
            if (childPath.isDirectory()) {
              def dstParentDir = separatorsToWindows(concat(dstDir, relativePath))
              createRemoteDirectory(dstParentDir)
            } else {
              def dstPath = separatorsToWindows(concat(dstDir, relativePath))
              doPut(childPath.canonicalFile, dstPath)
            }
          }
        }
      } else {
        remoteDirs.each { String dstDir ->
          def dstPath = separatorsToWindows(concat(dstDir, sourcePath.name))
          doPut(sourcePath, dstPath)
        }
        remoteFiles.each { String dstFile ->
          doPut(sourcePath, dstFile)
        }
      }
    }
  }

  private void remoteEachFileRecurse(String remoteDir, Closure cl) {
    logger.info("> Getting file list from ${remoteDir} directory")
    List<RemoteFile> entries = remoteFile(separatorsToWindows(remoteDir)).listFiles()
    entries.each { RemoteFile file ->
      def childPath = separatorsToWindows(concat(remoteDir, file.name))
      if (file.isDirectory()) {
        if (!(file.name in ['.', '..'])) {
          remoteEachFileRecurse(childPath, cl)
        }
      } else {
        cl(childPath)
      }
    }
  }

  private void doPut(File srcFile, String dst) {
    logger.info("> ${srcFile.canonicalPath} => ${dst}")
    try {
      def newInputStream = srcFile.newInputStream()
      def outputStream = remoteFile(dst).outputStream
      outputStream << newInputStream
      outputStream.close()
      newInputStream.close()
    } catch (IOException e) {
      throw new WinRMException("File [$srcFile.canonicalPath] upload failed with a message $e.message")
    }
  }

  private void doGet(String srcFile, File dstFile) {
    logger.info("> ${srcFile} => ${dstFile.canonicalPath}")
    try {
      def newOutputStream = dstFile.newOutputStream()
      def inputStream = remoteFile(srcFile).inputStream
      newOutputStream << inputStream
      newOutputStream.close()
      inputStream.close()
    } catch (IOException e) {
      throw new WinRMException("File [$dstFile.canonicalPath] download failed with a message $e.message")
    }
  }

  private static String relativePath(File parent, File child) {
    separatorsToWindows(child.canonicalPath.replace(parent.canonicalPath, '')).replaceAll('^\\\\', '')
  }

  private static String relativePath(String parent, String child) {
    normalizeNoEndSeparator(child)
        .replace(normalizeNoEndSeparator(parent) + File.separatorChar, '')
        .replace(File.separatorChar.toString(), '\\')
  }

  private void createRemoteDirectory(String dstFile) {
    if (options.verbose) {
      logger.debug("> Check if $dstFile exists")
    }
    def file = remoteFile(dstFile)
    boolean dirExists = file.exists()
    if (!dirExists) {
      logger.debug("Creating remote directory: $dstFile")
      exec('mkdir', dstFile)
    }
  }
}