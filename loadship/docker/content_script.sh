#!/bin/bash

ORGFILE=$1
NEWCONTENT=$2
SEARCH=$3

TOTALROWS=`wc -l $ORGFILE | sed -r 's/([0-9]*).*/\1/'`
ROWNUMBER=`grep -n "$SEARCH" $ORGFILE | sed -r 's/([0-9]*).*/\1/'`
echo $TOTALROWS $ROWNUMBER

FIRSTBOTTOM=`expr $ROWNUMBER - 1`
ENDTOP=`expr $TOTALROWS - $ROWNUMBER `
ENDTOP=`expr $ENDTOP + 1 `
head -n $FIRSTBOTTOM $ORGFILE > result.txt

cat $NEWCONTENT >> result.txt
tail -n $ENDTOP $ORGFILE >> result.txt

cp result.txt $ORGFILE
