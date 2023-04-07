# BearMaps

BearMaps is a web mapping application inspired by Google Maps and OpenStreetMap. This project involves building the "smart" pieces of a web-browser based Google Maps clone. In this project, we implemented and extended data structures and algorithms we learned in the course, such as a k-d tree, MinHeapPQ, and A* algorithm.

The project consists of six parts: Map Rastering, Data Structures, Routing, Autocomplete, Written Directions, and Above and Beyond. The first three parts are required, the fourth is extra credit, and the last two are optional.

#Getting Started
To run the application, you will need to have Java and Maven installed on your system. Once you have them installed, follow these steps:

1. Clone this repository to your local machine.
2. Open a terminal window and navigate to the project directory.
3. Type mvn clean compile to compile the project.
4. Type mvn exec:java -Dexec.mainClass="MapServer" to start the server.
5. Open a web browser and go to localhost:4567 to see the map.

#Map Rastering
Map Rastering involves generating images of a rectangular region of the world at a specified resolution that covers the region. This is done by parsing a URL that is provided by the user's web browser and generating an appropriate image.

#Data Structures

Data Structures involve implementing a k-d tree and extending the MinHeapPQ to be used in routing.

#Routing
Routing involves finding the shortest path between two points on the map. This is done by implementing the A* algorithm, which takes into account the distance between two points and the estimated distance to the destination.

#Autocomplete
Autocomplete involves finding all locations that match a given string. This is useful for finding, for example, all the Top Dogs in Berkeley.

#Written Directions
Written Directions involves augmenting the routing algorithm to include turn-by-turn directions.
