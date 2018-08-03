#!/usr/bin/env bash

export PERL5_DEBUG_ROLE=client
export PERL5_DEBUG_HOST=localhost
export PERL5_DEBUG_PORT=12345
perl -d:Camelcadedb src/main/java/ranbato/term/asciiquarium.pm