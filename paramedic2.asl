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
			!rescueMission(C,NC).
		
		//When this agent knows its proposal has been rejected
		//Clear its proposal
		+reject_proposal(CNPId)<- 
			.print("I lost CNP ",CNPId, ".");
			-proposal(CNPId,_,_).
	
	/*Plan Library for Beliefs */
		

			
		
			   
	/*Plan Library for Goals */
		
		+!rescueMission(C,NC): C > 0 & NC > 0 <-
			moveToNextTarget;
			inspectPatient;
			?colour(X,Y,Colour);
			?plays(initiator,D);
			.send(D,tell, requestVictimStatus(X,Y,COLOUR)).
		
		+!rescueMission(C,NC).

