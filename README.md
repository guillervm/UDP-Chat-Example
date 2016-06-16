# UDP-Chat-Example
UDP Chat Example implemented in 2013.

## Project definition
The purpose of this project is developing a distributed system which offers a basic chat functionality. This functionality is described below:
* A user can connect to a chat server with a username (which can't be in use before).
* Once connected to the server, the user can ask for the list of users connected to this server.
* After asking for the connected users list, the user can send a chat session start to another user on the same server.
  * If the other user accepts it, both users can start chatting until one of them leaves the chat.
  * If the other user doesn't accept it, the chat session will not start.
* A user can finish a chat session at any time (in an ordered way). In this case, the other user has to be notified in order to finish the session as well.
* A user can leave the server at any time (in an ordered way). At this point, the chat session is finished (if it was active).

From the architectural point of view, the features of the distributed system are:
* The new system must implement a client-server architecture.
* The client represents the part of the system the final users interacts with.
* Any distributed communication will be performed through the server, not performing any direct communication between clients.

## Design
### Functional Description
The features of the chat are:
* Users are limited to 10.
* Messages will have a maximum length. The text entry won't allow the user to exceed this limit.
* Connected users list and chat requests will be updated every half second.
* Some nicknames will be forbidden, such as _false_, and some characters, such as "/", in order to prevent errors.
* Messages will be received every half second.
* Nicknames can't be repeated.

### Protocol
The protocol will use the following codes and message types:

Messages sent by the client | Server answer
--------------------------- | ---------------------------
`0`nick | _true, false, name_
`1`nick | _(no answer)_
`2`nick`/n`othernick | _true, error, chatting, refused_
`3`nick | _(no answer)_
`4`nick | user0`/n`user1`/n`...
`5`nick | _false_ (if no chats), _nick_ (if chats pending)
`6`message | _(no answer)_
`7`nick | _~false_ (if no messages), _message_ (if messages pending)

### Failure Model
Action which causes the failure | Failure | Possible solution
--------------------------- | --------------------------- | ---------------------------
Close the window without disconnecting | User permanently connected | Automatically disconnect when the window closes or not allowing to close the window if connected
Message longer than the buffer size | Incomplete message | Limit the number of characters of the text entry or trunk the message
A user tries to open a chat with another user who is already chatting | The user gets blocked | The server refuses the request automatically
The user list isn't updated | The user can't start chats with users connected after | Update the list eventually
A user tries to open a chat and the other user doesn't answer to the request | The user who tries to open the chat gets blocked | Set a timeout
A user connects with a username in use | Inconsistencies in the server | Check if the nick is already in use and don't allow the user to use it if it is already in use

