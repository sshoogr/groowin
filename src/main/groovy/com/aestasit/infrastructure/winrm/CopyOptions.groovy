/*
 * Copyright (C) 2011-2015 Aestas/IT
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

package com.aestasit.infrastructure.winrm

/**
 * Configuration object holding options used for CIFS (remote file copying) functionality.
 *
 * @author Andrey Adamovich
 *
 */
class CopyOptions extends CommonOptions {
  boolean showProgress = true

  CopyOptions() {
  }

  CopyOptions(CopyOptions opt1) {
    this.failOnError  = setValue(opt1?.failOnError, true)
    this.showProgress = setValue(opt1?.showProgress, true)
  }

  CopyOptions(CopyOptions opt1, CopyOptions opt2) {
    this(opt1, opt2?.properties)
  }

  CopyOptions(CopyOptions opt1, Map opt2) {
    this.failOnError   = setValue(opt2?.failOnError, opt1?.failOnError, true)
    this.showProgress  = setValue(opt2?.showProgress, opt1?.showProgress, true)
  }
}