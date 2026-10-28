#!/usr/bin/env bash
set -e

if [ ! -f "bin/com/bank/Main.class" ]; then
    echo "[INFO] Binaries missing. Compiling first..."
    chmod +x compile.sh
    ./compile.sh
fi

java -cp bin com.bank.Main
