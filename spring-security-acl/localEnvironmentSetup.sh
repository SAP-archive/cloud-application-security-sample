#!/usr/bin/env bash
echo "Hint: run script with 'source localEnvironmentSetup.sh'"
echo "This script prepares the current shell's environment variables (not permanently)"

export SPRING_PROFILES_ACTIVE='cloud,uaamock'
export VCAP_APPLICATION='{}'
export VCAP_SERVICES='{"postgresql":[{"credentials":{"dbname":"test","hostname":"127.0.0.1","password":"test123!","port":"5432","uri":"postgres://testuser:test123!@localhost:5432/test","username":"testuser"},"label":"postgresql","name":"postgres-bulletinboard-ads","plan":"v9.4-dev","tags":["postgresql","relational"]}],"xsuaa":[{"credentials":{"clientid":"sb-bulletinboard!t400","clientsecret":"dummy-clientsecret","identityzone":"uaa","identityzoneid":"1e505bb1-2fa9-4d2b-8c15-8c3e6e6279c6","url":"dummy-url","verificationkey":"-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2G5MUF7szUMJfiaghYeb\nwB+BgaG4wkbIDZ5fJU8Zm0/WnaupCCKX0fguLC21FtHATC6SOpOuVClXe9GY9AVJ\nq3nyqAsiUil66jH9Y+kmeLeRVoBp8KXMQ15+W69GNU7/sYv+0k5PLUaxJPmcwb+W\nCq9hw76zRXeEijnZ41YlVC9jcnZ7IjHjp2BASoznImmGJDW6F30FRbP/MLtfv5fM\npj17OziVNE+eacuIygSH0IZZ+wvV7AcJAZlEwkCFqzzbVx2cLvRIpacHz2ci4seI\nIxdPRj8O7i4y29hdSsHqTRFLtQiwCgIr7YItA5voVY/bS+CYy8a1MSckdXvFa5jY\newIDAQAB\n-----END PUBLIC KEY-----","xsappname":"bulletinboard!t400"},"label":"xsuaa","name":"uaa-bulletinboard","plan":"application","tags":["xsuaa"]}]}'

echo \$VCAP_SERVICES=${VCAP_SERVICES}


