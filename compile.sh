#!/usr/bin/env bash
set -e

echo "=================================================================="
echo "   Building Apex Secure Banking System (25CE076) [Bash/Unix]     "
echo "=================================================================="

mkdir -p bin

find src -name "*.java" > sources.txt
echo "[INFO] Compiling $(wc -l < sources.txt) Java source files..."
javac -d bin -encoding UTF-8 @sources.txt
rm -f sources.txt

echo "[SUCCESS] Build succeeded! Classes compiled into bin/"
echo "Run using: ./run.sh"
