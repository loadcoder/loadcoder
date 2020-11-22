SearchTerm=$1
for i in $( find . -name "*.java" ); do 
if grep -q "$SearchTerm" $i; then
   echo $i
else
   A=b
fi
done

