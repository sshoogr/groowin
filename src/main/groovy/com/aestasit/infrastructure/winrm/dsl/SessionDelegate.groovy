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
import com.aestasit.infrastructure.winrm.exception.WinRMException
import com.aestasit.infrastructure.winrm.WinRMOptions
import com.aestasit.infrastructure.winrm.client.WinRMClient
import com.aestasit.infrastructure.winrm.log.Logger
import com.aestasit.infrastructure.winrm.log.Slf4jLogger

import java.util.regex.Pattern

import static org.apache.commons.io.FilenameUtils.*

import static com.aestasit.infrastructure.winrm.dsl.FileSetType.UNKNOWN
import static groovy.lang.Closure.DELEGATE_FIRST

import static com.aestasit.infrastructure.winrm.client.util.Constants.*
import static com.aestasit.infrastructure.winrm.dsl.FileSetType.*
import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator
import static org.apache.commons.io.FilenameUtils.separatorsToWindows


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

  void setHost(String host) {
    this.host = host
  }

  void setPort(int port) {
    this.port = port
  }

  void setUser(String username) {
    this.username = username
  }

  void setPassword(String password) {
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

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //   ________   ________ _____
  //  |  ____\ \ / /  ____/ ____|
  //  | |__   \ V /| |__ | |
  //  |  __|   > < |  __|| |
  //  | |____ / . \| |___| |____
  //  |______/_/ \_\______\_____|
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  def connectWinRM(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    println "protocol = ${protocol}"
    println "host = ${host}"
    println "port = ${port}"
    println "username = ${username}"
    println "password = ${password}"

    WinRMClient client = new WinRMClient(protocol, host, port, username, password)
    client.initialize()
    cl.delegate = this
    cl.resolveStrategy = DELEGATE_FIRST
    cl(client)
  }

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
    println "cmd = $cmd"
    println "arguments = $arguments"

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
    CommandOutput output = null

    connectWinRM { WinRMClient client ->
      output = catchExceptions(options) {
        executeCommand(cmd, options, client, arguments)
      }
    }
    output
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

  private CommandOutput executeCommand(String cmd, ExecOptions options, WinRMClient client, String[] args = []) {
    def commandToLog = "$cmd ${args ? args.join(' ') : ''}"
    if (options.showCommand) {
      logger.info("> $commandToLog")
    }

    com.aestasit.infrastructure.winrm.client.CommandOutput output = client.execute(cmd, args)
    if (output.failed()) {
      if(output.exception){
        throw new WinRMException(output.exception)
      } else{
        throw new WinRMException("Executing command line [$commandToLog] failed. Error text is [$output.errorOutput]")
      }
    }

    if (options.showOutput && output.output) {
      logger.info(output.output)
    }

    new CommandOutput(0, output.output)
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
      logger.debug("Copy local file represented by string $sourceFile to remote folder represented by string $dst")
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
    logger.debug 'Copy local file to remote destination'
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
    validateCopySpec(copySpec)

    if (copySpec.source.type == LOCAL) {
      upload(copySpec)
    } else if (copySpec.source.type == REMOTE) {
      download(copySpec)
    }
  }

  private void validateCopySpec(CopyOptionsDelegate copySpec) {
    if (copySpec.source.type == null || copySpec.source.type == UNKNOWN ||
            copySpec.target.type == null || copySpec.target.type == UNKNOWN) {
      throw new WinRMException("Either copying source (from) or target (into) is of unknown type!")
    }
    if (copySpec.source.type == copySpec.target.type) {
      throw new WinRMException("Copying source (from) and target (into) shouldn't be both local or both remote!")
    }
  }

  private void download(CopyOptionsDelegate copySpec) {
    logger.info("> Downloading remote file(s)")

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
    def remoteDirs = copySpec.target.remoteDirs
    def remoteFiles = copySpec.target.remoteFiles
    createRemoteFolderStructure(remoteFiles, remoteDirs)
    // Upload local files and directories.
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
    logger.info("> Uploading local file(s)")
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
    def newOutputStream = dstFile.newOutputStream()
    def inputStream = remoteFile(srcFile).inputStream
    newOutputStream << inputStream
    newOutputStream.close()
    inputStream.close()
  }

  static private String relativePath(File parent, File child) {
    separatorsToWindows(child.canonicalPath.replace(parent.canonicalPath, '')).replaceAll('^\\\\', '')
  }

  static private String relativePath(String parent, String child) {
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