#! /bin/bash
git log -m -1 --first-parent --name-only --pretty='format:' HEAD| sed 's:[^/]*$::' | uniq