# MY-BNB
This repository contains a Java system that simulates the database and service layer of an online marketplace for homestays.
The project has a strong emphasis on testability, as demonstrated by JUnit test suites that ensure the correctness and reliability of the system.
It handles exceptions gracefully and ensures data integrity through the use of DAO (Data Access Object) patterns.
Transactions are used to ensure that the database is kept in a consistent state when a set of operations fail or when the Database Management System (DBMS) is accessed concurrently.

Key technologies and libraries used include Java, JUnit for testing, MySQL and JDBC for database operations, Docker for containerization,
Log4J for logging, and the MySQL spatial extensions for handling geographical data.
Additionally, to generate emulated data for testing, the Google Maps API is used to retrieve the coordinates of addresses, and the Faker library is used to generate realistic data.

Two applications are included to demonstrate the functionality of the system.
The first is a ServiceCLI, which allows users to create listings, make bookings, and add reviews. A notable capability of this application is that it
allows users to search for listings based on various filters, such as price, location, and amenities, accomplished through a builder design pattern and the dynamic generation of SQL queries in the DAO.
The second application is ManagementCLI, which allows administrators to load test data, reset the database, and run reports. It also allows administrators to view reports on the system, such as the most popular listings and the most active users.

# ServiceCLI Commands

## Login

`login user -s <sin number>`

**Description:**

Login to mybnb.

**Requirements:**
* An existing user must be created with *sin number*.

## Logout

`logout user`

**Description:**

Logout of mybnb.

**Requirements:**
* User must be first logged in.

## Create User

`create user -s <sin number> -n <full name> -a <address> -b <birthdate> -o <occupation>`

**Description:**

Create a user.

**Requirements:**
* *sin number* must not belong to an existing user.
* *full name* must be separated with "_" character instead of space.
* *address* must be separated with "_" character instead of space.
* *birthdate* must be in the format of YYYY-MM-DD.
* *occupation* must be separated with "_" character instead of space.

## Create Listing

`create listing -t <listing type> -a <address> -pc <postal code> -lo <longitude> -la <latitude> -ci <city> -co <country>`

**Description:**

Create a listing.  
Users will be given a prompt to add amenities to their listings.

**Requirements:**
* User must be logged in.
* A listing with *listing id* must exist.
* *address* must be separated with "_" character instead of space.
* *longitude* must be a valid decimal value.
* *latitude* must be a valid decimal value.

## Delete Listing
`delete listing -l <listing id>`

**Description:**

Delete user's listing.

**Requirements:**
* User must be logged in.
* User must be the host of the listing.
* A listing with *listing id* must exist.
* Future bookings for the listing must not exist.

## Add Amenity
`add amenity -l <listing id>`

**Description:**

Add an amenity to user's listing.

**Requirements:**
* User must be logged in.
* User must be the host of the listing.
* A listing with *listing id* must exist.

## Delete Amenity
`delete amenity -l <listing id> -a <amenity>`

**Description:**

Delete existing amenity in user's listing.

**Requirements:**
* User must be logged in.
* User must be the host of the listing.
* A listing with *listing id* must exist.
* *amenity* must exist in listing.

## Add Availability
`add availability -l <listing id> -sd <start date> -ed <end date> -ppn <price per night>`

**Description:**

Add an availability period to user's listing.  
When *ppn* is unsure, the user will recommend a listing price, based on the average listing price per night in their city. If there are no listings, the user will have to enter a price per night.

**Requirements:**
* User must be logged in.
* User must be the host of the listing.
* A listing with *listing id* must exist.
* *start date* must be in format of YYYY-MM-DD, and before end date.
* *end date* must be in format of YYYY-MM-DD, and after end date.
* *ppn* can either be a float or "unsure".

## Remove Availability

`remove availability -l <listing id> -sd <start date> -ed <end date>`

**Description:**

Remove an availability period to user's listing.

**Requirements:**
* User must be logged in.
* User must be the host of the listing.
* An availability with *start date* and *end date* must already exist.
* A listing with *listing id* must exist.
* *start date* must be in format of YYYY-MM-DD.
* *end date* must be in format of YYYY-MM-DD.

## Create Booking

`create booking -l <listing id> -sd <start date> -ed <end date> -pm <payment method> -cn <card number>`

**Description:**

Create a booking.

**Requirements:**
* User must be logged in.
* User must not be the host of the listing.
* An availability with *start date* and *end date* must already exist.
* A listing with *listing id* must exist.
* *start date* must be in format of YYYY-MM-DD.
* *end date* must be in format of YYYY-MM-DD.
* *card number* must be a valid long.

## Cancel Booking

`cancel booking -b <booking id>`

**Description:**  
Cancel booking as tenant or host.

**Requirements:**
* User must be logged in.
* User must be either host or tenant.
* A booking with *booking id* must exist.

## Add Review

`add review -b <booking id>`

**Description:**

Add review to a related booking.  
A prompt will appear asking the user to create a review.

**Requirements:**
* User must be logged in.
* User must be either host or tenant.
* A booking with *booking id* must exist.

## Delete Review

`delete review -b <booking id>`

**Description:**

Delete review related to a booking.

**Requirements:**
* User must be logged in.
* User must be either host or tenant.
* A booking with *booking id* must exist.

## Show User's Listings

`show mylistings`

**Description:**

Show all listings that the user is the host of.

**Requirements:**
* User must be logged in.

## Show Listings by filtering search

`show listings <option> <arg>`

**Description:**  
Show all listings available based on filter options.

All optional options are shown below. When no options are specified, all listings will be shown.

```agsl
usage: show listings
 -a,--address <arg>                          listing address
 -amen,--amenities <arg>                     amenities (A list of
                                             amenities, each separated by
                                             comma
 -ci,--city <arg>                            listing city
 -co,--country <arg>                         listing country
 -ed,--end-date <arg>                        availability end date
 -edr,--end-date-range <arg>                 availability end date range
 -h,--help                                   show help
 -l,--listing-id <arg>                       listing id
 -la,--latitude <arg>                        listing latitude
 -lo,--longitude <arg>                       listing longitude
 -pc,--postal-code <arg>                     listing postal code
 -ppn,--price-per-night <arg>                price per night
 -ppnmax,--price-per-night-range-max <arg>   price per night range max
 -ppnmin,--price-per-night-range-min <arg>   price per night range min
 -rad,--search-radius <arg>                  Search radius. Defaults to 20
                                             if address is not specified
                                             and is not set
 -s,--user-sin <arg>                         user sin
 -sd,--start-date <arg>                      availability start date
 -sdr,--start-date-range <arg>               availability start date range
 -t,--listing-types <arg>                    a list of listing types, each
                                             separated by a comma
```

**Requirements:**
* User must be logged in.
* If an argument has spaces, each of the spaces must be replaced with the "_" character. Ex: "123 Main Street" -> "123_Main_Street".

## Show User's Bookings
show mybookings

**Description:**

Show all user's bookings.

**Requirements:**
* User must be logged in.
* If an argument has spaces, each of the spaces must be replaced with the "_" character. Ex: "123 Main Street" -> "123_Main_Street".

## Show User's Bookings
`show mybookings`

**Description:**

Show all user's bookings.

**Requirements:**
* User must be logged in.

## Show Listing's Availabilities
`show availabilities -l <listing id>`

**Description:**

Show all availabilities for listing.

**Requirements:**
* User must be logged in.
* A listing with *listing id* must exist.

## Show Listing's Amenities
`show amenities -l <listing id>`

**Description:**

Show all amenities for listing.

**Requirements:**
* User must be logged in.
* A listing with *listing id* must exist.

## Show User's Reviews
`show myreviews`

**Description:**

Show all reviews as host and as tenant.

**Requirements:**
* User must be logged in.

## Quit
`quit`

**Description:**

Quit console.

# ManagementCli Commands
## Load Test Data
`load testData`

**Description:**

Load test data into database.

Note: the users and listing data are all pre-defined, while all other data will be randomly generated.

**Requirements:**
* Database must be empty.

## Reset Database
`reset database`

**Description:**

Reset database. Note: this will delete all data in the database.

## Run Reports
`run reports`

**Description:**

Run all reports.

## Stop Running Reports
`stop reports`

**Description:**

Stop all running reports.

## Quit ManagementCli
`quit`

**Description:**

Quit console.


# Running mysql using docker.
Below is an example. Note the current program has not been tested for any mysql version other than version 8.0.

### Pull image:
```
docker pull mysql:8.0
```

### Run with no password setup:
```
docker run --name mysql-bnb -d -p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=mydb -v mysql-bnb:/var/lib/mysql mysql:8.0
```
Note: The port can be changed to any port on the host machine.

### Run with root password setup:
```
docker run --name mysql-bnb -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=change-me -e MYSQL_DATABASE=mydb -v mysql-bnb:/var/lib/mysql mysql:8.0
```
Note: The port can be changed to any port on the host machine.

### Open mysql shell inside the container:
```
docker exec -it mysql-bnb mysql -p
```

### Remove Container:
```
docker stop mysql-bnb
```
```
docker rm mysql-bnb
```
```
docker volume rm mysql-bnb
```
