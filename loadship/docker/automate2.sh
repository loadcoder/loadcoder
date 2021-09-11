
sudo docker container stop loadship
sudo docker container rm loadship

sudo docker image rm loadship:1.0.0

#cp ../target/loadship-1.0.0-SNAPSHOT.jar loadship.jar

cd ..

sudo docker build -f docker/Dockerfile -t loadcoder:1.0.0 .

