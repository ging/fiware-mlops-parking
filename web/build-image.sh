docker build . --tag tonihurtado/$1:$2
docker push tonihurtado/$1:$2
docker tag tonihurtado/$1:$2 tonihurtado/$1:latest
docker push tonihurtado/$1:latest