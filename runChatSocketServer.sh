mvn compile
mvn compile -DskipTests
mvn install -DskipTests
mvn exec:java -Dexec.mainClass="edu.tcd.scss.nds.chatroom.socket.server.SocketServer" -Dexec.args="8888"

