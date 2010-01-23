package aima.search.local

sealed abstract class SearchResult[S,A]
case class Success[S,A](plan: Plan[S,A]) extends SearchResult[S,A]
case class Failure[S,A] extends SearchResult[S,A]

/* And-Or-Graph-Search, described in Fig 4.11 */
object AndOrGraphSearch {
  def apply[S,A](problem: NonDeterministicProblem[S,A]): SearchResult[S,A] =
    OrSearch(problem.initialState,problem,List[S]())

  //Returns Failure/Success(ResultTree)
  private def OrSearch[S,A](state: S, problem: NonDeterministicProblem[S,A], path: List[S]): SearchResult[S,A] = {
     if (problem.goalTest(state))
      Success(new Plan[S,A](state,None))
    else {
      if (path.exists(_ == state)) Failure()
      else {
        def loop(actions: List[A]): SearchResult[S,A] =
          actions match {
            case action :: rest =>
              AndSearch(problem.results(state,action),problem, state :: path,state,action) match {
                case Success(x) => Success(x)
                case Failure() => loop(rest)
              }
            case Nil => Failure()
          }
        loop(problem.actions(state))
      }
    }
  }

  private def AndSearch[S,A](states: List[S], problem: NonDeterministicProblem[S,A],path: List[S],prevState: S, prevAction: A): SearchResult[S,A] = {
    if(states.length == 0)
      throw new IllegalStateException("From " + prevState + " action " + prevAction + " results in no states.")
    
    var result = new Plan(prevState,Some(prevAction))
    def loop(states: List[S]): SearchResult[S,A] = {
      states match {
        case state :: rest =>
          OrSearch(state,problem,path) match {
            case Success(x) => result.addChild(x); loop(rest)
            case _ => Failure()
          }
        case Nil => Success(result)
      }
    }
    loop(states)
  }
}


/*
 * The And-Or-Graph-Search can return a Plan tree.
 *
 * For example, the Plan tree for solution described
 * in Fig 4.10 looks like following(numbers denote the
 * corresponding states shown in the figure for that number)..
 * 
 *             (1,Some(Suck))
 *                |
 *    |-----------------------|
 * (7,None)                (5,Some(Right))
 *                            |
 *                         (6,Some(Suck))
 *                            |
 *                         (8,None)
 *
 *
 */
class Plan[S,A](s: S, a: Option[A]) {
  private val children = new scala.collection.jcl.ArrayList[Plan[S,A]](new java.util.ArrayList[Plan[S,A]]())

  def addChild(plan: Plan[S,A]) { children.add(plan) }
  def isLeaf = children.length == 0

  def state = s
  def action = a

  //Only for the testing purpose, for any computation
  //Plan instance itself should be used
  override def toString() = {
    var result = rawActionString(a)
    if(children.length == 1)
      result = result + " " + children(0).toString()
    else {
      if(children.length > 0) {
        for(x <- children) {
          result = result + " IF " + x.state + " THEN [" + x.toString() + "] ELSE "
        }
        result = result.substring(0,result.length-6)
      }
    }
    result
  }

  private def rawActionString(a: Option[A]):String =
    a match {
      case Some(x) => x.toString()
      case None => "NoOp"
    }
}

abstract class NonDeterministicProblem[S,A](initState: S) extends Problem[S,A](initState) {

  def results(state: S, action: A): List[S]

  override def result(state: S, action: A):S =
    throw new UnsupportedOperationException("result does not exist for non-deterministic problem.")

  override def successorFn(state: S) =
    throw new UnsupportedOperationException("result does not exist.")
}

//state is represented by (agent-location,dirt-in-A?,dirt-in-B?)
//possible actions are SUCK/LEFT/RIGHT
class VacuumWorldNonDeterministicProblem(initLocation: String) extends NonDeterministicProblem[(String,Boolean,Boolean),String]((initLocation,true,true)) {
  private val Suck = "Suck"
  private val Right = "Right"
  private val Left = "Left"
  private val A = "A"
  private val B = "B"

  type State = (String,Boolean,Boolean)
  def actions(state: State): List[String] =
    state match {
      case (A,_,_) => List(Suck,Right)
      case (B,_,_) => List(Left,Suck)
      case _ => throw new IllegalStateException(state + " is not a valid vacuum world state")
    }

  def results(state: State, action: String):List[State] =
    (action,state) match {
      case (Left,(_,a,b)) => List((A,a,b))
      case (Right,(_,a,b)) => List((B,a,b))
      case (Suck, (A,true,true)) => List((A,false,false),(A,false,true))
      case (Suck, (A,true,false)) => List((A,false,false))
      case (Suck, (A,false,b)) => List((A,false,b),(A,true,b))
      case (Suck, (B,true,true)) => List((B,false,false),(B,true,false))
      case (Suck, (B,false,true)) => List((B,false,false))
      case (Suck, (B,a,false)) => List((B,a,true),(B,a,false))
      case _ => throw new IllegalStateException("Either invalid action: " + action + " or invalid state: " + state)
    }

  def goalTest(state: State) =
    state match {
      case (_,false,false) => true
      case _ => false
    }

  //TODO: change the signature
  def stepCost(from: State,to :State): Double =
    throw new UnsupportedOperationException("stepCost is not supported.")

  def estimatedCostToGoal(state: State): Double =
    throw new UnsupportedOperationException("estimatedCostToGoal is not supported.")
}