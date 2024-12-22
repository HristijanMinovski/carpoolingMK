Carpooling app
This is my carpooling app that is an Android application developed in Android Studio with java programming language. The main purpose of this app is to have users login as either passenger or driver and with that 
divide them into users that offer or search for a ride. This project is useful because it helps people find themselves a ride, helps the users save money and also helps the environment.

What is included in this project?
1)	User authentication where if users don’t have an account in the database, they need to register first. After the splash screen(that lasts 3 seconds), they can either login or register.
    After the login and registration they have two different user interfaces depending on which type of user they are.
2)	Client interface has a screen with a map where they choose two dots on the map (their starting and destination location on the map). Below the map they have two buttons, one to rate a previous driver
    and second one to search for an available driver. If they search for a driver they get a new screen where as a recycler view they get all the available rides(they are filtered with
  	the ratings of the drivers in a descending order and also the starting and destination location) that shows the drivers name, rating, start, end and also price. If they choose one driver they are sent
  	to a screen that confirms their seat on that route.
3)	Driver interface has a screen with all of their information and in the toolbar a place where they can insert/update their vehicle. And on the bottom of this screen they also have two buttons one that
	  they can rate a previous passenger and one to add a route. If they choose to add a route they have a screen that they input the start time, starting location, destination and price per client.
	  Than the route is shown on a map with a click on a button and with another click on a different botton below the map they add the route to the database.
4)	This project also includes a rating system that follows how the drivers rate the clients and vise versa. After every new rating it is updated.

For running this project you will need to have 
- Android Studio, 
- Java Development Kit (JDK): JDK 7 or above is recommended,
- Gradle is used for building, android emulator is recommended
- a medium phone with API 35(Android 15).

