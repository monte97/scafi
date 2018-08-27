package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment
import it.unibo.scafi.simulation.gui.controller.input.{InputCommandController, InputController}
import it.unibo.scafi.simulation.gui.controller.logical.{ExternalSimulation, LogicController}
import it.unibo.scafi.simulation.gui.controller.presenter.Presenter
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.view.{SimulationView, WindowConfiguration}

/**
  * scafi enviroment
  * @param presenter the presenter of scafi simulation
  * @param simulation the simulation
  * @param policy the policy
  * @param controller the controller
  */
class ScafiProgramEnvironment(val presenter : Presenter[ScafiLikeWorld,SimulationView],
                              val simulation : ExternalSimulation[ScafiLikeWorld],
                              val policy : ProgramEnvironment.PerformancePolicy,
                              val controller : LogicController[ScafiLikeWorld]*)
                              extends ProgramEnvironment[ScafiLikeWorld,SimulationView] {

  override val input: InputController = InputCommandController
}
