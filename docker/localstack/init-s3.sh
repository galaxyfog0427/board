#!/bin/bash
set -e

if awslocal s3api head-bucket --bucket board-files 2>/dev/null; then
  echo "board-files 버킷이 이미 있습니다"
else
  awslocal s3 mb s3://board-files
fi