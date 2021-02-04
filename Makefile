include .env

JAVAC ?= javac
JAVA  ?= java
JAR   ?= jar
SRC   ?= src
BIN   ?= bin
LIB   ?= lib
PPI   ?= $(LIB)/ppi.jar

SRCS = $(shell find $(SRC) -name '*.java')
BINS = $(SRCS:$(SRC)/%.java=$(BIN)/%.class)
EXE  = paxos.jar


CONFIG ?= ppi.json
PROC   ?= ara.paxos.Paxos
NP     ?= 4

JAVAC_FLAGS += -cp $(PPI) -d $(BIN)
JAVA_FLAGS  += -cp $(BIN):$(PPI)
PPI_FLAGS   += -c $(CONFIG) --np $(NP)

SUBDIRS = rapport

all: $(SUBDIRS)

java: $(BINS)

run: java
	$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi $(PPI_FLAGS) $(PROC) org.sar.ppi.peersim.PeerSimRunner

clean: $(SUBDIRS)
	rm -rf $(EXE)
	rm -rf $(BIN)
	rm -rf *.log

.PHONY: all java run clean

$(BIN)/%.class: $(SRC)/%.java | $(BIN)
	$(JAVAC) $(JAVAC_FLAGS) $<

$(BIN):
	mkdir bin

%:
	touch $@

# Recurse `make` into each subdirectory
# Pass along targets specified at command-line (if any).
$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: $(SUBDIRS)
