# ARA Paxos

This project uses `make` for every possible tasks.
The root Makefile loads variables from the `.env` file.
This is an easy way to override every varibles defined with `?=`.

## Build all

    make

## Run a simulation using [`ppi`][ppi] with PeerSimRunner

    make run [NP=5] [PROC=ara.paxos.Paxos]


[ppi]: https://github.com/PolyProcessInterface/ppi
