#!/bin/sh

# Scripts for the QFJ binary distribution

scriptdir=`dirname $0`
qfjhome=$scriptdir/..

CP=$qfjhome/lib/mina-core-1.1.0.jar:$qfjhome/lib/slf4j-api-1.3.0.jar:$qfjhome/lib/slf4j-jdk14-1.3.0.jar:$qfjhome/quickfixj-all-1.3.3.jar:$qfjhome/quickfixj-core-1.3.3.jar:$qfjhome/quickfixj-msg-fix40-1.3.3.jar:$qfjhome/quickfixj-msg-fix41-1.3.3.jar:$qfjhome/quickfixj-msg-fix42-1.3.3.jar:$qfjhome/quickfixj-msg-fix43-1.3.3.jar:$qfjhome/quickfixj-msg-fix44-1.3.3.jar:$qfjhome/quickfixj-examples-1.3.3.jar

java -classpath "$CP" quickfix.examples.banzai.Banzai $*

