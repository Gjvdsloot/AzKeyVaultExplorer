docker build -t icon-builder .
docker run --rm -v ${PWD}/../icons:/app/assets icon-builder