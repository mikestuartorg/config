#!/bin/bash
 
sleep 2
 
ls $HOME/.java-buildpack/open_jdk_jre/lib/security
cp $HOME/jce/* $HOME/.java-buildpack/open_jdk_jre/lib/security
ls $HOME/.java-buildpack/open_jdk_jre/lib/security

