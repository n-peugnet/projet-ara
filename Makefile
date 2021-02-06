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

JAVAC_FLAGS += -cp $(SRC):$(PPI):$(JAVAP) -d $(BIN)
JAVA_FLAGS  += -cp $(BIN):$(PPI):$(JAVAP)
PPI_FLAGS   += -j $(CONFIG) --np $(NP)

PLOTS = ex1nodes
IMGS  = $(PLOTS:%=%.png)
DATAS = $(PLOTS:%=%.dat)

all: pdf java

pdf:
	$(MAKE) -C $(PDF) $@

java: $(BINS)

plots: $(IMGS) ;

run: java
	$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi $(PPI_FLAGS) $(PROC) org.sar.ppi.$(RUNNER)

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


$(DATAS): PEERSIM_PROPERTIES = $*.properties
$(DATAS): %.dat: java
	rm -rf $*.log
	for i in $$(seq 4 10) 15; do \
		$(JAVA) $(JAVA_FLAGS) org.sar.ppi.Ppi -j $(CONFIG) --np $$i $(PROC) org.sar.ppi.$(RUNNER) >> $*.log 2> /dev/null; \
	done;

$(BIN)/%.class: $(SRC)/%.java | $(BIN)
	$(JAVAC) $(JAVAC_FLAGS) $<

$(BIN):
	mkdir bin

%:
	touch $@
