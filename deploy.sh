#!/bin/sh
##   ____  _
##  / __ \| |
## | |  | | |_   _ _ __ ___  _ __   __ _
## | |  | | | | | | '_ ` _ \| '_ \ / _` |
## | |__| | | |_| | | | | | | |_) | (_| |
##  \____/|_|\__, |_| |_| |_| .__/ \__,_|
##            __/ |         | |
##           |___/          |_|
##
## Script to deploy api with good version
## Author > Tristiisch
#
# ./deploy.sh date "master@2021-02-26 18:30:00"
# ./deploy.sh master
# ./deploy.sh dev

# DÉPENDANCES
(cd /home/repo/olympaapi/ && sh ./deploy.sh $1
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mErreur > Arrêt de la création des JAR\e[0m"; exit 1
fi
)

# PARAMETRES
PLUGIN_NAME="core"
USE_BRANCH="master dev"
ACTUAL_COMMIT_ID=`cat target/commitId`
ACTUAL_COMMIT_ID_API=`cat target/commitIdAPI`

if [ -n "$1" ]; then
	if [ -n "$2" ] && [ "$1" = "date" ]; then
		DATE="$2"
	else
		BRANCH_NAME="$1"
		SERV="$2"
	fi
else
	echo -e "\e[0;36mTu peux choisir la version du $PLUGIN_NAME en ajoutant une date (ex './deploy.sh date \"2021-02-26 18:30:00\"') ou une branch (ex './deploy.sh dev').\e[0m"
fi
git pull --all
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mEchec du git pull, tentative de git reset\e[0m"
	git reset --hard HEAD
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git reset !\e[0m" && rm target/commit*; exit 1
	fi
	git pull --all
	if [ "$?" -ne 0 ]; then
		git checkout $BRANCH_NAME --force
		if [ "$?" -ne 0 ]; then
			echo -e "\e[91mEchec du git pull & checkout !\e[0m" && rm target/commit*; exit 1
		fi
	fi
fi
if [ -n "$BRANCH_NAME" ]; then
	git checkout $BRANCH_NAME --force
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mLa branch $BRANCH_NAME n'existe pas !\e[0m"; exit 1
	fi
fi
if [ -n "$DATE" ] && [ "$DATE" != "" ]; then
	git checkout '$DATE' --force
elif [ -z "$BRANCH_NAME" ]; then
	git checkout master --force
fi
if [ -n "$ACTUAL_COMMIT_ID" ] && [ -n "$ACTUAL_COMMIT_ID_API" ]; then
	if [ "$ACTUAL_COMMIT_ID" = `git rev-parse HEAD` ]; then
		if [ "$ACTUAL_COMMIT_ID_API" = `cd ../olympaapi && git rev-parse HEAD` ]; then
			echo -e "\e[32mPas besoin de maven install le $PLUGIN_NAME, l'api est up & le jar est déjà crée.\e[0m"
			exit 0
		else
			echo -e "\e[32mIl faut build le $PLUGIN_NAME, l'api a été up.\e[0m"
		fi
	fi
fi
mvn install
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mLe build du $PLUGIN_NAME a échoué !\e[0m" && rm target/commit*; exit 1
else
	echo `git rev-parse HEAD` > target/commitId
	echo `cd ../olympaapi && git rev-parse HEAD` > target/commitIdAPI
fi
echo -e "\e[32mLe jar du commit de l'api $(cat target/commitIdAPI) avec le $PLUGIN_NAME $(cat target/commitId) a été crée.\e[0m"
