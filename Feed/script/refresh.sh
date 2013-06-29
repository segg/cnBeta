#!/bin/bash

for i in `seq 242800 242852`
do
  curl http://gg--uu.appspot.com/feed?id=$i
done
