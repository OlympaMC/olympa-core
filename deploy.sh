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
# ./deploy.sh date "2021-02-26 18:30:00"
# ./deploy.sh master
# ./deploy.sh dev

# PARAMETRES
PLUGIN_NAME="core"
USE_BRANCH="master dev"
BASEDIR=$(dirname "$0")
ACTUAL_COMMIT_ID=`cat $BASEDIR/target/commitId`
ACTUAL_COMMIT_ID_API=`cat $BASEDIR/target/commitIdAPI`

# DÉPENDANCES

cd /home/repo/olympa-api/ && sh ./deploy.sh $1
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mErreur > Arrêt du maven build du $PLUGIN_NAME\e[0m"; exit 1
fi

cd $BASEDIR

if [ -n "$1" ]; then
	if [ -n "$2" ] && [ "$2" = "date" ]; then
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
	echo -e "\e[91mEchec du git pull pour $PLUGIN_NAME, tentative de git reset\e[0m"
	git reset --hard HEAD
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git reset pour $PLUGIN_NAME ! Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
	fi
	git pull --all
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git pull pour $PLUGIN_NAME ! Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
	fi
fi
if [ -n "$BRANCH_NAME" ]; then
	commit_id=`git rev-parse -q --verify $BRANCH_NAME`
	if [ -n "$commit_id" ]; then
		git checkout $commit_id --force
	else
		echo -e "\e[91mLa branch ou commit id $BRANCH_NAME n'existe pas pour $PLUGIN_NAME ! Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
	fi
fi
if [ -n "$DATE" ] && [ "$DATE" != "" ]; then
	git checkout 'master@{$DATE}' --force
elif [ -z "$BRANCH_NAME" ]; then
	echo -e "\e[32mIl faut ajouter une branch en argument 1. Souvent dev ou master, marche aussi avec un commit.\e[0m"
	exit 0
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
gradle publishToMavenLocal
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mLe build du $PLUGIN_NAME a échoué ! Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
else
	echo `git rev-parse HEAD` > target/commitId
	echo `cd ../olympaapi && git rev-parse HEAD` > target/commitIdAPI
fi
echo -e "\e[32mLe jar du commit de l'api $(cat target/commitIdAPI) avec le $PLUGIN_NAME $(cat target/commitId) a été crée.\e[0m"
