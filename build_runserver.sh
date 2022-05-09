# run with ./run <port number>
rm bin/*
javac -d bin src/*.java
echo "running server on port " + $1
java -cp ./bin/ Server_app $1
