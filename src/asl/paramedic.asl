// Paramedic Agent
// 23rd November 2018 - Terry Payne
// This is the AgentSpeak code for an example paramedic agent, that
// responds to a call to assist in rescuing victims, based on triaging
// possible victims in a number of candidate locations.  This is partially
// achieved using the CONTRACT NET PROTOCOL, based on the code that was
// presented in the COMP329 Lectures: 24-25 Communication through AgentSpeak 
//
// Differences from the CNP Code
// 1) The definition of the plan @c1 for the addition of the cfp belief
// has been extended to accept calls that include additional information
// regarding the number of critical and non-critical victims.  These are
// then included in the mental note proposal(CNPId,Task,C,NC,Offer);
//
// 2) The rule for accepting the contract, @r1 +accept_proposal() has been
// extended with two other achievement tasks: 
//    !getScenario(A) - this obtains the beliefs regarding the location of 
//    the obstacles, the victim locations and the hospital
//
//    startRescueMission(A,C,NC) - this mental note is created if the cfp
//    was successful, and the request for locations was successfully sent.
//    We cannot immediately start the mission, until we have all the beliefs,
//    and so there are two plans that respond to the creation of this belief;
//    one that checks if we have the location information, which then is the
//    starting point for the mission.  The other simply waits for a couple of
//    seconds and then replaces the mental note, which should cause one of the
//    plans to be recursively generated.

// ========================================================================
// Initial beliefs and rules
// ========================================================================
// Determine the cost in assisting with the task.
// Truth is that we don't actually need this, but we will be using a variant
// of the CNP, which awards the contract to the agent with the lowest cost.
// In this assignment, only one agent will participate in the solution, and
// so the value generated here is somewhat arbitrary!
price(_Service,X) :- .random(R) & X = (10*R)+100.

// the name of the agent who is the initiator in the CNP
plays(initiator,doctor).

// ========================================================================
// Initial goals
// ========================================================================

// ========================================================================
// Plan Library
// ========================================================================
// Plans for the CNP
// send a message to the initiator introducing the agent as a participant 
+plays(initiator,In)
   :  .my_name(Me)
   <- .send(In,tell,introduction(participant,Me)).

// answer to Call For Proposal
@c1 +cfp(CNPId,Task,C,NC)[source(A)]
   :  plays(initiator,A) & price(Task,Offer)
   <- +proposal(CNPId,Task,C,NC,Offer);		// remember my proposal
      .send(A,tell,propose(CNPId,Offer)).

// Handling an Accept message
@r1 +accept_proposal(CNPId)[source(A)]
		: proposal(CNPId,Task,C,NC,Offer)
		<- !getScenario(A);
		    +startRescueMission(A,C,NC).
 
// Handling a Reject message
@r2 +reject_proposal(CNPId)
		<- .print("I lost CNP ",CNPId, ".");
		// clear memory
		-proposal(CNPId,_,_).

// ========================================================================
// Plan Library for beliefs and mental notes 
// ========================================================================
// Plan for responding to the creation of the new mental note
// startRescueMission(D,C,NC).  Although this is not technically
// modelled as an AgentSpeak intention (we have not covered how
// AgentSpeak manages BDI explicitly), this could be thought of
// as a new intention, and so the creation of the belief starts
// the mission!  This is the starting point of Assignment 2.
+startRescueMission(D,C,NC) : location(hospital,X,Y) & 
							  location(victim,_,_) &
							  location(obstacle,_,_)
    <- .count(location(victim,_,_),Vcount);		// Determine the victims
       .count(location(obstacle,_,_),Ocount);	// Determine the obstacles
       .print("Start the Resuce mission for ",C," critical and ",NC,
    		" non-critical victims; Hospital is at (",X,",",Y,"), and we have ",
    		Vcount, " victimes and ", Ocount," known obstacles").
 
// This is our recursive plan that only executes if we have yet to receive
// the beliefs    
+startRescueMission(D,C,NC)
    <- .wait(2000);  				// wait for the beliefs to be obtained
       -+startRescueMission(D,C,NC).// replace the mental note
    		
// ========================================================================
// Plan for responding to the acquisition of beliefs about the environment
// as a result of the askAll request in the plan !getScenario(D). This could
// be extended by adding environment actions to inform the JASON Environment
// of the features of the world.  This is done below with the actions
//    addVictim(X,Y)
//    addObstacle(X,Y)
//    addHospital(X,Y)
// You will probably need to do something like this to update your model.

+location(victim,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Victim could be at ",X,", ",Y); addVictim(X,Y).

+location(obstacle,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Obstacle is at ",X,", ",Y); addObstacle(X,Y).
    
+location(hospital,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Hospital is at ",X,", ",Y); addHospital(X,Y).
    
// ========================================================================
// Plan for responding to the critical status of victims at certain locations
// in the environment.  These are generated through communication with the
// doctor agents, via the achievement task +!requestVictimStatus(D,X,Y,C)

+critical(X,Y)
    <- .print("The victim at ", X, ",", Y, " is critical").

+~critical(X,Y)
    <- .print("The victim at ", X, ",", Y, " is not critical").


// ========================================================================
// Plan Library for achievement goals 
// ========================================================================
// Request the beliefs of the locations.  D is the doctor agent
+!getScenario(D) <- .send(D,askAll,location(_,_,_)).
	
// Check the status of a victim
// D is the doctor agent, X, Y are cell coordinates, and
// C is a colour from the set {burgandy,cyan}
// If the doctor knows the colour, it will send a belief for the 
// location X,Y to say if the victim is critical or not
//
// For example:
//			!requestVictimStatus(A,3,1,burgandy);
//			!requestVictimStatus(A,3,2,cyan);

+!requestVictimStatus(D,X,Y,C)
    <- .send(D, tell, requestVictimStatus(X,Y,C)).
    
// ========================================================================
// End 
// ========================================================================
    