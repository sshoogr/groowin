# The contents below were provided by the Packer Vagrant post-processor
Vagrant.configure("2") do |config|

  config.vm.define "win_rm_host" do |win_rm_host|
      win_rm_host.winrm.username = "vagrant"
      win_rm_host.winrm.password = "vagrant"

      win_rm_host.vm.box = "aestasit/2008r2"

      win_rm_host.vm.guest = :windows

      win_rm_host.vm.network "private_network", ip: "192.168.25.25"
      win_rm_host.vm.network :forwarded_port, guest: 5985, host: 5985, id: "winrm", auto_correct: true

      win_rm_host.vm.provider :virtualbox do |v, override|
        v.gui = true
        v.customize ["modifyvm", :id, "--memory", 700]
      end
  end

  config.vm.define "linux_integration_test" do |linux_integration_test|

    linux_integration_test.vm.box = "ubuntu/trusty64"

    linux_integration_test.vm.provider "virtualbox" do |vb|
      vb.gui = true
      vb.customize ["modifyvm", :id, "--memory", "700"]
    end

    linux_integration_test.vm.network "private_network", ip: "192.168.25.26"
    linux_integration_test.vm.network "forwarded_port", guest: 22, host: 2323, auto_correct: true, id: "ssh"

    linux_integration_test.vm.provision "shell", inline: "sudo apt-get update", privileged: false
    linux_integration_test.vm.provision "shell", inline: "sudo apt-get install -y openjdk-7-jdk", privileged: false
    linux_integration_test.vm.provision "shell", inline: "sudo apt-get install -y curl", privileged: false
    linux_integration_test.vm.provision "shell", inline: "sudo apt-get install unzip", privileged: false
    linux_integration_test.vm.provision "shell", inline: "sudo apt-get install -y git", privileged: false
    linux_integration_test.vm.provision "shell", inline: "git clone https://github.com/aestasit/groowin", privileged: false
    linux_integration_test.vm.provision "shell", inline: "curl -s get.gvmtool.net | bash", privileged: false
    linux_integration_test.vm.provision "shell", inline: "echo \"gvm_auto_answer=true\" >> ~/.gvm/etc/config", privileged: false
    linux_integration_test.vm.provision "shell", inline: 'source "/home/vagrant/.gvm/bin/gvm-init.sh" && gvm i groovy 2.3.6', privileged: false
    linux_integration_test.vm.provision "shell", inline: 'source "/home/vagrant/.gvm/bin/gvm-init.sh" && gvm i gradle 2.1', privileged: false
    linux_integration_test.vm.provision "shell", inline: "cd /home/vagrant/groowin && gradle clean integration", privileged: false
  end

  config.vm.define "linux_integration_test", autostart: false

end

