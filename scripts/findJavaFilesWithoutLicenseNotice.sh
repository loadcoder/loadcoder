SearchTerm="You should have received a copy of the GNU General Public License"
for i in $( find . -name "*.java" ); do 
if grep -q "$SearchTerm" $i; then
A=b
else
   echo $i
fi
done

