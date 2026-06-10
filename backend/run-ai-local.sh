#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

if [[ -f "${ROOT_DIR}/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "${ROOT_DIR}/.env"
  set +a
fi

export AI_PROVIDER=http
export AI_API_BASE_URL="${AI_API_BASE_URL:-https://open.bigmodel.cn/api/paas/v4/chat/completions}"
export AI_API_KEY="${AI_API_KEY:?请先在仓库根目录 .env 或当前终端中设置 AI_API_KEY}"
export AI_MODEL="${AI_MODEL:-glm-4.5-flash}"

cd "${SCRIPT_DIR}"
mvn spring-boot:run
