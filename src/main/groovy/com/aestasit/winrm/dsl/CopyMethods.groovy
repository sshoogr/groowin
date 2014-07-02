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

import com.aestasit.winrm.WinRMException
import com.xebialabs.overthere.*

import static com.aestasit.winrm.dsl.FileSetType.*
import static org.apache.commons.codec.digest.DigestUtils.*
import static org.apache.commons.io.FilenameUtils.*

/**
 * The <code>CopyMethods</code> class provides
 * methods for copying files from local to remote machines and vise versa.
 *
 * On a remote machine one of the Windows family OSs has to be installed.
 * A local machine does not have restrictions on the installed OS
 *
 * Communication is done based on CIFS protocol
 *
 * <p>
 * @author Sergey Korenko
 */
class CopyMethods {

  /**
   * Copies file from a local machine to a remote machine represented by file names
   *
   * @param sourceFile string points to the file on a machine from which script is run
   * @param dst name of the directory where to place a source file of a remote machine
   */
  def copying(String sourceFile, String dst) {
    if (options.verbose) {
      logger.debug("Copy local file represented by string $sourceFile to remote folder represented by string $dst")
    }

    copying(new File(sourceFile), dst)
  }

  def copying(File sourceFile, String dst) {
    if (options.verbose) {
      logger.debug("Copy local file represented by File object with path $sourceFile to remote folder represented by string $dst")
    }

    cifsConnection { OverthereConnection connection ->
      CopyOptionsDelegate copySpec = new CopyOptionsDelegate()
      copySpec.with {
        from { localFile(sourceFile) }
        into { remoteDir(dst) }
      }
      upload(copySpec, connection)
    }
  }

  def copying(Closure cl) {
    if (options.verbose) {
      logger.debug("Copying with closure")
    }

    CopyOptionsDelegate copySpec = new CopyOptionsDelegate()
    cl.delegate = copySpec
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    validateCopySpec(copySpec)

    cifsConnection { OverthereConnection connection ->
      if (copySpec.source.type == LOCAL) {
        upload(copySpec, connection)
      } else if (copySpec.source.type == REMOTE) {
        download(copySpec, connection)
      }
    }
  }

  private void validateCopySpec(CopyOptionsDelegate copySpec) {
    if (options.verbose) {
      logger.debug("Checking if source and destination parameters are defined correctly")
    }

    if (copySpec.source.type == null || copySpec.source.type == UNKNOWN ||
            copySpec.target.type == null || copySpec.target.type == UNKNOWN) {
      throw new WinRMException("Either copying source (from) or target (into) is of unknown type!")
    }
    if (copySpec.source.type == copySpec.target.type) {
      throw new WinRMException("copying source (from) and target (into) shouldn't be both local or both remote!")
    }
  }

  private void download(CopyOptionsDelegate copySpec, OverthereConnection connection) {
    // Download remote files.
    copySpec.source.remoteFiles.each { String srcFile ->
      copySpec.target.localDirs.each { File dstDir ->
        dstDir.mkdirs()
        doGet(srcFile, new File(dstDir.canonicalPath, getName(srcFile)), connection)
      }
      copySpec.target.localFiles.each { File dstFile ->
        dstFile.parentFile.mkdirs()
        doGet(srcFile, dstFile, connection)
      }
    }

    // Download remote directories.
    copySpec.source.remoteDirs.each { String srcDir ->
      remoteEachFileRecurse(srcDir, connection) { String srcFile ->
        copySpec.target.localDirs.each { File dstDir ->
          def dstFile = new File(dstDir.canonicalPath, relativePath(srcDir, srcFile))
          dstFile.parentFile.mkdirs()
          doGet(srcFile, new File(dstDir.canonicalPath, relativePath(srcDir, srcFile)), connection)
        }
      }
      copySpec.target.localFiles.each { File dstFile ->
        logger.warn("Can't copy remote directory ($srcDir) to a local file (${dstFile.path})!")
      }
    }
  }

  private void upload(CopyOptionsDelegate copySpec, OverthereConnection connection) {
    if (options.verbose) {
      logger.debug("BEGIN Uploading files from a local machine to a remote machine")
    }

    def remoteDirs = copySpec.target.remoteDirs
    def remoteFiles = copySpec.target.remoteFiles

    createRemoteFolderStructure(remoteFiles, remoteDirs, connection)

    // Upload local files and directories.
    def allLocalFiles = copySpec.source.localFiles + copySpec.source.localDirs
    uploadLocalFiles( allLocalFiles, remoteFiles, remoteDirs, connection)
  }

  private void createRemoteFolderStructure(def remoteFiles, def remoteDirs, OverthereConnection connection){
    if (options.verbose) {
      logger.debug("Creation on a remote machine file structure that is " +
              "identical to the file structure on a local machine and which is defined by input parameters of copy procedure")
    }

    remoteFiles.each { String dstFile ->
      def dstDir = getFullPathNoEndSeparator(dstFile)
      createRemoteDirectory(dstDir, connection)
    }
    remoteDirs.each { String dstDir ->
      createRemoteDirectory(dstDir, connection)
    }
  }

  private void uploadLocalFiles(def allLocalFiles, def remoteFiles, def remoteDirs, OverthereConnection connection) {
    if (options.verbose) {
      logger.info("> Uploading files into remote directory")
    }

    allLocalFiles.each { File sourcePath ->
      if (sourcePath.isDirectory()) {
        sourcePath.eachFileRecurse { File childPath ->
          def relativePath = relativePath(sourcePath, childPath)

          remoteDirs.each { String dstDir ->
            if (childPath.isDirectory()) {
              def dstParentDir = separatorsToWindows(concat(dstDir, relativePath))
              createRemoteDirectory(dstParentDir, connection)
            } else {
              def dstPath = separatorsToWindows(concat(dstDir, relativePath))
              doPut(childPath.canonicalFile, dstPath, connection)
            }
          }
        }
      } else {
        remoteDirs.each { String dstDir ->
          def dstPath = separatorsToWindows(concat(dstDir, sourcePath.name))
          doPut(sourcePath, dstPath, connection)
        }
        remoteFiles.each { String dstFile ->
          doPut(sourcePath, dstFile, connection)
        }
      }
    }
  }

  private void remoteEachFileRecurse(String remoteDir, OverthereConnection connection, Closure cl) {
    if (options.verbose) {
      logger.info("> Getting file list from ${remoteDir} directory")
    }

    List<OverthereFile> entries = connection.getFile(separatorsToWindows(remoteDir)).listFiles()
    entries.each { OverthereFile entry ->
      def childPath = separatorsToWindows(concat(remoteDir, entry.name))
      if (entry.isDirectory()) {
        if (!(entry.name in ['.', '..'])) {
          remoteEachFileRecurse(childPath, connection, cl)
        }
      } else {
        cl(childPath)
      }
    }
  }

  private void doPut(File srcFile, String dst, OverthereConnection connection) {
    if (options.verbose) {
      logger.info("> ${srcFile.canonicalPath} => ${dst}")
    }
    try {
      def outputStream = connection.getFile(dst).outputStream
      outputStream << srcFile.newInputStream()
    } catch (IOException e) {
      throw new WinRMException("File [$srcFile.canonicalPath] upload failed with a message $e.message")
    }
  }

  private void doGet(String srcFile, File dstFile, OverthereConnection connection) {
    if (options.verbose) {
      logger.info("> ${srcFile} => ${dstFile.canonicalPath}")
    }

    dstFile.newOutputStream() << connection.getFile(srcFile).inputStream
  }

  private String relativePath(File parent, File child) {
    separatorsToWindows(child.canonicalPath.replace(parent.canonicalPath, '')).replaceAll('^\\\\', '')
  }

  private String relativePath(String parent, String child) {
    normalizeNoEndSeparator(child)
            .replace(normalizeNoEndSeparator(parent) + File.separatorChar, '')
            .replace(File.separatorChar.toString(), '\\')
  }


  private void createRemoteDirectory(String dstFile, OverthereConnection connection) {
    logger.debug("> Check if $dstFile exists")

    boolean dirExists = connection.getFile(dstFile).exists()

    if (!dirExists) {
      logger.debug("Creating remote directory: $dstFile")
      mkdir(dstFile, connection)
    }
  }

  private void mkdir(String folderToCreate, OverthereConnection connection) {
    if (options.verbose) {
      logger.info("> make directory ${folderToCreate}")
    }

    connection.getFile(folderToCreate).mkdir()
  }
}
