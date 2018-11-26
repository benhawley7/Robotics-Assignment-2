// Agent paramedic in project Assignment2.mas2j

////////////// Initial beliefs and rules ///////////////////////////////////////

	//Value of price doesn't matter as there is only one agent
	price(_Service,X):- X = 1.
	
	location(self,0,0).
	
	//The doctor is playing the part of the initiator
	plays(initiator,doctor).
	
	nearest(self, Nx, Ny):-
		location(self, Sx, Sy)&
		location(victim, Nx, Ny)&
		.print(Nx-Sx + Ny-Sy).	

///////////////////// Initial goals ///////////////////////////////////////////



///////////////////////// Plans ////////////////////////////////////////////////

	/*Contract Net Protocol Implementation*/
	
		//When this agent knows who plays the part of initiator
		//Introduce this agent to the initiator as a participant
		+plays(initiator, In): .my_name(Me)
			<- .send(In,tell,introduction(participant,Me)).
		
		//When this agent knows there is a Call For Proposal, respond to it
		+cfp(CNPId,Task,C,NC)[source(A)]
		   :  plays(initiator,A) & price(Task,Offer)
		   <- +proposal(CNPId,Task,C,NC,Offer);		// remember my proposal
			  .send(A,tell,propose(CNPId,Offer)).
		
		//When this agent knows its proposal has been accepted
		//Get the scenario from the source
		//Start the rescue mission
		+accept_proposal(CNPId)[source(A)]
			: proposal(CNPId,Task,C,NC,Offer)
			<- !getScenario(A);
				+startRescueMission(A,C,NC).
		
		//When this agent knows its proposal has been rejected
		//Clear its proposal
		+reject_proposal(CNPId)
				<- .print("I lost CNP ",CNPId, ".");
				// clear memory
				-proposal(CNPId,_,_).
	
	/*Plan Library for Beliefs */
		
		//When a rescue mission has been started, and there is a hospital,
		//a victim and an obstacle:
		+startRescueMission(Doctor,Critical,NonCritical): 
			location(hospital,_,_) & 
			location(victim,_,_) &
			location(obstacle,_,_)
			<- nearest(self, Nx, Ny).
		
		//If we have tried to start a rescue mission without knowing
		// the scenario, wait until we have the scenario and try again
		+startRescueMission(D,C,NC)
			<- .wait(2000);
			   -+startRescueMission(D,C,NC).
			   
		+location(N,X,Y) <- .print(N,X,Y).
		
		
			   
	/*Plan Library for Goals */
	
		//When this agent wants to get the scenario
		//Ask the doctor for the location of all victims
		+!getScenario(D) <- .send(D,askAll,location(_,_,_)).
		
		//When we want to move somewhere, move there and
		//update our position
		+!moveTo(X,Y) <- 
			//TODO(): implement moveTo in environment
			//.moveTo(X,Y); 
			-location(self,_,_);
			+location(self,X,Y).
