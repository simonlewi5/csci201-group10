# Egyptian Ratscrew

This game was created as part of USC's CSCI 201 final project. We were tasked with creating a web-based project using Java either on the frontend or backend. We chose to create an online multiplayer card game, specifically Egyptian Ratscrew, due to the concurrent nature of the game's slap mechanic and the relative simplicity of the game's rules.

## Gameplay

Please see our demo video [here](https://youtu.be/skFXh_aqBw0) to see the game in action.
Also known as Egyptian War, this is a fast-paced card game that is easy to learn and fun to play. The game is played with a standard deck of 52 cards, and the objective is to win all the cards.

## Architecture

![Architecture Diagram](./Architecture%20Diagram.drawio.light.png)

The game is built using a client-server architecture. The server is responsible for managing the game state, handling game logic, and broadcasting updates to all connected clients. The clients are responsible for rendering the game state and sending user input to the server.

The server is built using Go and gorilla websockets. The game client is built using the Java LibGDX framework.

All artwork credit goes to team member [Alice Sun](https://github.com/alicesunn).

## Deployment

Note: The game is no longer deployed due to the cost of hosting the resources in GCP and the expiration of the free trial.

The game was deployed on Google Cloud Platform (GCP) using Google's Compute Engine and Cloud Storage services. The server was hosted on a Compute Engine instance running a Go server. The client was hosted on a Cloud Storage bucket and made downloadable via presigned URLs on our website. All deployment was handled via Github Actions as can be seen in the actions tab of this repository and the various workflow files in the `.github/workflows` directory.

Resources were defined in Terraform and deployed using Github Actions. The Terraform configuration can be found in the `infrastructure` directory.



