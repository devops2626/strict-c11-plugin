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

    # Check for unsafe strcpy() instead of strncpy/memcpy_s
    if grep -n '\bstrcpy\s*(' "$file" > /dev/null; then
        echo "  [!] $file: Unsafe string copy - use safer alternatives."
        grep -n '\bstrcpy\s*(' "$file"
        ERRORS=$((ERRORS + 1))
    fi

    # Check for unvalidated malloc (naked malloc without NULL check handling or pattern)
    if grep -n '\bmalloc\s*(' "$file" > /dev/null; then
        # Simple heuristic check for direct assignments without immediate null checking
        if grep -n '=\s*malloc\s*(' "$file" > /dev/null; then
            echo "  [i] $file: Notice - ensure malloc return values are explicitly validated against NULL."
        fi
    fi
done

exit $ERRORS
