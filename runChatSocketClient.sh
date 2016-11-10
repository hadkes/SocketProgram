mvn compile
mvn compile -DskipTests
mvn install -DskipTests
mvn exec:java -Dexec.mainClass="edu.tcd.scss.nds.chatroom.socket.client.SocketClient" -Dexec.args="localhost 8888"

