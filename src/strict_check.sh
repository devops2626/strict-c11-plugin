#!/bin/sh
ERRORS=0

echo "[*] Scanning C files for Strict C11 compliance..."

for file in "$@"; do
    if [ ! -f "$file" ]; then
        continue
    fi
    
    # Check for gets()
    if grep -n '\bgets\s*(' "$file" > /dev/null; then
        echo "  [!] $file: Security risk - usage of 'gets()' is strictly forbidden."
        grep -n '\bgets\s*(' "$file"
        ERRORS=$((ERRORS + 1))
    fi
    
    # Check for register keyword
    if grep -n '\bregister\s\+' "$file" > /dev/null; then
        echo "  [!] $file: C11 deprecated/removed register storage class."
        grep -n '\bregister\s\+' "$file"
        ERRORS=$((ERRORS + 1))
    fi
done

exit $ERRORS
