# groowin

## Overview

The `groowin` is a **Groovy**-based **DSL** library for working with remote (Windows) servers through **WinRM**. The **DSL** allows:

- connecting,
- executing remote commands,
- copying files and directories.

The library was jointly developed by **Aestas/IT** (http://aestasit.com) and **NetCompany A/S** (http://www.netcompany.com/) to support the quickly growing company's operations and hosting department.

### Using `groowin` in Groovy scripts

The easiest way to use `groowin` in a **Groovy** script is by importing the dependency using [Grape](http://groovy.codehaus.org/Grape).

    @Grab('com.aestasit.infrastructure.groowin:groowin:0.1')
    import static com.aestasit.winrm.DefaultWinRM.*

The entry point for using the **DSL** is the `remoteSession` method, which accepts an **WinRM** **URL** and a closure with **Groovy** or **DSL** code:

    remoteSession('user2:654321@localhost:2222') {
      exec 'del C:\\temp.txt'
      remoteFile('C:\\my.conf').text = "enabled=true"
    }

For more use cases, please refer to the following sections or to the `examples` folder in this repository.

