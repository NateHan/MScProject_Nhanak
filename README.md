# MScProject_Nhanak
Track Wild Web Application - Submission for MSc Thesis Project for Birkbeck University of London

Video Walkthrough of completed software: https://youtu.be/nSoQk7V4hQk

Link to full report: https://goo.gl/pd7NXX 

# About

Animal tagging technology such as satellite, sensor, and other archival tags allows wildlife researchers to gather and collect a large variety of data. New advancements in technology allows the hardware for the tags to become smaller, more complex, and to store more data than ever before, facilitating new insight into the animal world. In order to capitalize on this wealth of information, researchers must effectively analyze and share their data among close colleagues and the broader scientific community. This report documents the creation of a full-stack web application, Track Wild, which was built to satisfy these requirements. The application provides scientists with an online collaborative tool to store and perform detailed analysis functions on their data, such as visualizing animal positional data on a satellite map. 

# Languages, Frameworks, and Libraries used

Scala 2.12.2
     Play! Supports both Scala and Java, but ultimately Scala was the programming language chosen to code all of the back-end operations. Scala’s hybrid object-oriented and functional programming design paradigm made it an attractive choice. The object-oriented side of the language allows the use of inheritance and increases the modularity of the programming, which provides the ability to scale or make easy changes to the application in the future. Additionally, Scala carries a powerful pattern-matching construct, which makes it easy to build methods which must parse objects or perform actions on inputs based on one or more conditions, rather than chaining together a series of if… else statements. 


JavaScript
    JavaScript was used to work with the front end of the application, often to handle many of the user’s interactions with the application. The JavaScript for this application is executed entirely in the client’s browser, loaded with the initial HTML view. This prevents the client from making a call back to the server for every single piece of functionality, which will free up server processing power and enable future scalability of the application. To improve dynamic response in DOM manipulation and for making get and post requests which did not cause the entire page to reload, two popular JavaScript libraries were used:

jQuery: This JavaScript library was used for its ability to efficiently traverse the DOM and to add responsiveness to user interactions with dynamically loaded pieces of a page. This was used frequently through the application to attach dynamic event listeners assigned to the submit button of a dynamically loaded form.

Ajax: This JavaScript library was used to boost the dynamic responsiveness of the application by handling almost all requests where it was desired to either post things like forms or make get requests to load smaller views into an already existing DOM without reloading the entire page. Utilizing the asynchronous .success()and .fail()callback methods meant the user could see the result of their requests without navigating away from their work, and could instantly try a request again in the case of an error.

SBT
    Scala Build Tool (SBT) was used to compile the Scala classes, execute tests, and to act as the trigger to start the local server and begin running the application. In the application tier, SBT handles some of the configuration between the other tiers. SBT is responsible for importing the application’s library dependencies, such as the version of Scala to use, the Java Database Connection (JDBC) drivers, the unit testing frameworks, and any other third-party libraries used for the application.
    
Play! Framework
    The full stack web development service Play! Framework was used to build most of the Track Wild application. It was chosen because it offers scalability and fast processing without necessarily needing a lot of computing power from the server. Its documentation claims to help reduce the need for server power, advertising a reduction of 80% when a social media management company, HootSuite, switched to using Play! to host their web application (Lightbend, 2016). This was important for this project, as most of it would be run on a single local machine and would eventually be hosted by a limited free service when deployed. It is built using Akka, an Actor system which utilizes the multi-core capabilities of most modern computer hardware. Its capabilities are enabled by its use of asynchronous and non-blocking code, working with Java or Scala’s concurrency libraries. Other Scala frameworks considered were Lift and Scalatra, but Play! was chosen because it had extensive documentation and offered the ability to begin creating the application without a lengthy setup process. 

PostgreSQL version 9.6
    PostgreSQL, an object relational database management system (ORDBMS), was used to create the persistent storage necessary for a web application. It was chosen for its strong multiversion concurrency controls, which are necessary for an application which will allow multiple simultaneous users to view and add information to the data tier through the application. It was also selected because it is often used with other similar GIS applications (Urbano et al. 2010). This is likely because it offers many of the familiar conventions as other common SQL databases such as MySQL, but also incorporates geometric data types such as Point (an X,Y coordinate) and Path (two consecutive points) which are often used when plotting points into a visualized GIS. Play! utilizes a JDBC connection with direct support for PostgreSQL databases, making the configuration between the application and the database very simple.

    Scala-CSV
Scala-CSV, a free third party library, was used to parse user-uploaded CSV files. It will take each line from the CSV file and return it as a data type which can be used in scala, such as a List[List[String]]. It was possible to build a simple parser using the default Scala IO, but this pre-built library offers a robust set of data parsing methods and is able to handle CSV files which may come in different formats. For example, it offers methods to convert CSV files which come with headers and those which do not. 

Google Maps JavaScript API v3
    To enable the GIS component of this application, the Google Maps Javascript API was integrated with the client tier of this application. Originally the Google Earth Engine was selected due to the recent update which added the default inclusion of decades of topographical data. Unfortunately, to integrate the engine as a module within another application required the use of Python and PHP, both languages in which the author is not versed. OpenLayers and ArcGIS were also considered, but both posed a higher barrier to entry as their documentation and initialization processes appeared to require a familiarity of working with GIS applications. Google Maps was chosen because its API provided extensive documentation which does not make this assumption, and there is a robust support community on Google and from sites such as StackOverflow.

Bootstrap 3.3.7
    Bootstrap was used to help provide styling and responsiveness within the HTML to the client tier of the application. Bootstrap, as defined on their site, “is an open source toolkit for developing with HTML, CSS, and JS” (Bootstrap, 2017). It utilizes an easy-to-use grid system of columns and rows which allows the different blocks of HTML to maintain their positioning and which will responsively adjust the on-screen elements as necessary depending on the size of the user’s browser window or device. It was chosen for its ease of integration and use, and for its vast support community. 

Font Awesome
    Font Awesome is a font toolkit which provides glyphicons that are recognized as text by the browser. This toolkit provides a way to make buttons and other interactive pieces of the GUI visually pleasing without using any JavaScript. They are easy to insert as HTML elements and since they are recognized as text, can be very quickly styled with CSS.

Google Fonts
    Google Fonts was used to provide a few additional custom fonts, lending a unique feel to the UX of the application.
 
# Running the program

-Clone the repo.
-Download and install PostgreSQL. Note - you may have to alter the build.sbt file and application.conf files to match your version and login information for your version of PostgreSQL. 
-Use SBT to import/build the project in your IDE of choice
-Use SBT to run the program
-Enter 'localhost:9000' in your browser. 
