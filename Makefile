JC = javac
# JC = javac -Xlint:unchecked
OUTPUT = out
MAIN = Main
CP_SEP = :
# CP_SEP = ;
LIBS = ./libraries/jcommon-1.0.23.jar$(CP_SEP)./libraries/jfreechart-1.0.19.jar$(CP_SEP)./libraries/jSerialComm-2.11.0.jar

CLASSES = \
./src/*.java \
./src/frame/*.java

all:
	make build
	make run

build:
	$(JC) -d $(OUTPUT) -cp .$(CP_SEP)$(LIBS) $(CLASSES)

run:
	java -cp $(OUTPUT)$(CP_SEP)$(LIBS) $(MAIN)

clean:
	$(RM) $(OUTPUT)/*.class
