FILES = rapport
TEX = $(FILES:%=%.tex)
PDF = $(FILES:%=%.pdf)

pdf: $(PDF)

%.pdf: %.tex
	latexmk -pdf -f $<

cleantmp:
	latexmk -c

clean:
	latexmk -C

.PHONY: pdf cleantmp clean
