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

import com.aestasit.infrastructure.winrm.WinRMException
import com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection
import com.xebialabs.overthere.cifs.CifsFile

import static FileSetType.*
import static groovy.lang.Closure.DELEGATE_FIRST
import static org.apache.commons.io.FilenameUtils.*

/**
 * The <code>CopyMethods</code> class provides
 * methods for copying files from local to remote machines and vice versa.
 *
 * On a remote machine one of the Windows family OSs has to be installed.
 * A local machine does not have restrictions on the installed OS.
 *
 * Communication is done based on CIFS protocol.
 *
 * @author Sergey Korenko
 */
class CopyMethods {

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
    cifsConnection { CifsWinRmConnection connection ->
      CopyOptionsDelegate copySpec = new CopyOptionsDelegate()
      copySpec.with {
        from { localFile(sourceFile) }
        into { remoteDir(dst) }
      }
      upload(copySpec, connection)
    }
  }

  def cp(@DelegatesTo(strategy = DELEGATE_FIRST, value = CopyOptionsDelegate) Closure cl) {
    CopyOptionsDelegate copySpec = new CopyOptionsDelegate()
    cl.delegate = copySpec
    cl.resolveStrategy = DELEGATE_FIRST
    cl()
    validateCopySpec(copySpec)
    cifsConnection { CifsWinRmConnection connection ->
      if (copySpec.source.type == LOCAL) {
        upload(copySpec, connection)
      } else if (copySpec.source.type == REMOTE) {
        download(copySpec, connection)
      }
    }
  }

  private void validateCopySpec(CopyOptionsDelegate copySpec) {
    if (copySpec.source.type == null || copySpec.source.type == UNKNOWN ||
        copySpec.target.type == null || copySpec.target.type == UNKNOWN) {
      throw new WinRMException("Either copying source (from) or target (into) is of unknown type!")
    }
    if (copySpec.source.type == copySpec.target.type) {
      throw new WinRMException("copying source (from) and target (into) shouldn't be both local or both remote!")
    }
  }

  private void download(CopyOptionsDelegate copySpec, CifsWinRmConnection connection) {
    logger.info("> Downloading remote file(s)")

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

  private void upload(CopyOptionsDelegate copySpec, CifsWinRmConnection connection) {
    def remoteDirs = copySpec.target.remoteDirs
    def remoteFiles = copySpec.target.remoteFiles
    createRemoteFolderStructure(remoteFiles, remoteDirs, connection)
    // Upload local files and directories.
    def allLocalFiles = copySpec.source.localFiles + copySpec.source.localDirs
    uploadLocalFiles(allLocalFiles, remoteFiles, remoteDirs, connection)
  }

  private void createRemoteFolderStructure(def remoteFiles, def remoteDirs, CifsWinRmConnection connection) {
    remoteFiles.each { String dstFile ->
      def dstDir = getFullPathNoEndSeparator(dstFile)
      createRemoteDirectory(dstDir, connection)
    }
    remoteDirs.each { String dstDir ->
      createRemoteDirectory(dstDir, connection)
    }
  }

  private void uploadLocalFiles(def allLocalFiles, def remoteFiles, def remoteDirs, CifsWinRmConnection connection) {
    logger.info("> Uploading local file(s)")
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

  private void remoteEachFileRecurse(String remoteDir, CifsWinRmConnection connection, Closure cl) {
    logger.info("> Getting file list from ${remoteDir} directory")
    List<CifsFile> entries = connection.getFile(separatorsToWindows(remoteDir)).listFiles()
    entries.each { CifsFile entry ->
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

  private void doPut(File srcFile, String dst, CifsWinRmConnection connection) {
    logger.info("> ${srcFile.canonicalPath} => ${dst}")
    try {
      def outputStream = connection.getFile(dst).outputStream
      outputStream << srcFile.newInputStream()
    } catch (IOException e) {
      throw new WinRMException("File [$srcFile.canonicalPath] upload failed with a message $e.message")
    }
  }

  private void doGet(String srcFile, File dstFile, CifsWinRmConnection connection) {
    logger.info("> ${srcFile} => ${dstFile.canonicalPath}")
    dstFile.newOutputStream() << connection.getFile(srcFile).inputStream
  }

  static private String relativePath(File parent, File child) {
    separatorsToWindows(child.canonicalPath.replace(parent.canonicalPath, '')).replaceAll('^\\\\', '')
  }

  static private String relativePath(String parent, String child) {
    normalizeNoEndSeparator(child)
        .replace(normalizeNoEndSeparator(parent) + File.separatorChar, '')
        .replace(File.separatorChar.toString(), '\\')
  }


  private void createRemoteDirectory(String dstFile, CifsWinRmConnection connection) {
    if (options.verbose) {
      logger.debug("> Check if $dstFile exists")
    }
    boolean dirExists = connection.getFile(dstFile).exists()
    if (!dirExists) {
      logger.debug("Creating remote directory: $dstFile")
      mkdir(dstFile, connection)
    }
  }

  private void mkdir(String folderToCreate, CifsWinRmConnection connection) {
    if (options.verbose) {
      logger.info("> make directory ${folderToCreate}")
    }
    connection.getFile(folderToCreate).mkdir()
  }
}
