#!/bin/bash
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
# ./deploy.sh master
# ./deploy.sh dev

# PARAMETRES
SERVEUR_DIR="/home/serveurs"

BASEDIR=$(dirname "$0")
ACTUAL_COMMIT_ID=`cat $BASEDIR/target/commitId`
ACTUAL_COMMIT_ID_API=`cat $BASEDIR/target/commitIdAPI`

# DÉPENDANCES

cd /home/repo/olympa-api/ && bash ./deploy.sh master justGitPull
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mErreur > Arrêt du maven build du $PLUGIN_NAME\e[0m"; exit 1
fi

cd $BASEDIR

if [ -n "$1" ] && [ -n "$2" ]; then
	PLUGIN_NAME="$1"
	BRANCH_NAME="$2"
	if [ ! -d "$SERVEUR_DIR/*$PLUGIN" ]; then
		echo -e "\e[91m$SERVEUR_DIR/*$PLUGIN n'existe pas."
		exit 1
	fi
else
	echo -e "\e[91mTu dois choisir la version du $PLUGIN_NAME en ajoutant une branch (ex './deploy.sh core $master')"
	echo -e "un commit (ex './deploy.sh dev $(git rev-parse HEAD)').\e[0m"
	exit 1
fi
git pull --all
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mEchec du git pull pour $PLUGIN_NAME, tentative de git reset\e[0m"
	git reset --hard HEAD
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git reset  pour $PLUGIN_NAME, tentative de git checkout\e[0m"
		git checkout $BRANCH_NAME --force
		if [ "$?" -ne 0 ]; then
			echo -e "\e[91mEchec du git checkout pour $PLUGIN_NAME. Dernier build avec succès : $ACTUAL_COMMIT_ID[0m"; exit 1
		fi
	fi
	git pull --all
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git pull pour $PLUGIN_NAME. Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
	fi
else
	git checkout $BRANCH_NAME --force
	if [ "$?" -ne 0 ]; then
		echo -e "\e[91mEchec du git checkout pour $PLUGIN_NAME. Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
	fi
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
if [[ ${@:1} == *justGitPull ]]; then
	echo -e "\e[0;36mLe $PLUGIN_NAME n'a pas été build comme demandé, il a juste été git pull.\e[0m"; exit 0
else
	gradle publishToMavenLocal
fi
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mLe build du $PLUGIN_NAME a échoué ! Dernier build avec succès : $ACTUAL_COMMIT_ID\e[0m"; exit 1
else
	echo `git rev-parse HEAD` > target/commitId
	echo `cd ../olympaapi && git rev-parse HEAD` > target/commitIdAPI
fi
echo -e "\e[32mLe jar du commit de l'api $(cat target/commitIdAPI) avec le $PLUGIN_NAME $(cat target/commitId) a été crée.\e[0m"
