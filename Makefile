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

PEERSIM_PROPERTIES ?= ex2.properties
CONFIG ?= '{"infra": {"peersim": {"properties": "$(PEERSIM_PROPERTIES)"}}}'
PROC   ?= ara.paxos.Paxos
NP     ?= 5
RUNNER ?= peersim.PeerSimRunner

EX1NODES_VALUES ?= 4 5 6 8 10 15
EX1BACKOFF_VALUES ?= 500 1000 5000
EX1BACKOFFCOEF_VALUES ?= 0 1 10
EX1TIMEOUT_VALUES ?= 500 1000 5000
EX1RETRY_VALUES ?= 20 50 100

LOG_REDIRECTIONS = >> $*.log 2>> $*.err.log

JAVAC_FLAGS += -cp $(SRC):$(PPI):$(JAVAP) -d $(BIN)
JAVA_FLAGS  += -cp $(BIN):$(PPI):$(JAVAP)
PPI_FLAGS   += -j $(CONFIG) --np $(NP)
PAXOS_ARGS  ?= true 1000 1000 100 1

PLOTS   = ex1nodes ex1backoff
IMGS    = $(PLOTS:%=%.png)
DATAS   = $(PLOTS:%=%.dat)
ERR_LOG = $(PLOTS:%=%.err.log)
LOG     = $(PLOTS:%=%.log)

all: pdf java

pdf: plots
	$(MAKE) -C $(PDF) $@

java: $(BINS)

plots: $(IMGS) ;

run: java
	$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi $(PPI_FLAGS) $(PROC) org.sar.ppi.$(RUNNER) \
		$(PAXOS_ARGS)

clean: $(SUBDIRS) cleandata
	rm -rf $(BIN)
	rm -rf $(IMGS)
	$(MAKE) -C $(PDF) $@

cleandata:
	rm -rf $(ERR_LOG) $(LOG)
	rm -rf $(DATAS)

.PHONY: all pdf java plots run clean cleandata

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

ex1nodes.dat: %.dat: | $(BINS)
	rm -rf $*.log $*.err.log
	for idAsRound in true false; do \
		for i in $(EX1NODES_VALUES); do \
			$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi -j $(CONFIG) --np $$i $(PROC) org.sar.ppi.$(RUNNER) \
				$$idAsRound 1000 1000 100 1 \
				$(LOG_REDIRECTIONS); \
		done; \
	done;

ex1backoff.dat: %.dat: | $(BINS)
	rm -rf $*.log $*.err.log
	for idAsRound in true false; do \
		for i in $(EX1TIMEOUT_VALUES); do \
			for j in $(EX1BACKOFF_VALUES); do \
				for k in $(EX1RETRY_VALUES); do \
					for l in $(EX1BACKOFFCOEF_VALUES); do \
						$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi -j $(CONFIG) --np $(NP) $(PROC) org.sar.ppi.$(RUNNER) \
							$$idAsRound $$i $$j $$k $$l \
							$(LOG_REDIRECTIONS); \
					done; \
				done; \
			done; \
		done; \
	done;

ex2.dat: %.dat: | $(BINS)
	rm -rf $*.log $*.err.log
	for idAsRound in true false; do \
		for i in $(EX1NODES_VALUES); do \
			$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi -j $(CONFIG) --np $$i $(PROC) org.sar.ppi.$(RUNNER) \
				$$idAsRound 1000 1000 100 1 \
				$(LOG_REDIRECTIONS); \
		done; \
	done;

$(BIN)/%.class: $(SRC)/%.java | $(BIN)
	$(JAVAC) $(JAVAC_FLAGS) $<

$(BIN):
	mkdir bin

.env:
	touch $@
