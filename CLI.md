# Commands

## Login
login user -s \<*sin number*\> 

**Description:** \
Login to mybnb.

**Requirements:**
* An existing user must be created with *sin number*.

## Logout
logout user

**Description:** \
Logout of mybnb.

**Requirements:**
* User must be first logged in.

## Create User
create user -s \<*sin number*\> -n \<*full name*\> -a \<*address*\> -b \<*birthdate*\> -o \<*occupation*\>

**Description:** \
Create a user.

**Requirements:**
* *sin number* must not belong to an existing user.
* *full name* must be separated with "_" character instead of space.
* *address* must be separated with "_" character instead of space.
* *birthdate* must be in the format of YYYY-MM-DD.
* *occupation* must be separated with "_" character instead of space.

## Create Listing
create listing -t \<listing type\> -a \<address\> -pc \<postal code\> -lo \<longitude\> -la \<latitude\> -ci \<city\> -co \<country\> \

**Description:** \
Create a listing. \
Users will be given a prompt to add amenities to their listings.

**Requirements:**
* User must be logged in.
* A listing with *listing id* must exist.
* *address* must be separated with "_" character instead of space.
* *longitude* must be a valid decimal value.
* *latitude* must be a valid decimal value.

## Delete Listing
delete listing -l \<listing id\>

**Description:** \
Delete user's listing.

**Requirements:**
* User must be logged in.
* User must be the host of the listing.
* A listing with *listing id* must exist.
* Future bookings for the listing must not exist. 

## Add Amenity
add amenity -l \<listing id\>

**Description:** \
Add an amenity to user's listing.

**Requirements:**
* User must be logged in.
* User must be the host of the listing.
* A listing with *listing id* must exist.

## Delete Amenity
delete amenity -l \<listing id\> -a \<amenity\>

**Description:** \
Delete exist amenity in user's listing.

**Requirements:**
* User must be logged in. 
* User must be the host of the listing. 
* A listing with *listing id* must exist.
* *amenity* must exist in listing.

## Add Availability
add availability -l \<listing id\> -sd \<start date\> -ed \<end date\> -ppn \<price per night\>

**Description:** \
Add an availability period to user's listing. \
When *ppn* is unsure, the user will recommend a listing price, based on the average listing price per night in their city. If there are no listings, the user will have to enter a price per night.

**Requirements:**
* User must be logged in.
* User must be the host of the listing.
* A listing with *listing id* must exist.
* *start date* must be in format of YYYY-MM-DD, and before end date.
* *end date* must be format of YYYY-MM-DD, and after end date.
* *ppn* can either be a float or "unsure".

## Remove Availability
remove availability -l \<listing id\> -sd \<start date\> -ed \<end date\>

**Description:** \
Remove an availability period to user's listing.

**Requirements:**
* User must be logged in.
* User must be the host of the listing.
* An availability with *start date* and *end date* must already exist.
* A listing with *listing id* must exist.
* *start date* must be in format of YYYY-MM-DD.
* *end date* must be format of YYYY-MM-DD.

## Create Booking 
create booking -l \<listing id\> -sd \<start date\> -ed \<end date\> -pm \<payment method\> -cn \<card number\>

**Description:** \
Create a booking.

**Requirements:**
* User must be logged in.
* User must not be the host of the listing.
* An availability with *start date* and *end date* must already exist.
* A listing with *listing id* must exist.
* *start date* must be in format of YYYY-MM-DD.
* *end date* must be format of YYYY-MM-DD.
* *card number* must be a valid long.

## Cancel Booking
cancel booking -b \<booking id\>

**Description:** \
Cancel booking as tenant or host.

**Requirements:**
* User must be logged in.
* User must be either host or tenant.
* A booking with *booking id* must exist.

## Add Review
add review -b \<booking id\>

**Description:** \
Add review to a related booking.
A prompt will appear asking the user to create a review.r

**Requirements:**
* User must be logged in.
* User must be either host or tenant.
* A booking with *booking id* must exist.

## Delete Review
delete review -b \<booking id\>

**Description:** \
Add review to a related booking.
A prompt will appear asking the user to create a review.r

**Requirements:**
* User must be logged in.
* User must be either host or tenant.
* A booking with *booking id* must exist.

## Show User's Listings
show mylistings

**Description:** \
Show all user's listings.

**Requirements:**
* User must be logged in.

## Show User's Bookings
show mybookings

**Description:** \
Show all user's bookings.

**Requirements:**
* User must be logged in.

## Show Listing's Availabilities
show availabilities -l \<listing id\>

**Description:** \
Show all availabilities for listing.

**Requirements:**
* User must be logged in.
* A listing with *listing id* must exist.

## Show Listing's Amenities
show amenities -l \<listing id\>

**Description:** \
Show all amenities for listing.

**Requirements:**
* User must be logged in.
* A listing with *listing id* must exist.

## Show User's Reviews
show myreviews

**Description:** \
Show all reviews as host and as tenant.

**Requirements:**
* User must be loged in

## Show Listings based on filter

## Quit
quit

**Description:** \
Quit console.
