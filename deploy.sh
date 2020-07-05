(cd /home/repo/olympaapi/ && sh ./deploy.sh) && git pull && mvn install && cp target/Olympa*.jar /home/serveurs/$1/plugins/ && mc restart $1
