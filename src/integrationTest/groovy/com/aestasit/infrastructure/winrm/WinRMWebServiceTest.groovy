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

import groovyx.net.http.HTTPBuilder
import org.apache.http.HttpRequest
import org.apache.http.client.methods.HttpRequestBase

import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

import org.junit.Test

class WinRMWebServiceTest {

  @Test
  void testWebService() {

    def builder = new HTTPBuilder('http://192.168.56.101:5985/wsman')
    builder.auth.basic('vagrant', 'vagrant')
    // builder.autoAcceptHeader = true


    println builder.post(headers: [SOAPAction: 'Shell'], contentType: 'application/soap+xml', body: '''<?xml version = "1.0" ?>
<env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
  <env:Header>
    <a:To xmlns:a="http://schemas.xmlsoap.org/ws/2004/08/addressing">http://192.168.56.101:5985/wsman</a:To>
    <a:ReplyTo xmlns:a="http://schemas.xmlsoap.org/ws/2004/08/addressing">
      <a:Address mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:Address>
    </a:ReplyTo>
    <w:MaxEnvelopeSize xmlns:w="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd"
                       mustUnderstand="true">153600</w:MaxEnvelopeSize>
    <a:MessageID xmlns:a="http://schemas.xmlsoap.org/ws/2004/08/addressing">uuid:F21BEA7F-64A8-4D12-A1B2-7634E9B7E7F9</a:MessageID>
    <w:Locale xmlns:w="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd"
              mustUnderstand="false"
              xml:lang="en-US" />
    <p:DataLocale xmlns:p="http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd"
                  mustUnderstand="false"
                  xml:lang="en-US" />
    <w:OperationTimeout xmlns:w="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd">PT600.0S</w:OperationTimeout>
    <a:Action xmlns:a="http://schemas.xmlsoap.org/ws/2004/08/addressing"
              mustUnderstand="true">http://schemas.xmlsoap.org/ws/2004/09/transfer/Create</a:Action>
    <w:ResourceURI xmlns:w="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd"
                   mustUnderstand="true">http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd</w:ResourceURI>
    <w:OptionSet xmlns:w="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd">
      <w:Option Name="WINRS_NOPROFILE">FALSE</w:Option>
      <w:Option Name="WINRS_CODEPAGE">437</w:Option>
    </w:OptionSet>
  </env:Header>
  <env:Body>
    <rsp:Shell xmlns:rsp="http://schemas.microsoft.com/wbem/wsman/1/windows/shell">
      <rsp:InputStreams>stdin</rsp:InputStreams>
      <rsp:OutputStreams>stdout stderr</rsp:OutputStreams>
    </rsp:Shell>
  </env:Body>
</env:Envelope>''')
  }

}
