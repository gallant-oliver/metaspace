#!/usr/bin/env bash
rm -fr api/
apidoc -i apidoc/ -o api/
start api/index.html