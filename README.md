# Federated Learning. The server side bit

This project works in conjunction with the Photo Labeller Android app

The server is in charge of the following tasks:

* Create an initial model
* Control the training rounds: when to open and close them
* Do the update of the model once a round is finished using the info uploaded by the clients

## Installation instructions
Use IntelliJ to build the project

## Training the initial model and testing it
To run the initial model training, use the [`Main.kt`](https://github.com/mccorby/PhotoLabellerServer/blob/master/model/src/main/kotlin/com/mccorby/photolabeller/ml/Main.kt) file

The *training process* expects three arguments:
* `train` string to trigger the process
* A valid directory to save the resulting model
* The third optional argument can be `web` to start the UI monitor provided by DL4J

You can also run a prediction to test your model. The expected arguments are:
* Location of the model
* Location of an image to classify

## Running the Federated Parameter Server
The server is in charge of starting the training rounds and to keep the updates sent by the clients until the shared model has been updated

The training round starts when the server is initialised. In a real life system, the training round would be triggered either automatically or manually by someone

Execute the `JobQueueServer` main method and the server will be up and running

The REST API is quite simple:
* `GET` model to get the latest shared model in this server
* `POST` model to upload the updates from the clients
* `GET` round to obtain the info about the current round

To check that the server is up and running in your local installation, hit this URL

```http://localhost:9997/service/federatedservice/available```

### Flow of an open round
* Client update is received

When a client sends the update to the model, this is stored by the server in the current round directory

The update is also cached in memory to help with the post process


* Closing the round and updating the model

A training round can be closed by reaching the minimum number of clients updates or by timing out

Once the round is closed, the updates are processed by using the `UpdatesStrategy` injected in the server. The current implementation of this strategy performs a `Federated Averaging`

The model is then updated (actually just the last feature layer) and saved. From this moment this is considered the latest version of the shared model

All client updates are deleted so that no trace of them remains in the server
