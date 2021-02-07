# ARA Paxos

This project uses `make` for every possible tasks.
The root Makefile loads variables from the `.env` file.
This is an easy way to override every varibles defined with `?=`.

## Simulations

### Build only

    make java

### Run a simulation using [`ppi`][ppi] with PeerSimRunner

    make run [NP=5] [PROC=ara.paxos.Paxos]

## PDF

### Install latex

    sudo apt install texlive latexmk texlive-lang-french texlive-latex-extra cm-super

### Render pdf

    make pdf


[ppi]: https://github.com/PolyProcessInterface/ppi
