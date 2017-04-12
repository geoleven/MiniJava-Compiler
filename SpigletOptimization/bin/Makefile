all: compile

compile:
	java -jar jtb132di.jar spiglet.jj
	javacc spiglet-jtb.jj
	javac -cp lib/iris-0.60.jar:lib/iris-parser-0.60.jar:./ Main.java
	java -cp lib/iris-0.60.jar:lib/iris-parser-0.60.jar:./ Main ./spigletFiles/Factorial.spg
	#java -cp lib/iris-0.60.jar:lib/iris-parser-0.60.jar:src/ iris.Main analyses/

clean:
	rm -f *.class *~
