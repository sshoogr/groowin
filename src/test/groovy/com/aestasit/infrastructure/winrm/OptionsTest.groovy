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

import org.junit.Test

/**
 * Options test
 *
 * @author Andrey Adamovich
 * @author Sergey Korenko
 */
class OptionsTest {

  @Test
  public void testExecOptions() throws Exception {
    def defaultOpts = new ExecOptions()
    assert defaultOpts.failOnError
    assert defaultOpts.showOutput
    def opts = new ExecOptions(defaultOpts, [ failOnError: false ] )
    assert !opts.failOnError
  }

  @Test
  public void testCopyOptions() throws Exception {
    CopyOptions defaultOpts = new CopyOptions()
    assert defaultOpts.failOnError
    assert defaultOpts.showProgress
    CopyOptions opts = new CopyOptions(defaultOpts, [ showProgress: false ] )
    assert !opts.showProgress
  }
}