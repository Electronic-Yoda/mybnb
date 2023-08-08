# MY-BNB
This repository contains a Java system that simulates the database and service layer of an online marketplace for homestays.
The system has a strong emphasis on testability, graceful error handling, and data integrity during concurrent access or operational failures, which is maintained through the use of transactions.

The system is developed according to the DAO (Data Access Object) pattern, which allows for the separation of the business logic and the data access logic.
Java Records are used to represent the data at the service layer, which allows for the immutability of the data and the ease of testing.
A builder design pattern and the dynamic generation of SQL queries in the DAO layer allows for flexible and extensible queries.

Key technologies and libraries used include Java, JUnit for testing, MySQL and JDBC for database operations, Docker for containerization,
Log4J for logging, MySQL spatial extensions for handling geographical data, Apache Commons CLI and JLine for the command line interface,
and Swing for the report UI.
Additionally, to generate emulated data for testing, the Google Maps API is used to retrieve the coordinates of addresses, and the Faker library is used to generate realistic data.

Two applications are included to demonstrate the functionality of the system.
The first is a ServiceCli, which allows users to create user accounts, listings, make bookings, and add reviews. A notable capability of this application is its ability to search for listings based on various filters, such as price, location, and amenities.
The second application is ManagementCli, which allows administrators to load test data, reset the database, and run reports. It also allows administrators to view reports on the system, such as the most popular listings and the most active users.

# Assumptions
* Users are unique based on their SIN number.
* Listings are unique based on the combination of their address, postal code, and city.
* Users must check-in by 11AM and check-out by 3PM thus the minimum booking range is at least two days.
* Users cannot book their own listings.
* Only tenants can review their booked listings and related hosts.
* Only hosts can review tenants.
* Reviews can only be created after the end date of the booking.
* Users cannot book listings with availabilities that ended before the current date.

# ServiceCli Commands
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
 -a,--address <arg>                               listing address
 -amen,--amenities <arg>                          amenities (A list of
                                                  amenities, each
                                                  separated by comma
 -ci,--city <arg>                                 listing city
 -co,--country <arg>                              listing country
 -ed,--end-date <arg>                             availability end date
 -edr,--end-date-range <arg>                      availability end date
                                                  range
 -h,--help                                        show help
 -l,--listing-id <arg>                            listing id
 -la,--latitude <arg>                             listing latitude
 -lo,--longitude <arg>                            listing longitude
 -pc,--postal-code <arg>                          listing postal code
 -ppn,--price-per-night <arg>                     price per night
 -ppnmax,--price-per-night-range-max <arg>        price per night range
                                                  max
 -ppnmin,--price-per-night-range-min <arg>        price per night range
                                                  min
 -ppnSortAsc,--price-per-night-sort-asc <arg>     sort by price per night
                                                  ascending
 -ppnSortDesc,--price-per-night-sort-desc <arg>   sort by price per night
                                                  descending
 -rad,--search-radius <arg>                       Search radius. Defaults
                                                  to 20 if address is not
                                                  specified and is not set
 -s,--user-sin <arg>                              user sin
 -sd,--start-date <arg>                           availability start date
 -sdr,--start-date-range <arg>                    availability start date
                                                  range
 -t,--listing-types <arg>                         a list of listing types,
                                                  each separated by a
                                                  comma
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


# Running MySQL using docker.
Below is an example. Note the current program has not been tested for any MySQL version other than version 8.0.

### Pull image:
```
docker pull mysql:8.0
```

### Run with no password setup:
```
docker run --name mysql-bnb -d -p 3307:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=mydb -v mysql-bnb:/var/lib/mysql mysql:8.0
```
Note: The port can be changed to any port on the host machine. We used port 3307 on the host for our tests and demos

### Run with root password setup:
```
docker run --name mysql-bnb -d -p 3307:3306 -e MYSQL_ROOT_PASSWORD=change-me -e MYSQL_DATABASE=mydb -v mysql-bnb:/var/lib/mysql mysql:8.0
```
Note: The port can be changed to any port on the host machine. We used port 3307 on the host for our tests and demos

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

# Program Demo
First, start ManagementCli and load test data. Make sure the database is empty before loading test data.
```
reset database
```

Then, load test data.

```
load testData
````

Then, start ServiceCli and login as any user with SIN between 3 to 21, inclusive.
Below are some example commands to run.
### Creating users and listings
```agsl
create user -s 1 -n Donald Doe -a 32_Main_St._Toronto,_ON -b 2001-03-12 -o Student

// let user with id 1 create two listings
login user -s 1
create listing -t house -a 123_Main_St. -pc M5S_1A1 -lo 43.66 -la 79.40 -ci Toronto -co Canada
    // then add tv, hot tub, and wifi to the first listing
create listing -t apartment -a 111_Main_St. -pc M5T_1C1 -lo 42.11 -la 79.40 -ci Toronto -co Canada
    // then add wifi only
    
show mylistings

// You can then add availabilities to the listings. You will need the listing id to do so.
```

### Search Listings
Try to run the following commands in the order they are listed below. Note that the commands are case sensitive.
```agsl
// Operations 3: login to user 6
login user -s 6

show mylistings

show mybookings

// Search by location (there is a 20 km default radius)
show listings -lo -79.39 -la 43.66

// Search by location (with radius)
show listings -lo -79.39 -la 43.66 -rad 10

// Search by address (exact and with radius) and with a few other filters
show listings -a 100_Queens_Park
show listings -a 100_Queens_Park -rad 10
    // Note: the returned listings are sorted by ascending distance from the given address
show listings -a 100_Queens_Park -rad 10 -ppnSortAsc true
    // Note: the returned listings are sorted by ascending price per night
show listings -a 100_Queens_Park -rad 10 -ppnSortDesc true
    // Note: the returned listings are sorted by descending price per night
show listings -a 100_Queens_Park -rad 100

// Search by postal code (exact and with radius)
show listings -pc 10011
show listings -pc 10011 -rad 50

// Search by listing type and with a few other filters
show listings -pc 10011 -rad 50 -t guesthouse
show listings -pc 10011 -rad 50 -t guesthouse,apartment
show listings -pc 10011 -rad 50 -t guesthouse,apartment -ppnSortAsc true
show listings -pc 10011 -rad 50 -t guesthouse,apartment -ppnSortDesc true

// Search by listing type and amenities plus a few other filters
show listings -pc 10011 -rad 50 -t guesthouse,apartment -amen wifi
show listings -amen tv,dryer
show listings -t hotel -amen tv,dryer

// Search by price per night
show listings -ppnmax 100
show listings -ppnmin 100 -ppnmax 110
show listings -ppn <input your exact value>
```
