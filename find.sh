#!/bin/sh

while /bin/true; do

  git checkout HEAD^1 || exit 1
  ./statistics.groovy --no-meta-update -c 2> /dev/null | rg "Всього слів: [0-9]{7}," && exit 0

done
