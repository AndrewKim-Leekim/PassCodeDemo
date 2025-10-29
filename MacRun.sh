#!/usr/bin/env zsh
set -euo pipefail

# 스크립트 폴더로 이동
SCRIPT_DIR=${0:A:h}
cd "$SCRIPT_DIR"

# JDK 25 + JavaFX 번들을 우선 탐색 (ZuluFX → Liberica Full 순)
if [ -d "/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home" ]; then
  export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home"    # ZuluFX 25
elif [ -d "/Library/Java/JavaVirtualMachines/liberica-jdk-25-full.jdk/Contents/Home" ]; then
  export JAVA_HOME="/Library/Java/JavaVirtualMachines/liberica-jdk-25-full.jdk/Contents/Home"  # Liberica Full 25
else
  echo "JavaFX 포함 JDK 25가 필요합니다.
- Azul ZuluFX 25 설치:   brew install --cask zulufx
- Liberica JDK 25 Full:  brew tap bell-sw/liberica && brew install --cask liberica-jdk25-full"
  exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"

JAR="target/PassCodeDemo-1.0-SNAPSHOT.jar"
[[ -f "$JAR" ]] || { echo "JAR not found: $JAR"; exit 2; }

# JavaFX 포함 JDK면 별도 --module-path 옵션 불필요
java -jar "$JAR"

echo
read -r "?Press Enter to exit..."
