cd read-resources-in-jar
./test.sh
STATUS=$?

if [ $STATUS = 0 ]
then
	echo "SUCCESS"
else
	echo "FAIL"
fi

exit $STATUS

