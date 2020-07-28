package programmingInScala

import scala.collection.mutable

object JobScheduler {

  /*
You are given a list of jobs where each job could depend on 0 or more other jobs.
Each job can begin execution only when its dependent jobs complete execution.
Can you write a function to print out the sequence in which a job scheduler might execute them?

Job1 -> Job2, Job3, Job4 (Job1, Job3, Job4 can be executed in any order) ==>
Job1 -> Job2->Job4 ==> Job4, Job2, Job1

Input: 1, 2, 3, 4, 5, 6 -- DAG
6 -> 2
2 -> 4
1 -> 2, 3
3 -> 4
4 -> 5, 1
*/

  case class Job(name: String, dependencies: Seq[Job])

  def executeJobs(jobs: Seq[Job]): Seq[String] = {
    require(jobs != null)

    jobs match {
      case Seq() => Seq[String]()
      case _ => doWork(jobs)
    }
  }

  private def doWork(jobs: Seq[Job]): Seq[String] = {
    var result: Seq[String] = Seq()
    val alreadyExecuted: mutable.Set[Job] = mutable.Set()

    jobs.foreach { job =>
      result = result ++ execute(job, alreadyExecuted)
    }

    result
  }

  // Seq = (5, 4, 2, 3, 1)
  // alreadyExecuted: 5, 4, 2, 3, 1
  private def execute(job: Job, alreadyExecuted: mutable.Set[Job]): Seq[String] = {
    //val parents: Set[Job] // TODO code does not handel cyclic dependencies
    if (alreadyExecuted.contains(job)) {
      return Seq()
    } else {
      val dependencies = job.dependencies // job=1, dependencies=2,3
      require(dependencies != null)

      if (dependencies.isEmpty) {
        alreadyExecuted.addOne(job)
        return Seq(job.name)
      } else {
        var result: Seq[String] = Seq()
        dependencies.foreach { d => result = result ++ execute(job, alreadyExecuted) }
        alreadyExecuted.addOne(job)
        return result :+ job.name
      }
    }
  }
}
