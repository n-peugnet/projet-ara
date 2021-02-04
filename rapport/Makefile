FILES = rapport
TEX = $(FILES:%=%.tex)
PDF = $(FILES:%=%.pdf)

all: $(PDF)

%.pdf: %.tex
	latexmk -pdf -f $<

cleantmp:
	latexmk -c

clean:
	latexmk -C

.PHONY: all cleantmp clean
