#make
rm toBeCompiled/MoreThan4.spg
#rm bs-pretty.pg
java Main toBeCompiled/MoreThan4.java
#java -jar pretty-printer.jar bs.spg 
#echo -e "\n\n"
java -jar spp.jar < toBeCompiled/MoreThan4.spg
echo -e "\n\n"
java -jar pgiv2.jar < toBeCompiled/MoreThan4.spg
echo -e "\n\n"
