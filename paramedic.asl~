// Agent paramedic in project Assignment2.mas2j



////////////// Initial beliefs and rules ///////////////////////////////////////


	//Value of price doesn't matter as there is only one agent
	price(_Service,X):- X = 1.
	

	//The doctor is playing the part of the initiator
	plays(initiator,doctor).
		


///////////////////// Initial goals ////////////////////////////////////////////





///////////////////////// Plans ////////////////////////////////////////////////


	/*Contract Net Protocol Implementation*/
	
		//When this agent knows who plays the part of initiator
		//Introduce this agent to the initiator as a participant
		+plays(initiator, In): .my_name(Me)<-
			.send(In,tell,introduction(participant,Me));
			.print("Intro done").
		
		//When this agent knows there is a Call For Proposal, respond to it
		+cfp(CNPId,Task,C,NC)[source(A)]:
			plays(initiator,A) &
			price(Task,Offer)
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
		
		//When there are nomore critical or non critical victims, tell the
		//environment we are finished
		+rescueMission(0,0)<-
			finish.
			
		//When a rescue mission has been started, and there is a hospital,
		//a victim and an obstacle and we are localised:
		+rescueMission(Critical,NonCritical):
			location(self,_,_) &
			location(hospital,_,_) & 
			(location(victim,_,_) | ~critical(_,_) | critical(_,_)) &
			location(obstacle,_,_)&
			not carringVictim(_)
			<- 
			
			//Tell the environment how many criticals there are
			criticals(Critical);
			
			//Choose the next target
			nextTarget;
			.wait(1000);
			
			//Attempt to rescue the nearest potential victim
			?nearest(X,Y);
			!rescue(X,Y).
		
		//If we try to start a rescue mission without knowing where we are,
		//localise ourselves
		+rescueMission(C,NC):
			location(hospital,_,_) & 
			(location(victim,_,_) | ~critical(_,_) | critical(_,_)) &
			location(obstacle,_,_)
			<-
			localise;
			-+rescueMission(C,NC).
		
		//If we have tried to start a rescue mission without knowing
		// the scenario, wait until we have the scenario and try again
		+rescueMission(C,NC)
			<- .wait(2000);
			   -+rescueMission(C,NC).
		
		//When the doctor tells us where the hospital is, tell the environment
		+location(hospital,X,Y) <- 
			addHospital(X,Y);
			.print(hospital,X,Y).
		
		//When the doctor tells us where the hospital is, tell the environment
		+location(victim,X,Y) <- 
			addVictim(X,Y);
			.print(victim,X,Y).
		
		//When the doctor tells us where the hospital is, tell the environment
		+location(obstacle,X,Y) <- 
			addObstacle(X,Y);
			.print(obstacle,X,Y).
		
		//When the environment tells us there is a new nearest potential victim,
		//change the location of the nearest victim
		@addNear[atomic]+newNearest(X,Y,Index)<-
			.print("Current nearest: ",X,Y);
			-nearest(I,J);
			+nearest(X,Y).
		
		//When we discover a critical victim at the location we are at
		+critical(X,Y): location(self,X,Y) <-
			//Tell the environment there is a critical victim and that we are
			//picking it up
			criticalVictimAt(X,Y);
			pickUpVictim(X,Y);
			
			//Carry the victim
			+carryingVictim(critical);
			
			//Remove the location of the victim from our beliefs
			-location(victim,X,Y)[source(_)];
			-critical(X,Y)[source(_)];
			
			//Tell the environment there is no longer a victim in this cell
			noVictimAt(X,Y);
			
			?rescueMission(C,NC);
			
			//If this is the last critical victim to be found, tell the
			//environment we have located all victims
			if(C=1 & NC = 0){allLocated};
			
			//Set a goal to move to the hospital
			!moveTo(0,0).
		
		//If we discover a non critical victim at our location, and there are
		//still critical victims to be found:
		+~critical(X,Y):
			location(self,X,Y)&
			rescueMission(C,NC)&
			C>0
			<-
			.print("Non critical found");
			
			//Tell the environment there is a non critical victim here
			nonCriticalVictimAt(X,Y);
			
			//There is no longer a potential victim here
			-location(victim,X,Y)[source(_)];
			
			//Calculate the next potential victim to visit
			nextTarget;
			.wait(1000);
			
			//Attempt to rescue the nearest potential victim
			?nearest(A,B);
			!rescue(A,B).	
		
		//If we discover a non critical victim at our location, and there are
		//no critical victims to be found:
		+~critical(X,Y): 
			location(self,X,Y)&
			rescueMission(C,NC)&
			C=0
			<-
			.print("Non critical found");
			
			//Tell the environment there is a non critical victim here and we 
			//are picking it up
			nonCriticalVictimAt(X,Y);
			pickUpVictim(X,Y);
			
			//Carry the victim
			+carryingVictim(~critical);
			
			//There is no longer a victim at this location
			-location(victim,X,Y)[source(_)];
			-~critical(X,Y)[source(_)];
			
			?rescueMission(C,NC);
			
			//If this is the last non critical victim, tell the environment we 
			//have located all victims
			if(C=0 & NC = 1){allLocated};
			
			//Set a goal to move to the hospital
			!moveTo(0,0).
		
		//If we are at the location of a non critical victim we have previously
		//visited
		+location(self,X,Y): ~critical(X,Y) <-
		
			//Tell the environment we are picking up the victim
			pickUpVictim(X,Y);
			
			//carry the victim
			+carryingVictim(~critical);
			
			//There is no longer a victim at this location
			-location(victim,X,Y)[source(_)];
			-~critical(X,Y)[source(_)];
			
			?rescueMission(C,NC);
			
			//If this is the last non critical victim, tell the environment we 
			//have located all victims
			if(C=0 & NC = 1){allLocated};
			
			//Set a goal to move to the hospital
			!moveTo(0,0).	
			
		//If this agent is at the hospital and is carrying a victim
		+location(self,0,0): carryingVictim(S) <-
			//Tell the environment we are putting the victim down
			putDownVictim;
			
			//Drop the victim
			-carryingVictim(_);
			
			?rescueMission(C,NC);
			
			//Change the count of critical and non critical victims, which will
			// start a new rescue mission
			-rescueMission(C,NC);
			if(S=critical){+rescueMission(C-1,NC)};
			if(S=~critical){+rescueMission(C,NC-1)}.
			
		//If we find the square we are on is white	
		+colour(X,Y,white)<-
		
			//There is no victim here
			-location(victim,X,Y)[source(_)];
			
			//Tell the environment there is no victim here
			noVictimAt(X,Y);
			.print("No victim at ",X,Y);
			
			//Calculate the next potential victim to visit
			nextTarget;
			.wait(1000);
			
			//Attempt to rescue the nearest potential victim
			?nearest(A,B);
			!rescue(A,B).
			
		
			   
	/*Plan Library for Goals */
		
		//When this agent wants to try to rescue the potential victim at X,Y
		+!rescue(X,Y)<-
			
			//Move to X,Y
			!moveTo(X,Y);
			
			//Ask the environment what colour the square we are on is
			inspectVictim(X,Y);
			?colour(X,Y,COLOUR);
			
			//Ask the doctor for the status of the victim
			?plays(initiator,D);
			.send(D,tell, requestVictimStatus(X,Y,COLOUR)).
		
		//When we want to move somewhere, move there and
		//update our position
		+!moveTo(X,Y) <- 
			moveTo(X,Y); 
			.print(X," ",Y);
			-location(self,_,_);    
			+location(self,X,Y).
