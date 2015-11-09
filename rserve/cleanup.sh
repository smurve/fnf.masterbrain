#!/bin/bash
echo "removing all existing zlotcar containers. The zlotcar image won't be deleted."
docker ps --all | grep zlotcar | cut -c1-13 | xargs echo -n | xargs docker rm
