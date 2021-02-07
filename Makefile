include .env

SHELL = /bin/bash

JAVAC ?= javac
JAVA  ?= java
SRC   ?= src
BIN   ?= bin
LIB   ?= lib
PDF   ?= pdf
PPI   ?= $(LIB)/ppi.jar
JAVAP ?= $(LIB)/JavaPlot.jar

SRCS = $(shell find $(SRC) -name '*.java')
BINS = $(SRCS:$(SRC)/%.java=$(BIN)/%.class)

PEERSIM_PROPERTIES ?= ex1nodes.properties
CONFIG ?= '{"infra": {"peersim": {"properties": "$(PEERSIM_PROPERTIES)"}}}'
PROC   ?= ara.paxos.Paxos
NP     ?= 5
RUNNER ?= peersim.PeerSimRunner

EX1NODES_VALUES ?= 4 5 6 8 10 15
EX1BACKOFF_VALUES ?= 100 500 1000 5000
EX1TIMEOUT_VALUES ?= 100 500 1000 5000
EX1RETRY_VALUES ?= 5 10 50 100

JAVAC_FLAGS += -cp $(SRC):$(PPI):$(JAVAP) -d $(BIN)
JAVA_FLAGS  += -cp $(BIN):$(PPI):$(JAVAP)
PPI_FLAGS   += -j $(CONFIG) --np $(NP)
PAXOS_ARGS  ?= true 1000 1000 100

PLOTS = ex1nodes
IMGS  = $(PLOTS:%=%.png)
DATAS = $(PLOTS:%=%.dat)

all: pdf java

pdf:
	$(MAKE) -C $(PDF) $@

java: $(BINS)

plots: $(IMGS) ;

run: java
	$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi $(PPI_FLAGS) $(PROC) org.sar.ppi.$(RUNNER) \
		$(PAXOS_ARGS)

clean: $(SUBDIRS)
	rm -rf $(EXE)
	rm -rf $(BIN)
	rm -rf *.log
	rm -rf *.dat
	rm -rf *.png
	$(MAKE) -C $(PDF) $@

.PHONY: all pdf java plots run clean

#%.png: %.dat java
# while the plot is not finished
#	cp $< $@

# $* ce qu'à matché %
# $@ c'est la target, donc le nom du png
# $< c'est la première deps donc le nom du fichier de données
# Exemple :
# $* = ex1nodes
# $@ = ex1nodes.png
# $< = ex1nodes.dat
$(IMGS): %.png: %.dat java
	$(JAVA) $(JAVA_FLAGS) ara.graphi.SimpleGraphMaker $* $@ $<


%.dat: PEERSIM_PROPERTIES = $*.properties

.PRECIOUS: %.dat
ex1nodes.dat: %.dat: java
	rm -rf $*.log
	for idAsRound in true false; do \
		for i in $(EX1NODES_VALUES); do \
			$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi -j $(CONFIG) --np $$i $(PROC) org.sar.ppi.$(RUNNER) \
				$$idAsRound 1000 1000 100 \
				>> $*.log 2> /dev/null; \
		done; \
	done;

ex1backoff.dat: %.dat: java
	rm -rf $*.log
	for idAsRound in true false; do \
		for i in $(EX1TIMEOUT_VALUES); do \
			for j in $(EX1BACKOFF_VALUES); do \
				for k in $(EX1RETRY_VALUES); do \
					$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi -j $(CONFIG) --np $(NP) $(PROC) org.sar.ppi.$(RUNNER) \
						$$idAsRound $$i $$j $$k \
						>> $*.log 2> /dev/null; \
				done; \
			done; \
		done; \
	done;

$(BIN)/%.class: $(SRC)/%.java | $(BIN)
	$(JAVAC) $(JAVAC_FLAGS) $<

$(BIN):
	mkdir bin

.env:
	touch $@
