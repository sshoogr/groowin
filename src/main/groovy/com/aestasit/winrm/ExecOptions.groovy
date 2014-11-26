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

/**
 * Configuration object holding options used for EXEC (remote command execution) functionality.
 *
 * @author Andrey Adamovich
 *
 */
class ExecOptions extends CommonOptions {

  Boolean showOutput = true
  Boolean showCommand = true

  ExecOptions() {
  }

  ExecOptions(ExecOptions opt1) {
    this.failOnError = setValue(opt1?.failOnError, true)
    this.showOutput = setValue(opt1?.showOutput, true)
    this.showCommand = setValue(opt1?.showCommand, true)
  }

  ExecOptions(ExecOptions opt1, ExecOptions opt2) {
    this(opt1, opt2?.properties)
  }

  ExecOptions(ExecOptions opt1, Map opt2) {
    this.failOnError = setValue(opt2?.failOnError, opt1?.failOnError, true)
    this.showOutput = setValue(opt2?.showOutput, opt1?.showOutput, true)
    this.showCommand = setValue(opt2?.showCommand, opt1?.showCommand, true)
  }
}
