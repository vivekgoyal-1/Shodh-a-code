FROM ubuntu:22.04
RUN apt-get update && apt-get install -y openjdk-17-jdk python3 python3-pip g++ gcc && rm -rf /var/lib/apt/lists/*
WORKDIR /code
CMD ["tail", "-f", "/dev/null"]
