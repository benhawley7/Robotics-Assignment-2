// Agent paramedic in project Assignment2.mas2j



////////////// Initial beliefs and rules ///////////////////////////////////////


	//Value of price doesn't matter as there is only one agent
	price(_Service,X):- X = 1.
	

	//The doctor is playing the part of the initiator
	plays(initiator,doctor).
		


///////////////////// Initial goals ///////////////////////////////////////////





///////////////////////// Plans ////////////////////////////////////////////////


	/*Contract Net Protocol Implementation*/
	
		//When this agent knows who plays the part of initiator
		//Introduce this agent to the initiator as a participant
		+plays(initiator, In): .my_name(Me)
			<- .send(In,tell,introduction(participant,Me));
			.print("Intro done").
		
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
				+rescueMission(A,C,NC).
		
		//When this agent knows its proposal has been rejected
		//Clear its proposal
		+reject_proposal(CNPId)
				<- .print("I lost CNP ",CNPId, ".");
				// clear memory
				-proposal(CNPId,_,_).
	
	/*Plan Library for Beliefs */
		
		//When a rescue mission has been started, and there is a hospital,
		//a victim and an obstacle:
		+rescueMission(Doctor,Critical,NonCritical): 
			location(hospital,_,_) & 
			location(victim,_,_) &
			location(obstacle,_,_)
			<- 
			
			nextTarget;
			.print("next target found")
			?nearest(X,Y);
			!rescue(X,Y).
		
		//If we have tried to start a rescue mission without knowing
		// the scenario, wait until we have the scenario and try again
		+rescueMission(D,C,NC)
			<- .wait(2000);
			   -+rescueMission(D,C,NC).
			   
		+location(hospital,X,Y) <- 
			addHospital(X,Y);
			.print(hospital,X,Y).
			
		+location(victim,X,Y) <- 
			addVictim(X,Y);
			.print(victim,X,Y).
			
		+location(obstacle,X,Y) <- 
			addObstacle(X,Y);
			.print(obstacle,X,Y).
		
		+nearest(X,Y): nearest(I,J) & not X=I & not Y = J<-
			-nearest(I,J);
			?nearest(A,B);
			.print("Nearest: ",A,B).
			
		+critical(X,Y): location(self,X,Y) <-
			pickUpVictim(X,Y);
			+carryingVictim(critical);
			-location(victim,X,Y);
			removeVictim(X,Y);
			-critical(X,Y);
			!moveTo(0,0).
			
		+location(self,0,0): carryingVictim(critical) <-
			putDownVictim;
			-carryingVictim(_);
			-startRescueMission(Doctor,Critical,NonCritical);
			+startRescueMission(Doctor,Critical-1,NonCritical).
		
		+location(self,0,0): carryingVictim(nonCritical) <-
			putDownVictim;
			-carryingVictim(_);
			-startRescueMission(Doctor,Critical,NonCritical);
			+startRescueMission(Doctor,Critical,NonCritical-1).
			
		+haveChecked(X,Y): not critical(X,Y) <-
			-location(victim,X,Y);
			removeVictim(X,Y);
			.print("No victim at ",X,Y);
			nextTarget;
			?nearest(A,B);
			!rescue(A,B).
			
		
			   
	/*Plan Library for Goals */
	
		//When this agent wants to get the scenario
		//Ask the doctor for the location of all victims
		+!getScenario(D) <- .send(D,askAll,location(_,_,_)).
		
		+!rescue(X,Y)<-
			!moveTo(X,Y);
			getColour(X,Y);
			+colour(X,Y,burgandy);
			?colour(X,Y,COLOUR);
			?plays(initiator,D);
			.send(D,tell, requestVictimStatus(X,Y,COLOUR));
			.wait(2000);
			+haveChecked(X,Y).
		
		//When we want to move somewhere, move there and
		//update our position
		+!moveTo(X,Y) <- 
			//TODO(): implement moveTo in environment
			moveTo(X,Y); 
			.print(X," ",Y);
			-location(self,_,_);
			
			+location(self,X,Y).
