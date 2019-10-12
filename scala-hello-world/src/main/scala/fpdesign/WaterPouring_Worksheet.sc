class Pouring(capacity: Vector[Int]) { // capacity: a list of capacities each representing a glass

  type State = Vector[Int]
  private val initialState: State = capacity map (_ => 0) // all glasses are empty to start with
  private val glasses = 0 until capacity.length
  val moves: IndexedSeq[Move] = {
    (for (g <- glasses) yield Fill(g)) ++
      (for (g <- glasses) yield Empty(g)) ++
      (for (from <- glasses; to <- glasses; if from != to) yield Pour(from, to))
  }

  val initialPath: Path = new Path(List(), initialState)

  trait Move {
    def changeState(state: State): State
  }

  // glass: the index of the glass in the capacity vector
  case class Empty(glassIndex: Int) extends Move {
    override def changeState(state: State): State = {
      state.updated(glassIndex, 0)
    }
  }

  case class Fill(glassIndex: Int) extends Move {
    override def changeState(state: State): State = {
      state.updated(glassIndex, capacity(glassIndex))
    }
  }

  case class Pour(fromIndex: Int, toIndex: Int) extends Move {
    override def changeState(state: State): State = {
      val amount = Math.min(state(fromIndex), capacity(toIndex) - state(toIndex))
      state updated(fromIndex, state(fromIndex) - amount) updated(toIndex, state(toIndex) + amount)
    }
  }

  class Path(moves: List[Move], val endState: State) {
    override def toString: String = {
      (moves.reverse mkString " ") + "--> " + endState
    }

    def extend(move: Move): Path = {
      new Path(move :: moves, move changeState endState)
    }
  }

  def from(paths: Set[Path]): LazyList[Set[Path]] = {
    if (paths.isEmpty) LazyList.empty
    else {
      val statesExplored: Set[State] = paths map (path => path.endState)
      val more: Set[Path] = {
        for {
          path <- paths
          next <- (moves map (move => path extend move))
          if !(statesExplored contains next.endState)
        } yield next
      }

      paths #:: from(more)
    }
  }

  val pathSets: LazyList[Set[Path]] = from(Set(initialPath))
  def solutions(target: Int): LazyList[Path] = {
    require(capacity exists (c => c >= target), "target=" +target + ", capacity=" + capacity)
    for {
      pathSet <- pathSets
      path <- pathSet
      if path.endState contains target
    } yield path
  }
}

// Given X glasses, each has an integer capacity. Find N solutions that will
// yield the target amount.  The target capacity must fit entirely in a single glass.
val problem = new Pouring(Vector(4, 9))
problem.solutions(6).take(1).toList mkString "\n"
// Fill(1) Pour(1,0) Empty(0) Pour(1,0) Empty(0) Pour(1,0) Fill(1) Pour(1,0)--> Vector(4, 6)

problem.moves
problem.initialPath


