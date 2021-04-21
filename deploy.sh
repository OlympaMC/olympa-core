
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
# $1 = 1979-02-26 ou <branchName>
# $2 = 18:30:00

(cd /home/repo/olympaapi/ && sh ./deploy.sh $1)
if [ "$?" -ne 0 ]; then
	echo "\e[91mArrêt de la création des JAR. Erreur code $rc\e[0m"; exit $rc
fi

USE_BRANCH="master dev"

ACTUAL_COMMIT_ID=`cat target/commitId`
ACTUAL_COMMIT_ID_API=`cat target/commitIdAPI`


if [ -n "$1" ]; then
	if [ -n "$2" ] && [ "$2" = "date" ]; then
		DATE="$2"
	else
		BRANCH_NAME="$1"
		SERV="$2"
	fi
else
	echo "\e[0;36mTu peux choisir la version du core en ajoutant une date (ex './deploy.sh date \"2021-02-26 18:30:00\"') ou une branch (ex './deploy.sh dev').\e[0m"
fi

if [ -n "$BRANCH_NAME" ]; then
	exists=`git show-ref refs/heads/$BRANCH_NAME`
	if [ -n "$exists" ]; then
		git checkout $BRANCH_NAME --force
	else
		unset BRANCH_NAME
	fi
	#if [ `git branch --list $BRANCH_NAME` ]; then
	#elif [[ $BRANCH_NAME =~ ^[0-9A-Fa-f]{1,}$ ]] ; then
	#	git checkout $BRANCH_NAME --force
	#else
	#	unset BRANCH_NAME
	#fi
fi

if [ -n "$DATE" ] && [ "$DATE" != "" ]; then
	git checkout 'master@{$DATE}' --force
elif [ -z "$BRANCH_NAME" ]; then
	git reset --hard HEAD
	git checkout master --force
fi

if [ -n "$ACTUAL_COMMIT_ID" ] && [ -n "$ACTUAL_COMMIT_ID_API" ]; then
	git pull
	if [ "$ACTUAL_COMMIT_ID" = `git rev-parse HEAD` ]; then
		if [ "$ACTUAL_COMMIT_ID_API" = `cd ../olympaapi && git rev-parse HEAD` ]; then
			echo "\e[32mPas besoin de maven install le core, l'api est up & le jar est déjà crée.\e[0m"
			exit 0
		else
			echo "\e[32mIl faut build le core, l'api a été up.\e[0m"
		fi
	fi
fi
git pull && mvn install
if [ "$?" -ne 0 ]; then
	echo -e "\e[91mLe build du core a échoué ! Erreur $rc\e[0m"; exit $rc
else
	echo `git rev-parse HEAD` > target/commitId
	echo `cd ../olympaapi && git rev-parse HEAD` > target/commitIdAPI
fi
echo "\e[32mLe jar du commit de l'api $(cat target/commitIdAPI) avec le core $(cat target/commitId) a été crée.\e[0m"
exit 0
