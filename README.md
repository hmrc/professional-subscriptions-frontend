# Professional Subscriptions Frontend 

## Info

This service is also known as *Claim for your work related professional subscriptions*

This service allows an individual to update their tax accounts flat rate expense,
specifically that of their 57 IABD code relating to professional subscriptions.

This service does not have it's own backend for updating NPS, instead it uses TAI for this integration.

### Dependencies

|Service        |Link                                   |
|---------------|---------------------------------------|
|Tai            |https://github.com/hmrc/tai            |
|Citizen Details|https://github.com/hmrc/citizen-details|

### Endpoints used

|Service        |HTTP Method |Route                                  |Purpose |
|---------------|--- |----------------|----------------------------------|
|Tai            |GET |/tai/${nino}/tax-account/${year} /expenses/flat-rate-expenses| Returns details of a users tax account specifically that of IABD 57 |
|Tai            |POST|/tai/${nino}/tax-account/${year} /expenses/flat-rate-expenses| Updates a users tax account specifically that of IABD 57  |
|Citizen Details|GET |/citizen-details/${nino}/etag|retrieves the users etag which is added to their update request to NPS to ensure optimistic locking|

## Running the service

Service Manager: PSUBS_ALL

Port: 9335

## Tests and prototype

[View the prototype here](https://employee-expenses.herokuapp.com/)

|Repositories     |Link                                                                   |
|-----------------|-----------------------------------------------------------------------|
|Journey tests    |https://github.com/hmrc/professional-subscriptions-journey-tests       |
|Prototype        |https://github.com/hmrc/employee-expenses-prototype                    |
