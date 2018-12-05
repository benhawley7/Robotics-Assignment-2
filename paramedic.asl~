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
		+cfp(CNPId,Task,C,NC)[source(A)]:
			plays(initiator,A) & price(Task,Offer)
			<-
			+proposal(CNPId,Task,C,NC,Offer);
			.send(A,tell,propose(CNPId,Offer)).
		
		//When this agent knows its proposal has been accepted
		//Get the scenario from the source
		//Start the rescue mission
		+accept_proposal(CNPId)[source(A)]:
			proposal(CNPId,Task,C,NC,Offer)
			<- 
			.send(A,askAll,location(_,_,_));
			+rescueMission(C,NC).
		
		//When this agent knows its proposal has been rejected
		//Clear its proposal
		+reject_proposal(CNPId)<- 
			.print("I lost CNP ",CNPId, ".");
			-proposal(CNPId,_,_).
	
	/*Plan Library for Beliefs */
	
		-carrying(_):
			rescueMission(Critical,NonCritical)&
			Critical = 0 &
			NonCritical = 0&
			~carryingVictim(_)
			<-
			allRescued.
		
		+rescueMission(0,0)<-
			finish.
			
		//When a rescue mission has been started, and there is a hospital,
		//a victim and an obstacle:
		+rescueMission(Critical,NonCritical): 
			location(hospital,_,_) & 
			(location(victim,_,_) | ~critical(_,_) | critical(_,_)) &
			location(obstacle,_,_)&
			not carringVictim(_)
			<- 

			criticals(Critical);
			nextTarget;
			.print("Rescue mission target found")
			.wait(1000);
			?nearest(X,Y);
			!rescue(X,Y).
		
		//If we have tried to start a rescue mission without knowing
		// the scenario, wait until we have the scenario and try again
		+rescueMission(C,NC)
			<- .wait(2000);
			   -+rescueMission(C,NC).
			   
		+location(hospital,X,Y) <- 
			addHospital(X,Y);
			.print(hospital,X,Y).
			
		+location(victim,X,Y) <- 
			addVictim(X,Y);
			.print(victim,X,Y).
			
		+location(obstacle,X,Y) <- 
			addObstacle(X,Y);
			.print(obstacle,X,Y).
		
		@addNear[atomic]+newNearest(X,Y,Index)<-
			.print("Current nearest: ",X,Y);
			-nearest(I,J);
			+nearest(X,Y).
			//-newNearest[source(percerpt)].
			//?nearest(A,B);
			//.print("New Nearest: ",A,B).
		
		+critical(X,Y): location(self,X,Y) <-
			criticalVictimAt(X,Y);
			pickUpVictim(X,Y);
			+carryingVictim(critical);
			-location(victim,X,Y)[source(_)];
			-critical(X,Y)[source(_)];
			noVictimAt(X,Y);
			-critical(X,Y);
			?rescueMission(C,NC);
			if(C=1 & NC = 0){allLocated};
			!moveTo(0,0).
		
		+~critical(X,Y):
			location(self,X,Y)&
			rescueMission(C,NC)&
			C>0
			<-
			.print("Non critical found");
			nonCriticalVictimAt(X,Y);
			-location(victim,X,Y)[source(_)];
			nextTarget;
			.print("non Critical target found");
			.wait(1000);
			?nearest(A,B);
			!rescue(A,B).	
			
		+~critical(X,Y): 
			location(self,X,Y)&
			rescueMission(C,NC)&
			C=0
			<-
			.print("Non critical found");
			nonCriticalVictimAt(X,Y);
			pickUpVictim(X,Y);
			
			+carryingVictim(~critical);
			-location(victim,X,Y)[source(_)];
			-~critical(X,Y)[source(_)];
			?rescueMission(C,NC);
			if(C=0 & NC = 1){allLocated};
			!moveTo(0,0).
			
		+location(self,0,0): carryingVictim(S) <-
			putDownVictim;
			-carryingVictim(_);
			?rescueMission(C,NC);
			-rescueMission(C,NC);
			if(S=critical){+rescueMission(C-1,NC)};
			if(S=~critical){+rescueMission(C,NC-1)}.
			
		+location(self,X,Y): ~critical(X,Y) <-
			pickUpVictim(X,Y);
			+carryingVictim(~critical);
			-location(victim,X,Y)[source(_)];
			-~critical(X,Y)[source(_)];
			?rescueMission(C,NC);
			if(C=0 & NC = 1){allLocated};
			!moveTo(0,0).
			
		+colour(X,Y,white)<-
			-location(victim,X,Y)[source(_)];
			noVictimAt(X,Y);
			.print("No victim at ",X,Y);
			nextTarget;
			.print("No victim target found");
			.wait(1000);
			?nearest(A,B);
			!rescue(A,B).
			
		
			   
	/*Plan Library for Goals */
		
		+!rescue(X,Y)<-
			!moveTo(X,Y);
			inspectVictim(X,Y);
			?colour(X,Y,COLOUR);
			?plays(initiator,D);
			.send(D,tell, requestVictimStatus(X,Y,COLOUR)).
		
		//When we want to move somewhere, move there and
		//update our position
		+!moveTo(X,Y) <- 
			moveTo(X,Y); 
			.print(X," ",Y);
			-location(self,_,_);    
			+location(self,X,Y).
