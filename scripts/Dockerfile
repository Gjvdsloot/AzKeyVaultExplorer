# scripts/Dockerfile.icons
FROM ubuntu:24.04

# Install tools
RUN apt-get update && \
    apt-get install -y imagemagick icnsutils && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app/scripts

# Copy the generation script and icon source
COPY generate-icons.sh .
COPY icon.svg .

# Default command — can be overridden
CMD ["bash", "generate-icons.sh"]