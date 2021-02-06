include .env

JAVAC ?= javac
JAVA  ?= java
SRC   ?= src
BIN   ?= bin
LIB   ?= lib
PDF   ?= pdf
PPI   ?= $(LIB)/ppi.jar

SRCS = $(shell find $(SRC) -name '*.java')
BINS = $(SRCS:$(SRC)/%.java=$(BIN)/%.class)

CONFIG ?= ppi.json
PROC   ?= ara.paxos.Paxos
NP     ?= 5
RUNNER ?= peersim.PeerSimRunner

JAVAC_FLAGS += -cp $(PPI):$(SRC) -d $(BIN)
JAVA_FLAGS  += -cp $(BIN):$(PPI)
PPI_FLAGS   += -c $(CONFIG) --np $(NP)


all: pdf java

pdf:
	$(MAKE) -C $(PDF) $@

java: $(BINS)

run: java
	$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi $(PPI_FLAGS) $(PROC) org.sar.ppi.$(RUNNER)

clean: $(SUBDIRS)
	rm -rf $(EXE)
	rm -rf $(BIN)
	rm -rf *.log
	$(MAKE) -C $(PDF) $@

.PHONY: all pdf java run clean

$(BIN)/%.class: $(SRC)/%.java | $(BIN)
	$(JAVAC) $(JAVAC_FLAGS) $<

$(BIN):
	mkdir bin

%:
	touch $@
