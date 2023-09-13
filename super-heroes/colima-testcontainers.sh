export DOCKER_HOST="$(docker context inspect colima -f '{{ .Endpoints.docker.Host }}')"
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
export TESTCONTAINERS_HOST_OVERRIDE=`colima ls -j | jq -r '.address'`
