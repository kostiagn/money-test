#!/bin/bash

PORT=5434
if [[ ! $(docker ps -aq -f name=money-postgres) ]]
then
   docker run --name money-postgres -e POSTGRES_PASSWORD=Passwd123 -e POSTGRES_USER=money_user -e POSTGRES_DB=money -p ${PORT}:5432 -d postgres:10.5-alpine
else
  docker start money-postgres
fi

IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' money-postgres)
echo "Postgresql has started. To connect it use settings: "
echo "url = jdbc:postgresql://${IP}:${PORT}/money"
echo "username = money_user"
echo "password = Passwd123"
