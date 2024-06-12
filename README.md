# Professional Subscriptions Frontend 

## Info

This service is also known as *Claim for your work related professional subscriptions*

This service allows an individual to update their tax accounts flat rate expense,
specifically that of their 57 IABD code relating to professional subscriptions.

This service does not have its own backend for updating NPS, instead it uses TAI for this integration.

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

### Running the service

##### `run.sh`

* Starts the Play! server on [localhost:9335](http://localhost:9335) with test routes enabled.

* Service Manager: EE_ALL 

* Port: 9335

* NINOs: `LL111111A` & `AB216913B` (local and Staging environments only)

* Confidence Level: 200

### Start dependencies via Service Manager

To start all dependencies and services for professional subscriptions, use one of the following commands:
```
sm2 --start EE_ALL
```

### Accessing the service

* Redirect URL: [http://localhost:9335/professional-subscriptions](http://localhost:9335/professional-subscriptions)


## Tests and prototype

[View the prototype here](https://employee-expenses.herokuapp.com/)

|Repositories     |Link                                                                   |
|-----------------|-----------------------------------------------------------------------|
|Journey tests    |https://github.com/hmrc/professional-subscriptions-journey-tests       |
|Prototype        |https://github.com/hmrc/employee-expenses-prototype                    |
