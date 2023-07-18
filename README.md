# mybnb-database

Running mysql using docker:
```
docker pull mysql:8.0
```

Run with no password setup:
```
docker run --name mysql-bnb -d -p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=mydb -v mysql-bnb:/var/lib/mysql mysql:8.0
```

Run with root password setup:
```
docker run --name mysql-bnb -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=change-me -e MYSQL_DATABASE=mydb -v mysql-bnb:/var/lib/mysql mysql:8.0
```

Open mysql shell inside the container:
```
docker exec -it mysql mysql -p
```

Remove Container:
```
docker stop mysql-bnb
```
```
docker rm mysql-bnb
```
docker volume rm mysql-test
```
