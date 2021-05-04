function pause(){
 read -p ""
}

echo mkdir repo
mkdir repo
pause
echo ./simplegit.sh init repo
./simplegit.sh init repo
echo create first file
echo echo 1 >> repo/1.txt
echo 1 >> repo/1.txt
pause
echo staging, but fails because working dir is not under repo
echo ./simplegit.sh add repo/1.txt
./simplegit.sh add repo/1.txt
# changing working dir
echo cd repo
cd repo
pause
echo staging
echo ../simplegit.sh add 1.txt
../simplegit.sh add 1.txt
pause
echo have staged for commit changes
echo ../simplegit.sh status
../simplegit.sh status
pause
# creating inner dir with file
mkdir inner
echo 2 >> inner/2.txt
# staging and looking for status, we have 2 staged changes
../simplegit.sh add inner/2.txt
../simplegit.sh status
# now show that we can change to nested dir and stage file
echo 3 >> inner/3.txt
cd inner
../../simplegit.sh add 3.txt
# and remove it and return to previous dir
../../simplegit.sh


