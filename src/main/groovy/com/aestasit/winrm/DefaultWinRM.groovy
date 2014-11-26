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

package com.aestasit.winrm

import com.aestasit.winrm.dsl.SessionDelegate
import com.aestasit.winrm.dsl.WinRMDslEngine
import com.aestasit.winrm.log.Logger
import com.aestasit.winrm.log.SysOutLogger

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * Default "static" implementation of WinRM DSL to be used inside plain Groovy scripts.
 *
 * @author Andrey Adamovich
 *
 */
class DefaultWinRM {

  static WinRMOptions options = new WinRMOptions()

  static {
    options.with {
      logger = new SysOutLogger()
      verbose = true
      execOptions.with {
        showOutput = true
        showCommand = true
      }

    }
  }

  static remoteManagement(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    new WinRMDslEngine(options).remoteManagement(cl)
  }

  static execOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = ExecOptions) Closure cl) {
    options.execOptions(cl)
  }

  static ExecOptions getExecOptions() {
    options.getExecOptions()
  }

  static CopyOptions getCopyOptions() {
    options.getCopyOptions()
  }

  static copyOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = CopyOptions) Closure cl) {
    options.copyOptions(cl)
  }

  static String getDefaultHost() {
    options.getDefaultHost()
  }

  static String getDefaultPassword() {
    options.getDefaultPassword()
  }

  static boolean getVerbose() {
    options.getVerbose()
  }

  static Logger getLogger() {
    options.getLogger()
  }

  static Boolean getFailOnError() {
    options.getFailOnError()
  }

  static String getDefaultUser() {
    options.getDefaultUser()
  }

  static int getDefaultPort() {
    options.getDefaultPort()
  }

  static boolean isVerbose() {
    options.isVerbose()
  }

  static void setVerbose(boolean verbose) {
    options.setVerbose(verbose)
  }

  static void setCopyOptions(CopyOptions opts) {
    options.setCopyOptions(opts)
  }

  static void setLogger(Logger logger) {
    options.setLogger(logger)
  }

  static void setFailOnError(Boolean flag) {
    options.setFailOnError(flag)
  }

  static void setExecOptions(ExecOptions opts) {
    options.setExecOptions(opts)
  }

  static void setDefaultUser(String user) {
    options.setDefaultUser(user)
  }

  static void setDefaultPort(int port) {
    options.setDefaultPort(port)
  }

  static void setDefaultPassword(String password) {
    options.setDefaultPassword(password)
  }

  static void setDefaultHost(String host) {
    options.setDefaultHost(host)
  }
}